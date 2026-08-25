package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BrizTopAppBar
import com.example.ui.components.ChatInputBar
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.HeroEmptyState
import com.example.ui.components.SessionsDrawerContent
import com.example.ui.components.SettingsBottomSheet
import com.example.ui.components.StreamingAiMessageItem
import com.example.ui.theme.BrizBgLight
import com.example.ui.viewmodel.BrizChatViewModel
import kotlinx.coroutines.launch

@Composable
fun MainChatScreen(
  viewModel: BrizChatViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

  val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
  val currentSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()
  val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
  val messages by viewModel.messages.collectAsStateWithLifecycle()
  val streamingText by viewModel.streamingText.collectAsStateWithLifecycle()
  val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
  val activePersona by viewModel.activePersona.collectAsStateWithLifecycle()
  val activeModel by viewModel.activeModel.collectAsStateWithLifecycle()
  val temperature by viewModel.temperature.collectAsStateWithLifecycle()
  val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()
  val attachedFile by viewModel.attachedFile.collectAsStateWithLifecycle()
  val speakingMessageId by viewModel.speakingMessageId.collectAsStateWithLifecycle()

  var inputText by remember { mutableStateOf("") }
  var searchQuery by remember { mutableStateOf("") }
  var showSettingsSheet by remember { mutableStateOf(false) }

  val listState = rememberLazyListState()

  // Filter messages if search query is active
  val displayedMessages = remember(messages, searchQuery) {
    if (searchQuery.isBlank()) messages
    else messages.filter {
      it.content.contains(searchQuery, ignoreCase = true) ||
        (it.fileName?.contains(searchQuery, ignoreCase = true) == true)
    }
  }

  // Auto-scroll to bottom when new message arrives or streaming updates
  LaunchedEffect(messages.size, streamingText) {
    val totalCount = displayedMessages.size + if (streamingText != null) 1 else 0
    if (totalCount > 0 && searchQuery.isBlank()) {
      listState.animateScrollToItem(totalCount - 1)
    }
  }

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      SessionsDrawerContent(
        sessions = sessions,
        currentSessionId = currentSessionId,
        onSelectSession = { id ->
          viewModel.selectSession(id)
          scope.launch { drawerState.close() }
        },
        onNewChat = {
          viewModel.createNewSession()
          scope.launch { drawerState.close() }
        },
        onRenameSession = { id, title ->
          viewModel.renameSession(id, title)
        },
        onDeleteSession = { id ->
          viewModel.deleteSession(id)
        },
        onTogglePinSession = { id ->
          viewModel.togglePinSession(id)
        },
        onClearAll = {
          viewModel.clearAllHistory()
          scope.launch { drawerState.close() }
        }
      )
    }
  ) {
    Scaffold(
      modifier = modifier
        .fillMaxSize()
        .background(BrizBgLight)
        .statusBarsPadding()
        .navigationBarsPadding()
        .imePadding(),
      contentWindowInsets = WindowInsets.safeDrawing,
      topBar = {
        BrizTopAppBar(
          sessionTitle = currentSession?.title,
          activePersona = activePersona,
          activeModel = activeModel,
          searchQuery = searchQuery,
          onSearchQueryChanged = { searchQuery = it },
          onModelSelected = { model ->
            viewModel.setModel(model)
          },
          onOpenDrawer = {
            scope.launch { drawerState.open() }
          },
          onNewChat = {
            viewModel.createNewSession()
          },
          onOpenSettings = {
            showSettingsSheet = true
          },
          onExportChat = {
            scope.launch {
              val markdown = viewModel.exportCurrentChat()
              if (markdown.isNotBlank()) {
                val sendIntent = Intent().apply {
                  action = Intent.ACTION_SEND
                  putExtra(Intent.EXTRA_TEXT, markdown)
                  type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Ekspor Percakapan Briz (.md)")
                context.startActivity(shareIntent)
              } else {
                Toast.makeText(context, "Percakapan masih kosong", Toast.LENGTH_SHORT).show()
              }
            }
          }
        )
      },
      bottomBar = {
        ChatInputBar(
          inputText = inputText,
          onInputTextChanged = { inputText = it },
          onSendMessage = {
            val textToSend = inputText
            inputText = ""
            viewModel.sendMessage(textToSend)
          },
          onStopGeneration = {
            viewModel.stopGeneration()
          },
          isGenerating = isGenerating,
          attachedFile = attachedFile,
          onAttachFileUri = { uri ->
            viewModel.attachFileFromUri(uri)
          },
          onAttachBitmap = { bitmap ->
            viewModel.attachBitmap(bitmap)
          },
          onClearAttachedFile = {
            viewModel.clearAttachedFile()
          }
        )
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(BrizBgLight)
          .padding(innerPadding)
      ) {
        if (displayedMessages.isEmpty() && streamingText == null) {
          HeroEmptyState(
            persona = activePersona,
            modelName = activeModel,
            onSelectPrompt = { prompt ->
              inputText = prompt
            },
            modifier = Modifier.fillMaxSize()
          )
        } else {
          LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
          ) {
            items(displayedMessages, key = { it.id }) { message ->
              ChatMessageItem(
                message = message,
                isSpeaking = speakingMessageId == message.id,
                onSpeakMessage = { viewModel.speakMessage(message.id, message.content) },
                onStopSpeaking = { viewModel.stopSpeaking() },
                onRegenerate = { viewModel.regenerateResponse() },
                onTogglePin = { viewModel.togglePinMessage(message.id) },
                onDeleteMessage = { viewModel.deleteMessage(message.id) },
                onCodeAction = { action, codeSnippet ->
                  inputText = "Tolong $action potongan kode ini:\n\n```\n$codeSnippet\n```"
                }
              )
            }

            if (streamingText != null && searchQuery.isBlank()) {
              item(key = "streaming_item") {
                StreamingAiMessageItem(
                  streamingText = streamingText ?: ""
                )
              }
            }
          }
        }
      }
    }
  }

  // Settings Bottom Sheet
  if (showSettingsSheet) {
    SettingsBottomSheet(
      currentPersona = activePersona,
      currentModel = activeModel,
      currentTemperature = temperature,
      customApiKey = customApiKey,
      onPersonaSelected = { persona ->
        viewModel.setPersona(persona)
      },
      onModelSelected = { model ->
        viewModel.setModel(model)
      },
      onTemperatureChanged = { temp ->
        viewModel.setTemperature(temp)
      },
      onCustomApiKeyChanged = { key ->
        viewModel.setCustomApiKey(key)
      },
      onDismiss = { showSettingsSheet = false }
    )
  }
}
