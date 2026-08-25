package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.screens.MainChatScreen
import com.example.ui.theme.BrizAiTheme
import com.example.ui.theme.BrizBgLight
import com.example.ui.viewmodel.BrizChatViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: BrizChatViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      BrizAiTheme(darkTheme = false) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(BrizBgLight)
        ) {
          MainChatScreen(viewModel = viewModel)
        }
      }
    }
  }
}
