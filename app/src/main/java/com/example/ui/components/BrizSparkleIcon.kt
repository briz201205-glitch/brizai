package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrizPrimary
import com.example.ui.theme.BrizSparkGradient

/**
 * Custom Briz Monogram Logo Icon (Stylish Black & White).
 */
@Composable
fun BrizLogoIcon(
  modifier: Modifier = Modifier,
  size: Dp = 24.dp,
  tint: Color? = null,
  brush: Brush? = null
) {
  val fontSize = (size.value * 0.7f).sp

  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "B",
      color = Color.Black,
      fontSize = fontSize,
      fontWeight = FontWeight.Black,
      fontFamily = FontFamily.SansSerif,
      lineHeight = fontSize
    )
  }
}

/**
 * Backward compatibility alias for BrizLogoIcon
 */
@Composable
fun BrizSparkleIcon(
  modifier: Modifier = Modifier,
  size: Dp = 24.dp,
  tint: Color? = null,
  brush: Brush? = null
) {
  BrizLogoIcon(
    modifier = modifier,
    size = size,
    tint = tint,
    brush = brush
  )
}
