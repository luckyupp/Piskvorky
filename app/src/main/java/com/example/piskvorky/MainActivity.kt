package com.example.piskvorky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
//import com.example.piskvorky.ui.screens.GameScreen
import com.example.piskvorky.GameScreen
import com.example.piskvorky.ui.theme.PiskvorkyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gameHistoryManager = GameHistoryManager(applicationContext)
        val viewModelFactory = GameViewModelFactory(gameHistoryManager)
        val viewModel: GameViewModel by viewModels { viewModelFactory }

        setContent {
            PiskvorkyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameScreen(viewModel)
                }
            }
        }
    }
}
