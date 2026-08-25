package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiService
import com.example.data.model.BrizPersona
import com.example.ui.theme.BrizAccentPurple
import com.example.ui.theme.BrizBgLight
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
fun BrizTopAppBar(
  sessionTitle: String?,
  activePersona: BrizPersona,
  activeModel: String,
  searchQuery: String,
  onSearchQueryChanged: (String) -> Unit,
  onModelSelected: (String) -> Unit,
  onOpenDrawer: () -> Unit,
  onNewChat: () -> Unit,
  onOpenSettings: () -> Unit,
  onExportChat: () -> Unit,
  modifier: Modifier = Modifier
) {
  var menuExpanded by remember { mutableStateOf(false) }
  var modelMenuExpanded by remember { mutableStateOf(false) }
  var isSearchActive by remember { mutableStateOf(false) }

  val shortModelName = remember(activeModel) {
    GeminiService.getModelShortName(activeModel)
  }

  Column(modifier = modifier.background(BrizBgLight)) {
    TopAppBar(
      colors = TopAppBarDefaults.topAppBarColors(
        containerColor = BrizBgLight,
        titleContentColor = BrizTextPrimary,
        actionIconContentColor = BrizTextPrimary,
        navigationIconContentColor = BrizTextPrimary
      ),
      navigationIcon = {
        IconButton(
          onClick = onOpenDrawer,
          modifier = Modifier.testTag("open_drawer_button")
        ) {
          Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu Riwayat",
            tint = BrizTextPrimary
          )
        }
      },
      title = {
        if (isSearchActive) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(20.dp))
              .background(BrizPillLight)
              .border(1.dp, BrizPillBorder, RoundedCornerShape(20.dp))
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = BrizPrimary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              BasicTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier
                  .weight(1f)
                  .testTag("search_input_field"),
                textStyle = TextStyle(
                  color = BrizTextPrimary,
                  fontSize = 14.sp,
                  fontFamily = FontFamily.SansSerif
                ),
                cursorBrush = SolidColor(BrizPrimary),
                singleLine = true,
                decorationBox = { innerTextField ->
                  if (searchQuery.isEmpty()) {
                    Text(
                      text = "Cari di percakapan...",
                      fontSize = 14.sp,
                      color = BrizTextTertiary
                    )
                  }
                  innerTextField()
                }
              )
              if (searchQuery.isNotEmpty()) {
                IconButton(
                  onClick = { onSearchQueryChanged("") },
                  modifier = Modifier.size(20.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Hapus pencarian",
                    tint = BrizTextSecondary,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }
            }
          }
        } else {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            BrizSparkleIcon(
              size = 22.dp,
              brush = BrizSparkGradient
            )

            Text(
              text = "Briz",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp,
              color = BrizTextPrimary
            )

            // Model Switcher Pill
            Box {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .background(BrizBlueLight)
                  .border(1.dp, BrizPrimary.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                  .clickable { modelMenuExpanded = true }
                  .padding(horizontal = 10.dp, vertical = 4.dp)
                  .testTag("model_switcher_pill")
              ) {
                Text(
                  text = shortModelName,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 12.sp,
                  color = BrizPrimary
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                  imageVector = Icons.Default.ArrowDropDown,
                  contentDescription = "Pilih Model",
                  tint = BrizPrimary,
                  modifier = Modifier.size(16.dp)
                )
              }

              DropdownMenu(
                expanded = modelMenuExpanded,
                onDismissRequest = { modelMenuExpanded = false },
                modifier = Modifier
                  .background(Color.White)
                  .border(1.dp, BrizPillBorder, RoundedCornerShape(12.dp))
                  .padding(vertical = 4.dp)
              ) {
                GeminiService.AVAILABLE_MODELS.forEach { (modelId, info) ->
                  val (name, desc) = info
                  val isSelected = modelId == activeModel
                  DropdownMenuItem(
                    text = {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                      ) {
                        Column {
                          Text(
                            text = name,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (isSelected) BrizPrimary else BrizTextPrimary
                          )
                          Text(
                            text = desc,
                            fontSize = 11.sp,
                            color = BrizTextSecondary
                          )
                        }
                        if (isSelected) {
                          Spacer(modifier = Modifier.width(8.dp))
                          Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = BrizPrimary,
                            modifier = Modifier.size(16.dp)
                          )
                        }
                      }
                    },
                    onClick = {
                      modelMenuExpanded = false
                      onModelSelected(modelId)
                    }
                  )
                }
              }
            }
          }
        }
      },
      actions = {
        // Toggle Search Button
        IconButton(
          onClick = {
            isSearchActive = !isSearchActive
            if (!isSearchActive) onSearchQueryChanged("")
          },
          modifier = Modifier.testTag("toggle_search_button")
        ) {
          Icon(
            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
            contentDescription = "Cari Pesan",
            tint = if (isSearchActive) BrizPrimary else BrizTextPrimary
          )
        }

        if (!isSearchActive) {
          // New Chat action
          IconButton(
            onClick = onNewChat,
            modifier = Modifier.testTag("new_chat_button")
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Obrolan Baru",
              tint = BrizTextPrimary
            )
          }

          // Settings action
          IconButton(
            onClick = onOpenSettings,
            modifier = Modifier.testTag("settings_button")
          ) {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = "Pengaturan",
              tint = BrizTextPrimary
            )
          }

          // Overflow Menu
          Box {
            IconButton(
              onClick = { menuExpanded = true },
              modifier = Modifier.testTag("menu_more_button")
            ) {
              Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu Lainnya",
                tint = BrizTextPrimary
              )
            }

            DropdownMenu(
              expanded = menuExpanded,
              onDismissRequest = { menuExpanded = false },
              modifier = Modifier
                .background(Color.White)
                .border(1.dp, BrizPillBorder, RoundedCornerShape(12.dp))
            ) {
              DropdownMenuItem(
                text = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Share,
                      contentDescription = null,
                      modifier = Modifier.size(18.dp),
                      tint = BrizTextSecondary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                      text = "Ekspor Obrolan (.md)",
                      style = MaterialTheme.typography.bodyMedium,
                      color = BrizTextPrimary
                    )
                  }
                },
                onClick = {
                  menuExpanded = false
                  onExportChat()
                }
              )
            }
          }
        }
      }
    )
  }
}
