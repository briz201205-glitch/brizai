package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessageEntity
import com.example.ui.theme.BrizAccentPurple
import com.example.ui.theme.BrizBlueLight
import com.example.ui.theme.BrizPillBorder
import com.example.ui.theme.BrizPrimary
import com.example.ui.theme.BrizPrimaryDark
import com.example.ui.theme.BrizSparkGradient
import com.example.ui.theme.BrizTextPrimary
import com.example.ui.theme.BrizTextSecondary
import com.example.ui.theme.BrizTextTertiary
import com.example.ui.theme.BrizUserBubble
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageItem(
  message: ChatMessageEntity,
  isSpeaking: Boolean = false,
  onSpeakMessage: () -> Unit = {},
  onStopSpeaking: () -> Unit = {},
  onRegenerate: () -> Unit = {},
  onTogglePin: () -> Unit = {},
  onDeleteMessage: () -> Unit = {},
  onCodeAction: ((action: String, code: String) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val isUser = message.role == "user"
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var copied by remember { mutableStateOf(false) }

  val formattedTime = remember(message.timestamp) {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    sdf.format(Date(message.timestamp))
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    if (isUser) {
      // User Message Layout (Gemini pill bubble on the right)
      Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.fillMaxWidth(0.9f)
      ) {
        // Universal Attached File Render
        if (message.fileName != null || !message.imageBase64.isNullOrEmpty()) {
          val bitmap = remember(message.imageBase64) {
            if (!message.imageBase64.isNullOrEmpty() && (message.fileType == "image" || message.fileMimeType?.startsWith("image/") == true || message.fileType == null)) {
              try {
                val decodedBytes = Base64.decode(message.imageBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
              } catch (e: Exception) {
                null
              }
            } else null
          }

          if (bitmap != null) {
            Box(
              modifier = Modifier
                .padding(bottom = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, BrizPillBorder, RoundedCornerShape(16.dp))
            ) {
              Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Gambar Pengguna",
                modifier = Modifier
                  .size(190.dp)
                  .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
              )
            }
          } else if (message.fileName != null) {
            // File badge chip
            val icon = when (message.fileType) {
              "pdf" -> Icons.Default.PictureAsPdf
              "audio" -> Icons.Default.Audiotrack
              "code" -> Icons.Default.Code
              "image" -> Icons.Default.Image
              else -> Icons.Default.Description
            }
            val iconTint = when (message.fileType) {
              "pdf" -> Color(0xFFD93025)
              "audio" -> Color(0xFFE37400)
              "code" -> BrizPrimary
              else -> BrizAccentPurple
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .padding(bottom = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BrizBlueLight)
                .border(1.dp, BrizPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = message.fileName,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = BrizTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              if (message.fileSizeFormatted != null) {
                Text(
                  text = " (${message.fileSizeFormatted})",
                  style = MaterialTheme.typography.labelSmall,
                  color = BrizTextSecondary
                )
              }
            }
          }
        }

        // Text bubble
        if (message.content.isNotBlank()) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(BrizUserBubble)
              .border(1.dp, BrizPillBorder, RoundedCornerShape(20.dp))
              .padding(horizontal = 16.dp, vertical = 12.dp)
          ) {
            BrizMarkdownView(
              content = message.content,
              isUser = true
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(top = 4.dp, end = 4.dp)
        ) {
          if (message.isPinned) {
            Icon(
              imageVector = Icons.Default.PushPin,
              contentDescription = "Pesan Disematkan",
              tint = BrizPrimary,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
          }
          Text(
            text = formattedTime,
            fontSize = 11.sp,
            color = BrizTextTertiary
          )
        }
      }
    } else {
      // Briz AI Message Layout (Gemini style)
      Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
      ) {
        // AI Header with 4-point Sparkle Icon (with vibrant blue gradient)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(bottom = 6.dp)
        ) {
          BrizSparkleIcon(
            size = 20.dp,
            brush = BrizSparkGradient
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Briz",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = BrizTextPrimary
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = formattedTime,
            fontSize = 11.sp,
            color = BrizTextTertiary
          )

          if (message.isPinned) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = Icons.Default.PushPin,
              contentDescription = "Disematkan",
              tint = BrizPrimary,
              modifier = Modifier.size(13.dp)
            )
          }
        }

        // Response Body
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 28.dp)
        ) {
          BrizMarkdownView(
            content = message.content,
            isUser = false,
            onCodeAction = onCodeAction
          )
        }

        // Action Toolbar (TTS Speaker, Copy, Share, Regenerate, Pin, Delete)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.padding(start = 28.dp, top = 6.dp)
        ) {
          // TTS Audio Read Aloud
          IconButton(
            onClick = {
              if (isSpeaking) onStopSpeaking() else onSpeakMessage()
            },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
              contentDescription = if (isSpeaking) "Hentikan Suara" else "Dengarkan Jawaban",
              tint = if (isSpeaking) BrizPrimary else BrizTextSecondary,
              modifier = Modifier.size(17.dp)
            )
          }

          // Copy Message
          IconButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              val clip = ClipData.newPlainText("Briz Response", message.content)
              clipboard.setPrimaryClip(clip)
              copied = true
              Toast.makeText(context, "Jawaban berhasil disalin!", Toast.LENGTH_SHORT).show()
              scope.launch {
                delay(2000)
                copied = false
              }
            },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
              contentDescription = "Salin jawaban",
              tint = if (copied) BrizPrimary else BrizTextSecondary,
              modifier = Modifier.size(16.dp)
            )
          }

          // Share Message
          IconButton(
            onClick = {
              val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message.content)
                type = "text/plain"
              }
              val shareIntent = Intent.createChooser(sendIntent, "Bagikan Respon Briz")
              context.startActivity(shareIntent)
            },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Bagikan",
              tint = BrizTextSecondary,
              modifier = Modifier.size(16.dp)
            )
          }

          // Regenerate AI Response
          IconButton(
            onClick = onRegenerate,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Generate Ulang",
              tint = BrizTextSecondary,
              modifier = Modifier.size(16.dp)
            )
          }

          // Pin/Favorite Message
          IconButton(
            onClick = onTogglePin,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.PushPin,
              contentDescription = "Sematkan Pesan",
              tint = if (message.isPinned) BrizPrimary else BrizTextSecondary.copy(alpha = 0.5f),
              modifier = Modifier.size(16.dp)
            )
          }

          // Delete Message
          IconButton(
            onClick = onDeleteMessage,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Hapus pesan",
              tint = BrizTextTertiary,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun StreamingAiMessageItem(
  streamingText: String,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulsing_spark")
  val alpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(500),
      repeatMode = RepeatMode.Reverse
    ),
    label = "spark_alpha"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    horizontalAlignment = Alignment.Start
  ) {
    // Header with animated Briz Sparkle
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(bottom = 6.dp)
    ) {
      Box(modifier = Modifier.alpha(alpha)) {
        BrizSparkleIcon(
          size = 20.dp,
          brush = BrizSparkGradient
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Briz",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = BrizTextPrimary
      )
    }

    // Live Streaming Content
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 28.dp)
    ) {
      if (streamingText.isEmpty()) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = "Sedang menganalisis & menyusun jawaban...",
            style = MaterialTheme.typography.bodyMedium,
            color = BrizTextSecondary
          )
          Box(
            modifier = Modifier
              .size(6.dp)
              .alpha(alpha)
              .background(BrizPrimary, CircleShape)
          )
        }
      } else {
        Column {
          BrizMarkdownView(
            content = streamingText,
            isUser = false
          )
          Spacer(modifier = Modifier.height(4.dp))
          Box(
            modifier = Modifier
              .size(width = 4.dp, height = 16.dp)
              .alpha(alpha)
              .background(BrizPrimary)
          )
        }
      }
    }
  }
}
