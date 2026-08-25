package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttachedFile
import com.example.ui.theme.BrizAccentPurple
import com.example.ui.theme.BrizBgLight
import com.example.ui.theme.BrizBlueLight
import com.example.ui.theme.BrizPillBorder
import com.example.ui.theme.BrizPillLight
import com.example.ui.theme.BrizPrimary
import com.example.ui.theme.BrizPrimaryDark
import com.example.ui.theme.BrizSparkGradient
import com.example.ui.theme.BrizTextPrimary
import com.example.ui.theme.BrizTextSecondary
import com.example.ui.theme.BrizTextTertiary
import java.util.Locale

@Composable
fun ChatInputBar(
  inputText: String,
  onInputTextChanged: (String) -> Unit,
  onSendMessage: () -> Unit,
  onStopGeneration: () -> Unit,
  isGenerating: Boolean,
  attachedFile: AttachedFile?,
  onAttachFileUri: (Uri) -> Unit,
  onAttachBitmap: (android.graphics.Bitmap) -> Unit,
  onClearAttachedFile: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showAttachMenu by remember { mutableStateOf(false) }

  // Universal Document/File Picker
  val anyFileLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent(),
    onResult = { uri ->
      if (uri != null) {
        onAttachFileUri(uri)
      }
    }
  )

  // Camera Picker
  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview(),
    onResult = { bitmap ->
      if (bitmap != null) {
        onAttachBitmap(bitmap)
      }
    }
  )

  // Voice Recognition Speech-to-Text Launcher
  val speechLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult(),
    onResult = { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!spokenText.isNullOrBlank()) {
          val newText = if (inputText.isBlank()) spokenText else "$inputText $spokenText"
          onInputTextChanged(newText)
        }
      }
    }
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(BrizBgLight)
      .padding(horizontal = 16.dp, vertical = 6.dp)
  ) {
    // Attached File Preview Pill (Image / PDF / Audio / Code / Document)
    AnimatedVisibility(
      visible = attachedFile != null,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      if (attachedFile != null) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BrizBlueLight)
            .border(1.dp, BrizPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          // File Icon or Image Thumbnail
          if (attachedFile.previewBitmap != null) {
            Image(
              bitmap = attachedFile.previewBitmap.asImageBitmap(),
              contentDescription = "Preview Gambar",
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp)),
              contentScale = ContentScale.Crop
            )
          } else {
            val icon = when (attachedFile.fileType) {
              "pdf" -> Icons.Default.PictureAsPdf
              "audio" -> Icons.Default.Audiotrack
              "code" -> Icons.Default.Code
              "image" -> Icons.Default.Image
              else -> Icons.Default.Description
            }
            val iconTint = when (attachedFile.fileType) {
              "pdf" -> Color(0xFFD93025)
              "audio" -> Color(0xFFE37400)
              "code" -> Color(0xFF1B6EF3)
              else -> BrizAccentPurple
            }

            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = icon,
                contentDescription = attachedFile.fileType,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column(modifier = Modifier.weight(1f, fill = false)) {
            Text(
              text = attachedFile.name,
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
              color = BrizTextPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "${attachedFile.fileType.uppercase(Locale.ROOT)} • ${attachedFile.sizeFormatted}",
              style = MaterialTheme.typography.labelSmall,
              color = BrizTextSecondary
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(BrizTextSecondary.copy(alpha = 0.15f))
              .clickable { onClearAttachedFile() },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Hapus lampiran",
              tint = BrizTextPrimary,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }
    }

    // Gemini Capsule Input Container
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(28.dp))
        .background(BrizPillLight)
        .border(1.dp, BrizPillBorder, RoundedCornerShape(28.dp))
        .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        // Universal Attach Button (+)
        Box {
          IconButton(
            onClick = { showAttachMenu = true },
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .testTag("attach_file_button")
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Lampirkan Semua File",
              tint = BrizPrimary,
              modifier = Modifier.size(24.dp)
            )
          }

          DropdownMenu(
            expanded = showAttachMenu,
            onDismissRequest = { showAttachMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("Galeri Foto") },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Image,
                  contentDescription = null,
                  tint = BrizPrimary
                )
              },
              onClick = {
                showAttachMenu = false
                anyFileLauncher.launch("image/*")
              }
            )
            DropdownMenuItem(
              text = { Text("Kamera") },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.CameraAlt,
                  contentDescription = null,
                  tint = Color(0xFF26A69A)
                )
              },
              onClick = {
                showAttachMenu = false
                cameraLauncher.launch(null)
              }
            )
            DropdownMenuItem(
              text = { Text("Dokumen & File") },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.AttachFile,
                  contentDescription = null,
                  tint = BrizTextSecondary
                )
              },
              onClick = {
                showAttachMenu = false
                anyFileLauncher.launch("*/*")
              }
            )
          }
        }

        // Text Field Input
        Box(
          modifier = Modifier
            .weight(1f)
            .padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
          if (inputText.isEmpty() && attachedFile == null) {
            Text(
              text = "Tanya Briz apa saja...",
              style = MaterialTheme.typography.bodyMedium,
              color = BrizTextTertiary,
              fontSize = 15.sp
            )
          }

          BasicTextField(
            value = inputText,
            onValueChange = onInputTextChanged,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("chat_input_field"),
            textStyle = TextStyle(
              color = BrizTextPrimary,
              fontSize = 15.sp,
              lineHeight = 22.sp,
              fontFamily = FontFamily.SansSerif
            ),
            cursorBrush = SolidColor(BrizPrimary),
            maxLines = 6
          )
        }

        // Voice Input Mic Button (if input empty)
        if (inputText.isEmpty() && !isGenerating && attachedFile == null) {
          IconButton(
            onClick = {
              val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Bicara sekarang dengan Briz...")
              }
              try {
                speechLauncher.launch(intent)
              } catch (e: Exception) {
                e.printStackTrace()
              }
            },
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .testTag("voice_input_button")
          ) {
            Icon(
              imageVector = Icons.Default.Mic,
              contentDescription = "Input Suara",
              tint = BrizTextSecondary,
              modifier = Modifier.size(22.dp)
            )
          }
        }

        // Send or Stop Generation Button
        if (isGenerating) {
          IconButton(
            onClick = onStopGeneration,
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(BrizPrimaryDark)
              .testTag("stop_generation_button")
          ) {
            Icon(
              imageVector = Icons.Default.Stop,
              contentDescription = "Hentikan Generasi",
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
          }
        } else {
          val canSend = inputText.isNotBlank() || attachedFile != null
          IconButton(
            onClick = {
              if (canSend) {
                onSendMessage()
              }
            },
            enabled = canSend,
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(
                if (canSend) BrizPrimary else Color.Transparent
              )
              .testTag("send_message_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Kirim Pesan",
              tint = if (canSend) Color.White else BrizTextTertiary.copy(alpha = 0.4f),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}
