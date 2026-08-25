package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatSessionEntity
import com.example.ui.theme.BrizBgLight
import com.example.ui.theme.BrizBlueLight
import com.example.ui.theme.BrizPillBorder
import com.example.ui.theme.BrizPillHover
import com.example.ui.theme.BrizPillLight
import com.example.ui.theme.BrizPrimary
import com.example.ui.theme.BrizSparkGradient
import com.example.ui.theme.BrizTextPrimary
import com.example.ui.theme.BrizTextSecondary
import com.example.ui.theme.BrizTextTertiary

@Composable
fun SessionsDrawerContent(
  sessions: List<ChatSessionEntity>,
  currentSessionId: String?,
  onSelectSession: (String) -> Unit,
  onNewChat: () -> Unit,
  onRenameSession: (String, String) -> Unit,
  onDeleteSession: (String) -> Unit,
  onTogglePinSession: (String) -> Unit,
  onClearAll: () -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var sessionToRename by remember { mutableStateOf<ChatSessionEntity?>(null) }
  var renameText by remember { mutableStateOf("") }
  var showClearAllConfirm by remember { mutableStateOf(false) }

  val filteredSessions = remember(sessions, searchQuery) {
    if (searchQuery.isBlank()) sessions
    else sessions.filter { it.title.contains(searchQuery, ignoreCase = true) }
  }

  val pinnedSessions = remember(filteredSessions) { filteredSessions.filter { it.isPinned } }
  val recentSessions = remember(filteredSessions) { filteredSessions.filter { !it.isPinned } }

  Column(
    modifier = modifier
      .fillMaxHeight()
      .width(320.dp)
      .background(BrizBgLight)
      .padding(horizontal = 16.dp, vertical = 20.dp)
  ) {
    // Header Row with Briz Sparkle
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
    ) {
      BrizSparkleIcon(
        size = 24.dp,
        brush = BrizSparkGradient
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = "Briz",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        color = BrizTextPrimary
      )
    }

    // New Chat Button (Pill with + icon)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .background(BrizBlueLight)
        .border(1.dp, BrizPrimary.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
        .clickable { onNewChat() }
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .testTag("drawer_new_chat_button")
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Obrolan Baru",
          tint = BrizPrimary,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = "Obrolan Baru",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          fontSize = 14.sp,
          color = BrizPrimary
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Search input
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .background(BrizPillLight)
        .border(1.dp, BrizPillBorder, RoundedCornerShape(20.dp))
        .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "Cari",
          tint = BrizTextTertiary,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (searchQuery.isEmpty()) {
          Text(
            text = "Cari riwayat percakapan...",
            style = MaterialTheme.typography.bodyMedium,
            color = BrizTextTertiary,
            fontSize = 13.sp
          )
        }
        BasicTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("drawer_search_field"),
          textStyle = TextStyle(
            color = BrizTextPrimary,
            fontSize = 13.sp,
            fontFamily = FontFamily.SansSerif
          ),
          cursorBrush = SolidColor(BrizPrimary),
          singleLine = true
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Sessions List
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      // Pinned section
      if (pinnedSessions.isNotEmpty()) {
        item {
          Text(
            text = "DISEMATKAN",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = BrizPrimary,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
          )
        }
        items(pinnedSessions, key = { "pinned_${it.id}" }) { session ->
          DrawerSessionRow(
            session = session,
            isSelected = session.id == currentSessionId,
            onSelect = { onSelectSession(session.id) },
            onRename = {
              sessionToRename = session
              renameText = session.title
            },
            onDelete = { onDeleteSession(session.id) },
            onTogglePin = { onTogglePinSession(session.id) }
          )
        }
      }

      // Recent section
      if (recentSessions.isNotEmpty()) {
        item {
          Text(
            text = "TERBARU",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = BrizTextTertiary,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
          )
        }
        items(recentSessions, key = { it.id }) { session ->
          DrawerSessionRow(
            session = session,
            isSelected = session.id == currentSessionId,
            onSelect = { onSelectSession(session.id) },
            onRename = {
              sessionToRename = session
              renameText = session.title
            },
            onDelete = { onDeleteSession(session.id) },
            onTogglePin = { onTogglePinSession(session.id) }
          )
        }
      }

      if (filteredSessions.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (searchQuery.isNotEmpty()) "Tidak ada hasil pencarian" else "Belum ada riwayat percakapan",
              style = MaterialTheme.typography.bodySmall,
              color = BrizTextTertiary
            )
          }
        }
      }
    }

    HorizontalDivider(
      color = BrizPillBorder,
      modifier = Modifier.padding(vertical = 8.dp)
    )

    // Clear all history action
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .clickable { showClearAllConfirm = true }
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.ClearAll,
        contentDescription = "Bersihkan Riwayat",
        tint = BrizTextTertiary,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Text(
        text = "Hapus Semua Riwayat",
        style = MaterialTheme.typography.bodySmall,
        color = BrizTextSecondary
      )
    }
  }

  // Rename Dialog
  if (sessionToRename != null) {
    AlertDialog(
      onDismissRequest = { sessionToRename = null },
      containerColor = Color.White,
      title = {
        Text(
          text = "Ubah Nama Percakapan",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = BrizTextPrimary
        )
      },
      text = {
        OutlinedTextField(
          value = renameText,
          onValueChange = { renameText = it },
          label = { Text("Judul Obrolan") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BrizPrimary,
            unfocusedBorderColor = BrizPillBorder
          ),
          singleLine = true
        )
      },
      confirmButton = {
        TextButton(
          onClick = {
            val target = sessionToRename
            if (target != null && renameText.isNotBlank()) {
              onRenameSession(target.id, renameText.trim())
            }
            sessionToRename = null
          }
        ) {
          Text("Simpan", color = BrizPrimary, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { sessionToRename = null }) {
          Text("Batal", color = BrizTextTertiary)
        }
      }
    )
  }

  // Clear All Confirm Dialog
  if (showClearAllConfirm) {
    AlertDialog(
      onDismissRequest = { showClearAllConfirm = false },
      containerColor = Color.White,
      title = {
        Text(
          text = "Hapus Semua Riwayat?",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = BrizTextPrimary
        )
      },
      text = {
        Text(
          text = "Semua riwayat percakapan yang tersimpan akan dihapus secara permanen.",
          style = MaterialTheme.typography.bodyMedium,
          color = BrizTextSecondary
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showClearAllConfirm = false
            onClearAll()
          },
          colors = ButtonDefaults.buttonColors(containerColor = BrizPrimary)
        ) {
          Text("Hapus", color = Color.White)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearAllConfirm = false }) {
          Text("Batal", color = BrizTextTertiary)
        }
      }
    )
  }
}

