package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiService
import com.example.data.model.BrizPersona
import com.example.ui.theme.BrizBlueLight
import com.example.ui.theme.BrizPillBorder
import com.example.ui.theme.BrizPillLight
import com.example.ui.theme.BrizPrimary
import com.example.ui.theme.BrizSparkGradient
import com.example.ui.theme.BrizTextPrimary
import com.example.ui.theme.BrizTextSecondary
import com.example.ui.theme.BrizTextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
  currentPersona: BrizPersona,
  currentModel: String,
  currentTemperature: Float,
  customApiKey: String,
  onPersonaSelected: (BrizPersona) -> Unit,
  onModelSelected: (String) -> Unit,
  onTemperatureChanged: (Float) -> Unit,
  onCustomApiKeyChanged: (String) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var tempVal by remember { mutableFloatStateOf(currentTemperature) }
  var keyVal by remember { mutableStateOf(customApiKey) }
  var isKeyVisible by remember { mutableStateOf(false) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color.White
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 8.dp)
        .padding(bottom = 40.dp)
    ) {
      // Sheet Header with Sparkle
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 20.dp)
      ) {
        BrizSparkleIcon(
          size = 24.dp,
          brush = BrizSparkGradient
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Pengaturan Briz",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = BrizTextPrimary
        )
      }

      // 1. Model Engine Selection
      Text(
        text = "PILIHAN MODEL",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = BrizTextTertiary,
        letterSpacing = 0.5.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GeminiService.AVAILABLE_MODELS.forEach { (modelId, info) ->
          val (name, desc) = info
          val isSelected = modelId == currentModel

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(if (isSelected) BrizBlueLight else Color.Transparent)
              .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) BrizPrimary else BrizPillBorder,
                shape = RoundedCornerShape(16.dp)
              )
              .clickable { onModelSelected(modelId) }
              .padding(horizontal = 16.dp, vertical = 12.dp)
              .testTag("model_option_$name")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = name,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 15.sp,
                  color = if (isSelected) BrizPrimary else BrizTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = desc,
                  style = MaterialTheme.typography.bodySmall,
                  color = BrizTextSecondary,
                  fontSize = 12.sp
                )
              }

              if (isSelected) {
                Box(
                  modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(BrizPrimary),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Terpilih",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
      HorizontalDivider(color = BrizPillBorder)
      Spacer(modifier = Modifier.height(20.dp))

      // 2. Persona Selection
      Text(
        text = "GAYA & PERAN JAWABAN",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = BrizTextTertiary,
        letterSpacing = 0.5.sp
      )

      Spacer(modifier = Modifier.height(10.dp))

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BrizPersona.entries.forEach { persona ->
          val isSelected = persona == currentPersona

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(if (isSelected) BrizBlueLight else Color.Transparent)
              .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) BrizPrimary else BrizPillBorder,
                shape = RoundedCornerShape(16.dp)
              )
              .clickable { onPersonaSelected(persona) }
              .padding(horizontal = 16.dp, vertical = 12.dp)
              .testTag("persona_option_${persona.id}")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = persona.displayName,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 15.sp,
                  color = if (isSelected) BrizPrimary else BrizTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = persona.tagline,
                  style = MaterialTheme.typography.bodySmall,
                  color = BrizTextSecondary,
                  fontSize = 12.sp
                )
              }

              if (isSelected) {
                Box(
                  modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(BrizPrimary),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Terpilih",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
      HorizontalDivider(color = BrizPillBorder)
      Spacer(modifier = Modifier.height(20.dp))

      // 3. Temperature Slider
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "TINGKAT KREATIVITAS (TEMPERATURE)",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = BrizTextTertiary,
          letterSpacing = 0.5.sp
        )
        Text(
          text = String.format("%.2f", tempVal),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = BrizPrimary
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Slider(
        value = tempVal,
        onValueChange = {
          tempVal = it
          onTemperatureChanged(it)
        },
        valueRange = 0.0f..1.5f,
        colors = SliderDefaults.colors(
          thumbColor = BrizPrimary,
          activeTrackColor = BrizPrimary,
          inactiveTrackColor = BrizPillBorder
        ),
        modifier = Modifier.fillMaxWidth()
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Presisi & Faktual (0.0)",
          style = MaterialTheme.typography.bodySmall,
          fontSize = 11.sp,
          color = BrizTextTertiary
        )
        Text(
          text = "Kreatif & Eksploratif (1.5)",
          style = MaterialTheme.typography.bodySmall,
          fontSize = 11.sp,
          color = BrizTextTertiary
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
      HorizontalDivider(color = BrizPillBorder)
      Spacer(modifier = Modifier.height(20.dp))

      // 4. Custom API Key
      Text(
        text = "KUNCI API GEMINI (OPSIONAL)",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = BrizTextTertiary,
        letterSpacing = 0.5.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      OutlinedTextField(
        value = keyVal,
        onValueChange = {
          keyVal = it
          onCustomApiKeyChanged(it)
        },
        placeholder = { Text("Gunakan API Key default atau tempel kunci sendiri", fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = BrizPrimary,
          unfocusedBorderColor = BrizPillBorder
        ),
        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
          IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
            Icon(
              imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
              contentDescription = if (isKeyVisible) "Sembunyikan" else "Tampilkan",
              tint = BrizTextTertiary
            )
          }
        },
        singleLine = true
      )
    }
  }
}
