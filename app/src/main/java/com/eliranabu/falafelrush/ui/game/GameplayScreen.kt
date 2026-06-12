package com.eliranabu.falafelrush.ui.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

// SCREEN 2: ACTIVE GAMEPLAY SCREEN (THE RUSH)
@Composable
fun GameplayScreen(viewModel: GameViewModel, state: GameUiState) {
    val screenShakeOffset = remember(state.screenShake) {
        if (state.screenShake > 0) {
            val mult = 1
            Offset(
                x = (sin(System.currentTimeMillis().toDouble() / 15f) * state.screenShake * mult).toFloat(),
                y = (sin(System.currentTimeMillis().toDouble() / 10f) * state.screenShake * mult).toFloat()
            )
        } else {
            Offset.Zero
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .offset(x = screenShakeOffset.x.dp, y = screenShakeOffset.y.dp)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP HUD BAR
        HUDBar(state, onExitClicked = { viewModel.setScreen(GameScreen.START_SCREEN) })

        // Active Daily Event Banner during gameplay
        if (state.activeEvent != DailyEvent.NORMAL) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(state.activeEvent.colorHex).copy(alpha = 0.15f))
                    .border(1.5.dp, Color(state.activeEvent.colorHex).copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = state.activeEvent.emoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${state.activeEvent.titleHe}: ${state.activeEvent.descriptionHe}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Rush Hour neon alert banner
        if (state.isRushHour) {
            val infiniteTransition = rememberInfiniteTransition(label = "RushFlasher")
            val alphaGlow by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "Glow"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FalafelRushTheme.HotOrange.copy(alpha = 0.2f))
                    .border(2.dp, FalafelRushTheme.HotOrange.copy(alpha = alphaGlow), RoundedCornerShape(12.dp))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🚨 שעת עומס פעילה! 🚨 מכפיל מהירות וטיפים X1.5!",
                    color = FalafelRushTheme.BrightGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // CENTRAL AREA: Queue of active customers
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (state.activeCustomers.isEmpty()) "ממתין ללקוחות הבאים... 🧎" else "תור הלקוחות לעסק:",
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Draw customers cards list
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.activeCustomers.forEachIndexed { index, customer ->
                    CustomerVisualCard(
                        customer = customer,
                        isNextInQueue = index == 0
                    )
                }
            }
        }

        // MIDDLE-BOTTOM: Interactive 3D Assembly Workspace Drawer
        ThreeDStaticCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 10.dp),
            backgroundColor = FalafelRushTheme.GlassCardBg.copy(alpha = 0.85f),
            borderColor = FalafelRushTheme.NeonCyan.copy(alpha = 0.25f)
        ) {
            PitaAssemblyWorkspace(
                preparedIngredients = state.preparedIngredients,
                onIngredientAdded = { viewModel.tapAddIngredient(it) }
            )
        }

        // Live status ticker/interactive text banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.feedbackMessage,
                color = if (state.feedbackMessage.contains("אוי לא") || state.feedbackMessage.contains("לא נכון")) FalafelRushTheme.CrimsonRed else Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // BOTTOM PREPARATION CONTROLS BAR (TACTILE BIG TAP PADS)
        KitchenCookingPad(
            state = state,
            onTrashClicked = { viewModel.tapTrashPita() },
            onServeClicked = { viewModel.tapServePita() }
        )
    }
}

// Sub-Component: Top Header HUD stats Bar (Designed as a Premium Blackboard Menu / Food Slate)
@Composable
fun HUDBar(state: GameUiState, onExitClicked: () -> Unit) {
    // Score calculation
    val chefScore = (state.revenueEarnedToday * 10) + (state.servedCountToday * 55) + (state.comboStreak * 20) - (state.failedCountToday * 12)
    val positiveScore = chefScore.coerceAtLeast(0)

    val scaleAnimation = remember { Animatable(1f) }
    LaunchedEffect(positiveScore) {
        scaleAnimation.animateTo(1.15f, animationSpec = tween(150, easing = EaseOutBack))
        scaleAnimation.animateTo(1.0f, animationSpec = tween(150, easing = EaseInBack))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2C221E), Color(0xFF1E1613)) // Rustic dark mahogany wood board
                )
            )
            .border(2.dp, FalafelRushTheme.DeepGold.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // EXIT BUTTON (Styled like some chalkboard chalk text mark)
            IconButton(
                onClick = onExitClicked,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    .size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "חזרה",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }

            // SCORE / STARS (Chef Level Score)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scaleAnimation.value
                        scaleY = scaleAnimation.value
                    }
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "⭐", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "נקודות שף",
                        color = FalafelRushTheme.BrightGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = "$positiveScore",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }

            // MIDDLE COUNTDOWN: Circular Fryer/Oil Temperature Timer!
            Row(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(28.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { state.dayTimeRemainingSec / 120f },
                        color = if (state.dayTimeRemainingSec <= 15) FalafelRushTheme.CrimsonRed else FalafelRushTheme.GlowGreen,
                        trackColor = Color.White.copy(alpha = 0.15f),
                        strokeWidth = 3.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = if (state.dayTimeRemainingSec <= 15) "🔥" else "⏱️",
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "זמן נותר",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${state.dayTimeRemainingSec} שניות",
                        color = if (state.dayTimeRemainingSec <= 15) FalafelRushTheme.CrimsonRed else Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }

            // COINS / GROSS PROGRESS
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "🪙", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "קופה יומית",
                        color = FalafelRushTheme.GlowGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = "${state.revenueEarnedToday}",
                    color = FalafelRushTheme.GlowGreen,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }

            // COMBO STREAK
            if (state.comboStreak > 1) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                listOf(FalafelRushTheme.HotOrange, FalafelRushTheme.DeepGold)
                            ),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "X${state.comboStreak} 🔥",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

// Sub-Component: 3D Kitchen preparation buttons
@Composable
fun KitchenCookingPad(
    state: GameUiState,
    onTrashClicked: () -> Unit,
    onServeClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // BOTTOM Row: Actions (TRASH vs SERVE)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Bouncy3DButton(
                onClick = onTrashClicked,
                backgroundColor = FalafelRushTheme.CrimsonRed,
                modifier = Modifier.weight(0.35f),
                height = 54.dp,
                enabled = state.preparedIngredients.isNotEmpty()
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "זרוק לפח", tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "פח", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Bouncy3DButton(
                onClick = onServeClicked,
                backgroundColor = FalafelRushTheme.GlowGreen,
                modifier = Modifier.weight(0.65f),
                height = 54.dp,
                enabled = state.preparedIngredients.isNotEmpty() && state.activeCustomers.isNotEmpty()
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "הגש מנה", tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "הגש מנה מושלמת 🛎️",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}
