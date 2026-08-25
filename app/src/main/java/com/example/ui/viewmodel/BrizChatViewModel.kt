package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Base64
import android.webkit.MimeTypeMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiService
import com.example.data.local.AppDatabase
import com.example.data.model.AttachedFile
import com.example.data.model.BrizPersona
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChatSessionEntity
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Locale

class BrizChatViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application)
  private val geminiService = GeminiService()
  private val repository = ChatRepository(database.chatDao(), geminiService)

  // TTS Engine
  private var textToSpeech: TextToSpeech? = null
  private var isTtsReady = false
  private val _speakingMessageId = MutableStateFlow<Long?>(null)
  val speakingMessageId: StateFlow<Long?> = _speakingMessageId.asStateFlow()

  val allSessions: StateFlow<List<ChatSessionEntity>> = repository.allSessions
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  private val _currentSessionId = MutableStateFlow<String?>(null)
  val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

  @OptIn(ExperimentalCoroutinesApi::class)
  val currentSession: StateFlow<ChatSessionEntity?> = _currentSessionId.flatMapLatest { id ->
    if (id == null) flowOf(null) else repository.getSession(id)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = null
  )

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  @OptIn(ExperimentalCoroutinesApi::class)
  private val rawMessages: StateFlow<List<ChatMessageEntity>> = _currentSessionId.flatMapLatest { id ->
    if (id == null) flowOf(emptyList()) else repository.getMessages(id)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  val messages: StateFlow<List<ChatMessageEntity>> = combine(rawMessages, _searchQuery) { msgList, query ->
    if (query.isBlank()) {
      msgList
    } else {
      msgList.filter { it.content.contains(query, ignoreCase = true) || (it.fileName?.contains(query, ignoreCase = true) == true) }
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  private val _streamingText = MutableStateFlow<String?>(null)
  val streamingText: StateFlow<String?> = _streamingText.asStateFlow()

  private val _isGenerating = MutableStateFlow(false)
  val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

  private val _activePersona = MutableStateFlow(BrizPersona.STANDARD)
  val activePersona: StateFlow<BrizPersona> = _activePersona.asStateFlow()

  private val _activeModel = MutableStateFlow("gemini-3.5-flash")
  val activeModel: StateFlow<String> = _activeModel.asStateFlow()

  private val _temperature = MutableStateFlow(0.7f)
  val temperature: StateFlow<Float> = _temperature.asStateFlow()

  private val _topP = MutableStateFlow(0.95f)
  val topP: StateFlow<Float> = _topP.asStateFlow()

  private val _customApiKey = MutableStateFlow("")
  val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

  // Universal Attached File
  private val _attachedFile = MutableStateFlow<AttachedFile?>(null)
  val attachedFile: StateFlow<AttachedFile?> = _attachedFile.asStateFlow()

  private var activeGenerationJob: Job? = null

  init {
    initTTS(application)
    viewModelScope.launch {
      allSessions.collect { sessions ->
        if (_currentSessionId.value == null && sessions.isNotEmpty()) {
          _currentSessionId.value = sessions.first().id
          _activePersona.value = BrizPersona.fromId(sessions.first().personaId)
          _activeModel.value = sessions.first().modelName
        }
      }
    }
  }

  private fun initTTS(context: Context) {
    textToSpeech = TextToSpeech(context) { status ->
      if (status == TextToSpeech.SUCCESS) {
        isTtsReady = true
        val result = textToSpeech?.setLanguage(Locale("id", "ID"))
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
          textToSpeech?.setLanguage(Locale.US)
        }
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
          override fun onStart(utteranceId: String?) {}
          override fun onDone(utteranceId: String?) {
            _speakingMessageId.value = null
          }
          override fun onError(utteranceId: String?) {
            _speakingMessageId.value = null
          }
        })
      }
    }
  }

  fun speakMessage(messageId: Long, rawContent: String) {
    if (!isTtsReady || textToSpeech == null) return

    if (_speakingMessageId.value == messageId) {
      stopSpeaking()
      return
    }

    stopSpeaking()
    _speakingMessageId.value = messageId

    // Clean markdown symbols for natural audio speech
    val cleanedText = rawContent
      .replace(Regex("```[a-zA-Z]*"), "")
      .replace("```", "")
      .replace(Regex("[*#_`~]"), "")
      .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1")
      .trim()

    textToSpeech?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, messageId.toString())
  }

  fun stopSpeaking() {
    textToSpeech?.stop()
    _speakingMessageId.value = null
  }

  fun selectSession(sessionId: String) {
    stopSpeaking()
    _currentSessionId.value = sessionId
    viewModelScope.launch {
      val session = allSessions.value.find { it.id == sessionId }
      if (session != null) {
        _activePersona.value = BrizPersona.fromId(session.personaId)
        _activeModel.value = session.modelName
      }
    }
  }

  fun createNewSession(
    initialTitle: String = "Chat Baru",
    persona: BrizPersona = _activePersona.value,
    model: String = _activeModel.value
  ) {
    stopSpeaking()
    viewModelScope.launch {
      val newId = repository.createNewSession(
        title = initialTitle,
        personaId = persona.id,
        modelName = model
      )
      _currentSessionId.value = newId
      _activePersona.value = persona
      _activeModel.value = model
      _searchQuery.value = ""
    }
  }

  fun deleteSession(sessionId: String) {
    viewModelScope.launch {
      if (sessionId == _currentSessionId.value) {
        stopGeneration()
        stopSpeaking()
        _currentSessionId.value = null
      }
      repository.deleteSession(sessionId)
      val remaining = allSessions.value.filter { it.id != sessionId }
      if (remaining.isNotEmpty()) {
        selectSession(remaining.first().id)
      }
    }
  }

  fun renameSession(sessionId: String, newTitle: String) {
    viewModelScope.launch {
      repository.updateSessionTitle(sessionId, newTitle.trim())
    }
  }

  fun togglePinSession(sessionId: String) {
    viewModelScope.launch {
      repository.togglePinSession(sessionId)
    }
  }

  fun togglePinMessage(messageId: Long) {
    viewModelScope.launch {
      repository.togglePinMessage(messageId)
    }
  }

  fun clearAllHistory() {
    viewModelScope.launch {
      stopGeneration()
      stopSpeaking()
      repository.clearAllHistory()
      _currentSessionId.value = null
    }
  }

  fun setPersona(persona: BrizPersona) {
    _activePersona.value = persona
    val currentId = _currentSessionId.value
    if (currentId != null) {
      viewModelScope.launch {
        repository.updateSessionSettings(currentId, persona.id, _activeModel.value)
      }
    }
  }

  fun setModel(modelName: String) {
    _activeModel.value = modelName
    val currentId = _currentSessionId.value
    if (currentId != null) {
      viewModelScope.launch {
        repository.updateSessionSettings(currentId, _activePersona.value.id, modelName)
      }
    }
  }

  fun setTemperature(temp: Float) {
    _temperature.value = temp
  }

  fun setTopP(p: Float) {
    _topP.value = p
  }

  fun setCustomApiKey(key: String) {
    _customApiKey.value = key.trim()
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  /**
   * Universally attaches ANY file (Image, Audio, PDF, Code, Document, Plaintext)
   */
  fun attachFileFromUri(uri: Uri) {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val context = getApplication<Application>()
        val contentResolver = context.contentResolver

        // Resolve name and size
        var fileName = "file"
        var fileSize = 0L
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
          val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
          if (cursor.moveToFirst()) {
            if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: "file"
            if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
          }
        }

        val mimeType = contentResolver.getType(uri) ?: getMimeTypeFromExtension(fileName)
        val sizeFormatted = formatFileSize(fileSize)

        // Classify file type
        val fileType = when {
          mimeType.startsWith("image/") -> "image"
          mimeType == "application/pdf" -> "pdf"
          mimeType.startsWith("audio/") -> "audio"
          isCodeOrTextMime(mimeType, fileName) -> "code"
          else -> "document"
        }

        var previewBitmap: Bitmap? = null
        var base64Data: String? = null
        var textContent: String? = null

        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        if (inputStream != null) {
          if (fileType == "image") {
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (originalBitmap != null) {
              val maxDimension = 1024
              val width = originalBitmap.width
              val height = originalBitmap.height
              val scaled = if (width > maxDimension || height > maxDimension) {
                val scale = maxDimension.toFloat() / maxOf(width, height)
                Bitmap.createScaledBitmap(originalBitmap, (width * scale).toInt(), (height * scale).toInt(), true)
              } else {
                originalBitmap
              }
              val bos = ByteArrayOutputStream()
              scaled.compress(Bitmap.CompressFormat.JPEG, 85, bos)
              base64Data = Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP)
              previewBitmap = scaled
            }
          } else if (fileType == "code" || mimeType.startsWith("text/")) {
            val bytes = inputStream.readBytes()
            inputStream.close()
            textContent = String(bytes, Charsets.UTF_8)
          } else {
            // PDF, Audio, binary
            val bytes = inputStream.readBytes()
            inputStream.close()
            base64Data = Base64.encodeToString(bytes, Base64.NO_WRAP)
          }
        }

        val attached = AttachedFile(
          uri = uri,
          name = fileName,
          mimeType = mimeType,
          sizeFormatted = sizeFormatted,
          fileType = fileType,
          base64Data = base64Data,
          textContent = textContent,
          previewBitmap = previewBitmap
        )

        withContext(Dispatchers.Main) {
          _attachedFile.value = attached
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun attachBitmap(bitmap: Bitmap) {
    viewModelScope.launch(Dispatchers.Default) {
      try {
        val maxDimension = 1024
        val width = bitmap.width
        val height = bitmap.height
        val scaled = if (width > maxDimension || height > maxDimension) {
          val scale = maxDimension.toFloat() / maxOf(width, height)
          Bitmap.createScaledBitmap(bitmap, (width * scale).toInt(), (height * scale).toInt(), true)
        } else {
          bitmap
        }
        val bos = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, bos)
        val base64Data = Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP)
        
        val sizeFormatted = formatFileSize(bos.size().toLong())

        val attached = AttachedFile(
          uri = null,
          name = "Kamera_${System.currentTimeMillis()}.jpg",
          mimeType = "image/jpeg",
          sizeFormatted = sizeFormatted,
          fileType = "image",
          base64Data = base64Data,
          textContent = null,
          previewBitmap = scaled
        )

        withContext(Dispatchers.Main) {
          _attachedFile.value = attached
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun clearAttachedFile() {
    _attachedFile.value = null
  }

  fun deleteMessage(messageId: Long) {
    if (_speakingMessageId.value == messageId) {
      stopSpeaking()
    }
    viewModelScope.launch {
      repository.deleteMessage(messageId)
    }
  }

  fun sendMessage(promptText: String) {
    val trimmed = promptText.trim()
    val attached = _attachedFile.value
    if (trimmed.isEmpty() && attached == null) return
    if (_isGenerating.value) return

    clearAttachedFile()

    viewModelScope.launch {
      var sessionId = _currentSessionId.value
      if (sessionId == null) {
        val titleSnippet = if (trimmed.length > 25) trimmed.take(25) + "..." else trimmed
        sessionId = repository.createNewSession(
          title = titleSnippet.ifBlank { attached?.name ?: "Analisis File" },
          personaId = _activePersona.value.id,
          modelName = _activeModel.value
        )
        _currentSessionId.value = sessionId
      }

      _isGenerating.value = true
      _streamingText.value = ""

      activeGenerationJob = repository.sendMessageStreaming(
        sessionId = sessionId,
        prompt = trimmed,
        attachedFile = attached,
        persona = _activePersona.value,
        modelName = _activeModel.value,
        temperature = _temperature.value,
        topP = _topP.value,
        customApiKey = _customApiKey.value,
        scope = viewModelScope,
        onChunk = { currentText ->
          _streamingText.value = currentText
        },
        onComplete = {
          _isGenerating.value = false
          _streamingText.value = null
        },
        onError = { _ ->
          _isGenerating.value = false
          _streamingText.value = null
        }
      )
    }
  }

  fun regenerateResponse() {
    val sessionId = _currentSessionId.value ?: return
    if (_isGenerating.value) return

    _isGenerating.value = true
    _streamingText.value = ""

    activeGenerationJob = repository.regenerateResponse(
      sessionId = sessionId,
      persona = _activePersona.value,
      modelName = _activeModel.value,
      temperature = _temperature.value,
      topP = _topP.value,
      customApiKey = _customApiKey.value,
      scope = viewModelScope,
      onChunk = { currentText ->
        _streamingText.value = currentText
      },
      onComplete = {
        _isGenerating.value = false
        _streamingText.value = null
      },
      onError = { _ ->
        _isGenerating.value = false
        _streamingText.value = null
      }
    )
  }

  fun stopGeneration() {
    activeGenerationJob?.cancel()
    activeGenerationJob = null
    _isGenerating.value = false
    _streamingText.value = null
  }

  suspend fun exportCurrentChat(): String {
    val currentId = _currentSessionId.value ?: return ""
    return repository.exportSessionAsMarkdown(currentId)
  }

  private fun getMimeTypeFromExtension(name: String): String {
    val ext = name.substringAfterLast('.', "").lowercase()
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: when (ext) {
      "kt", "kts" -> "text/x-kotlin"
      "py" -> "text/x-python"
      "js", "ts", "jsx", "tsx" -> "text/javascript"
      "json" -> "application/json"
      "md" -> "text/markdown"
      "xml", "html", "htm" -> "text/html"
      "csv" -> "text/csv"
      "txt", "log" -> "text/plain"
      "pdf" -> "application/pdf"
      "mp3" -> "audio/mp3"
      "wav" -> "audio/wav"
      else -> "application/octet-stream"
    }
  }

  private fun isCodeOrTextMime(mime: String, name: String): Boolean {
    val ext = name.substringAfterLast('.', "").lowercase()
    val textExts = listOf("kt", "java", "py", "js", "ts", "jsx", "tsx", "html", "css", "xml", "json", "md", "txt", "csv", "sql", "sh", "yml", "yaml", "toml", "gradle", "c", "cpp", "h", "cs", "go", "rs", "rb", "php", "swift")
    return mime.startsWith("text/") || ext in textExts
  }

  private fun formatFileSize(bytes: Long): String {
    return when {
      bytes < 1024 -> "$bytes B"
      bytes < 1024 * 1024 -> "${bytes / 1024} KB"
      else -> String.format(Locale.US, "%.1f MB", bytes.toDouble() / (1024 * 1024))
    }
  }

  override fun onCleared() {
    super.onCleared()
    textToSpeech?.stop()
    textToSpeech?.shutdown()
  }
}
