package com.eliranabu.falafelrush.ui.game

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import com.eliranabu.falafelrush.audio.SoundManager

// Composition-wide access to the synthesized audio engine (e.g. button ticks)
val LocalSoundManager = staticCompositionLocalOf<SoundManager?> { null }

// Master Orchestrator Component routing scenes properly
@Composable
fun FalafelRushApp(viewModel: GameViewModel) {
    val state by viewModel.uiState.collectAsState()

    CompositionLocalProvider(LocalSoundManager provides viewModel.soundManager) {
    // Gradient fluid background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        FalafelRushTheme.DarkSpaceBg,
                        FalafelRushTheme.DeepBlue
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Render Floating Particles as physical rotating emojis over all backgrounds for cosmic aesthetic depth
        Box(modifier = Modifier.fillMaxSize()) {
            state.particles.forEach { particle ->
                val xDp = with(LocalDensity.current) { particle.x.toDp() }
                val yDp = with(LocalDensity.current) { particle.y.toDp() }
                Text(
                    text = particle.emoji,
                    fontSize = (particle.size * 0.75f).sp,
                    modifier = Modifier
                        .offset(x = xDp, y = yDp)
                        .graphicsLayer {
                            rotationZ = particle.rotation
                        }
                )
            }
        }

        // Render current scenes
        AnimatedContent(
            targetState = state.currentScreen,
            transitionSpec = {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            },
            label = "ScreenSwitch"
        ) { targetScreen ->
            when (targetScreen) {
                GameScreen.START_SCREEN -> StartScreen(viewModel, state)
                GameScreen.GAMEPLAY -> GameplayScreen(viewModel, state)
                GameScreen.DAY_SUMMARY -> DaySummaryScreen(viewModel, state)
                GameScreen.UPGRADES -> UpgradesScreen(viewModel, state)
                GameScreen.REVIEWS -> HistoricReviewsScreen(viewModel, state)
            }
        }
    }
    }
}
