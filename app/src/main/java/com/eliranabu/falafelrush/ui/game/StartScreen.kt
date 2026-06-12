package com.eliranabu.falafelrush.ui.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// SCREEN 1: START SCREEN
@Composable
fun StartScreen(viewModel: GameViewModel, state: GameUiState) {
    val infiniteTransition = rememberInfiniteTransition(label = "StartAnimations")

    // Rotating 3D Falafel bouncing animation
    val bounceY by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BounceY"
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Rotate"
    )

    val activeTier = getActiveBusinessTier(state.saveState.totalCoins, state.saveState.currentDay)
    val nextTier = getNextBusinessTier(state.saveState.totalCoins, state.saveState.currentDay)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP: Settings & Highscore bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.toggleSound() },
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    .size(48.dp)
            ) {
                Text(
                    text = if (state.saveState.soundEffectsEnabled) "🔊" else "🔇",
                    fontSize = 20.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = FalafelRushTheme.DeepGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "יום ${state.saveState.currentDay}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            IconButton(
                onClick = { viewModel.setScreen(GameScreen.REVIEWS) },
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "ביקורות עבר",
                    tint = Color.White
                )
            }
        }

        // MID: Animated Logo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer {
                    translationY = bounceY
                    rotationZ = rotateAngle
                }
        ) {
            // Textured Big 3D Circle Falafel Badge
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(FalafelRushTheme.DeepGold, FalafelRushTheme.HotOrange)
                        ),
                        CircleShape
                    )
                    .shadow(16.dp, CircleShape)
                    .border(4.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🧆",
                    fontSize = 76.sp,
                    modifier = Modifier.rotate(bounceY * 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hebrew stylized 3D display text
            Text(
                text = "Street Food Empire",
                color = FalafelRushTheme.NeonCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "FALAFEL RUSH",
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                style = TextStyle(
                    shadow = Shadow(
                        color = FalafelRushTheme.CrimsonRed,
                        offset = Offset(0f, 4f),
                        blurRadius = 0f
                    )
                ),
                textAlign = TextAlign.Center
            )
        }

        // PROGRESSION: Tycoon Tier Card
        ThreeDStaticCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = FalafelRushTheme.GlassCardBg,
            borderColor = activeTier.color.copy(alpha = 0.4f)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = activeTier.emoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = activeTier.titleHe,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "תואר הממלכה הנוכחי שלך",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(activeTier.color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .border(1.5.dp, activeTier.color, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "דרגה ${when(activeTier.id) { "EMPIRE" -> "5"; "BISTRO" -> "4"; "MARKET" -> "3"; "TRUCK" -> "2"; else -> "1" }}",
                            color = activeTier.color,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = activeTier.description,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                if (nextTier != null) {
                    Spacer(modifier = Modifier.height(10.dp))

                    val coinsProgress = (state.saveState.totalCoins.toFloat() / nextTier.requiredCoins).coerceIn(0f, 1f)
                    val daysProgress = (state.saveState.currentDay.toFloat() / nextTier.requiredDay).coerceIn(0f, 1f)
                    val totalProgress = (coinsProgress + daysProgress) / 2f

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "היעד הבא: ${nextTier.emoji} ${nextTier.titleHe}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${(totalProgress * 100).toInt()}%",
                                color = FalafelRushTheme.NeonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { totalProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape),
                            color = FalafelRushTheme.NeonCyan,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "דרוש: ${nextTier.requiredCoins} מטבעות ויום ${nextTier.requiredDay}",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 9.sp,
                            textAlign = TextAlign.Left,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // DAILY EVENT STATUS CARD ON START MENU
        val prospectiveEvent = getEventForDay(state.saveState.currentDay)
        ThreeDStaticCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(prospectiveEvent.colorHex).copy(alpha = 0.15f),
            borderColor = Color(prospectiveEvent.colorHex).copy(alpha = 0.5f)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = prospectiveEvent.emoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "מזהה שוק צפוי ליום ${state.saveState.currentDay}:",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = prospectiveEvent.titleHe,
                                color = Color(prospectiveEvent.colorHex),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(prospectiveEvent.colorHex).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(prospectiveEvent.colorHex), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "מצב שוק 🔔",
                            color = Color(prospectiveEvent.colorHex),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = prospectiveEvent.descriptionHe,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // BOTTOM ACTIONS
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Stats pill
            Row(
                modifier = Modifier
                    .background(FalafelRushTheme.GlassCardBg, RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💵 קופה נוכחית: ${state.saveState.totalCoins} מטבעות",
                    color = FalafelRushTheme.GlowGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            // Big 3D Play button
            Bouncy3DButton(
                onClick = { viewModel.startNewDay() },
                backgroundColor = FalafelRushTheme.GlowGreen,
                height = 58.dp,
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "פתח את הדוכן (יום ${state.saveState.currentDay})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            // 3D Upgrade Button
            Bouncy3DButton(
                onClick = { viewModel.setScreen(GameScreen.UPGRADES) },
                backgroundColor = FalafelRushTheme.DeepGold,
                height = 50.dp,
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "שדרג ציוד, צוות ושיווק",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Safety save resets
            TextButton(
                onClick = { viewModel.resetFullGame() },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "איפוס קובץ שמירה 🔄",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
