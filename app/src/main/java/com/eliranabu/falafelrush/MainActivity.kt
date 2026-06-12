package com.eliranabu.falafelrush

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eliranabu.falafelrush.ui.game.FalafelRushApp
import com.eliranabu.falafelrush.ui.game.GameViewModel
import com.eliranabu.falafelrush.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Dynamic edge-to-edge full bleed rendering
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color(0xFF0F0A1E) // DarkSpaceBg match
                ) {
                    val gameViewModel: GameViewModel = viewModel()
                    FalafelRushApp(viewModel = gameViewModel)
                }
            }
        }
    }
}
