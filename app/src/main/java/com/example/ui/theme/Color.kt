package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Stylish Cool Blue & Cyan Accents (Branded as Briz)
val BrizPrimary = Color(0xFF1B6EF3) // Signature Vibrant Blue
val BrizPrimaryDark = Color(0xFF0B57D0) // Deep Google Blue
val BrizPrimaryLight = Color(0xFFD3E3FD) // Soft Blue Container
val BrizBlueLight = Color(0xFFE8F0FE) // Light Pill / Active Card

val BrizAccentPurple = Color(0xFF00BCD4) // Renamed internally, but is now Cyan/Teal
val BrizAccentPink = Color(0xFF26A69A) // Cool Emerald Accent
val BrizSparkBlue = Color(0xFF1B6EF3) // Briz Sparkle Blue
val BrizSparkGrey = Color(0xFF1B6EF3) // Compatibility alias
val BrizAccentGrey = Color(0xFF5F6368) // Neutral secondary grey

val BrizSparkGradient = Brush.linearGradient(
  colors = listOf(
    Color(0xFF1B6EF3), // Blue
    Color(0xFF00BCD4), // Cyan
    Color(0xFF26A69A)  // Emerald
  )
)

// Neutral Surfaces & Backgrounds
val BrizBgLight = Color(0xFFFFFFFF)
val BrizSurfaceLight = Color(0xFFFFFFFF)
val BrizPillLight = Color(0xFFF0F4F9) // Gemini Signature Pill
val BrizPillHover = Color(0xFFE3E9F3)
val BrizPillBorder = Color(0xFFE1E3E1)
val BrizUserBubble = Color(0xFFF0F4F9)
val BrizUserBubbleDark = Color(0xFF282A2C)

// Dark Theme Colors
val BrizBgDark = Color(0xFF131314)
val BrizSurfaceDark = Color(0xFF1E1F20)
val BrizPillDark = Color(0xFF282A2C)
val BrizPillBorderDark = Color(0xFF3C4043)
val BrizBlueDarkTheme = Color(0xFFA8C7FA)

// Typography & Content Colors
val BrizTextPrimary = Color(0xFF1F1F1F)
val BrizTextSecondary = Color(0xFF444746)
val BrizTextTertiary = Color(0xFF747775)
val BrizTextLight = Color(0xFFE3E3E3)
val BrizTextLightSecondary = Color(0xFFC4C7C5)

// Code Highlighting Tones
val CodeBg = Color(0xFF1E1F20)
val CodeHeaderBg = Color(0xFF282A2C)
val CodeText = Color(0xFFE3E3E3)
val CodeKeyword = Color(0xFFA8C7FA)
val CodeString = Color(0xFF81C995)
val CodeComment = Color(0xFF9AA0A6)
val CodeBorder = Color(0xFF3C4043)
