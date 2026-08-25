package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrizPersona
import com.example.ui.theme.BrizTextSecondary

@Composable
fun HeroEmptyState(
  persona: BrizPersona,
  modelName: String,
  onSelectPrompt: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 24.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxWidth()
    ) {
      // Briz Exclusive Monogram Badge (Black & White)
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(Color.White)
          .border(2.dp, Color.Black, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        BrizLogoIcon(size = 44.dp)
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Stylish Black Greeting
      Text(
        text = "Halo, Briz di sini",
        style = TextStyle(
          fontFamily = FontFamily.SansSerif,
          fontWeight = FontWeight.Black,
          fontSize = 34.sp,
          color = Color.Black
        ),
        letterSpacing = (-0.5).sp,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Apa yang bisa kita selesaikan hari ini?",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Medium,
        color = BrizTextSecondary,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        textAlign = TextAlign.Center
      )
    }
  }
}