@Composable
private fun DrawerSessionRow(
  session: ChatSessionEntity,
  isSelected: Boolean,
  onSelect: () -> Unit,
  onRename: () -> Unit,
  onDelete: () -> Unit,
  onTogglePin: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .background(if (isSelected) BrizBlueLight else Color.Transparent)
      .clickable { onSelect() }
      .padding(horizontal = 12.dp, vertical = 10.dp)
      .testTag("session_item_${session.id}")
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Icon(
          imageVector = Icons.Default.ChatBubbleOutline,
          contentDescription = null,
          tint = if (isSelected) BrizPrimary else BrizTextTertiary,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = session.title,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
          color = if (isSelected) BrizPrimary else BrizTextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      // Quick action icons
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onTogglePin,
          modifier = Modifier.size(24.dp)
        ) {
          Icon(
            imageVector = if (session.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
            contentDescription = "Pin Obrolan",
            tint = if (session.isPinned) BrizPrimary else BrizTextTertiary,
            modifier = Modifier.size(14.dp)
          )
        }

        IconButton(
          onClick = onRename,
          modifier = Modifier.size(24.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Ubah Nama",
            tint = BrizTextTertiary,
            modifier = Modifier.size(14.dp)
          )
        }

        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(24.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Hapus Obrolan",
            tint = BrizTextTertiary,
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }
  }
}
