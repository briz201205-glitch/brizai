package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
  primary = BrizPrimary,
  onPrimary = BrizBgLight,
  primaryContainer = BrizBlueLight,
  onPrimaryContainer = BrizPrimaryDark,
  secondary = BrizAccentPurple,
  onSecondary = BrizBgLight,
  secondaryContainer = BrizPillLight,
  onSecondaryContainer = BrizTextPrimary,
  tertiary = BrizSparkBlue,
  onTertiary = BrizBgLight,
  background = BrizBgLight,
  onBackground = BrizTextPrimary,
  surface = BrizSurfaceLight,
  onSurface = BrizTextPrimary,
  surfaceVariant = BrizPillLight,
  onSurfaceVariant = BrizTextSecondary,
  outline = BrizPillBorder,
  outlineVariant = BrizPillBorder
)

private val DarkColorScheme = darkColorScheme(
  primary = BrizBlueDarkTheme,
  onPrimary = BrizBgDark,
  primaryContainer = BrizPillDark,
  onPrimaryContainer = BrizBlueDarkTheme,
  secondary = BrizAccentPurple,
  onSecondary = BrizBgDark,
  secondaryContainer = BrizPillDark,
  onSecondaryContainer = BrizTextLight,
  tertiary = BrizBlueDarkTheme,
  onTertiary = BrizBgDark,
  background = BrizBgDark,
  onBackground = BrizTextLight,
  surface = BrizSurfaceDark,
  onSurface = BrizTextLight,
  surfaceVariant = BrizPillDark,
  onSurfaceVariant = BrizTextLightSecondary,
  outline = BrizPillBorderDark,
  outlineVariant = BrizPillBorderDark
)

@Composable
fun BrizAiTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
