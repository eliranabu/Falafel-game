package com.example.ui.game

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.absoluteValue
import com.example.data.database.CustomerReview
import kotlinx.coroutines.delay
import kotlin.math.sin

// --- Premium 2026 Color Palette & Theme Tokens ---
object FalafelRushTheme {
    val DarkSpaceBg = Color(0xFF0F0A1E)
    val GlassCardBg = Color(0x331C1A32)
    val GlowGreen = Color(0xFF00E676)
    val DeepGold = Color(0xFFFFB300)
    val BrightGold = Color(0xFFFFE082)
    val HotOrange = Color(0xFFFF5722)
    val NeonCyan = Color(0xFF00E5FF)
    val CrimsonRed = Color(0xFFFF1744)
    val DeepBlue = Color(0xFF1E1B4B)
    val CozyCreame = Color(0xFFFDFBF7)
}

// Custom 3D Bouncy Pressed Extruded Button Component
@Composable
fun Bouncy3DButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = FalafelRushTheme.NeonCyan,
    extrudedColor: Color = backgroundColor.darken(0.3f),
    contentColor: Color = Color.White,
    height: Dp = 60.dp,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Scale and click compress spring animations
    val buttonOffsetY by animateDpAsState(
        targetValue = if (isPressed) 6.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "OffsetY"
    )

    Box(
        modifier = modifier
            .height(height + 8.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
    ) {
        // Bottom 3D Layer (the block extrusion shadow depth)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (enabled) extrudedColor else Color.Gray.darken(0.2f))
        )
        
        // Secondary Front Active Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = buttonOffsetY)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (enabled) {
                        Brush.linearGradient(
                            colors = listOf(backgroundColor, backgroundColor.lighten(0.15f))
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(Color.Gray, Color.LightGray)
                        )
                    }
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = content
            )
        }
    }
}

// Beautiful 3D extruded static panel for cards
@Composable
fun ThreeDStaticCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = FalafelRushTheme.GlassCardBg,
    borderColor: Color = Color.White.copy(alpha = 0.15f),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(16.dp),
        content = content
    )
}

// Utility extension functions to lighten or darken color tokens programmatically
fun Color.lighten(factor: Float): Color {
    return Color(
        red = (red + (1f - red) * factor).coerceIn(0f, 1f),
        green = (green + (1f - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}

fun Color.darken(factor: Float): Color {
    return Color(
        red = (red * (1f - factor)).coerceIn(0f, 1f),
        green = (green * (1f - factor)).coerceIn(0f, 1f),
        blue = (blue * (1f - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

// Master Orchestrator Component routing scenes properly
@Composable
fun FalafelRushApp(viewModel: GameViewModel) {
    val state by viewModel.uiState.collectAsState()
    
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

// --- progression model for Tycoon / Business Tiers ---
data class BusinessTier(
    val id: String,
    val titleHe: String,
    val emoji: String,
    val requiredCoins: Int,
    val requiredDay: Int,
    val color: Color,
    val description: String
)

fun getActiveBusinessTier(totalCoins: Int, day: Int): BusinessTier {
    return when {
        totalCoins >= 4000 && day >= 15 -> BusinessTier("EMPIRE", "אימפריית פלאפל עולמית", "🌍", 4000, 15, Color(0xFFD500F9), "סניפים בלונדון, ניו יורק וטוקיו! הטעמים והשיטה התפעולית שלך שולטים בעולם.")
        totalCoins >= 1400 && day >= 8 -> BusinessTier("BISTRO", "מסעדת גורמה פלאפל", "🏛️", 1400, 8, Color(0xFF00E5FF), "מסעדה מודרנית ויוקרתית עם לקוחות VIP קבועים ומנות מעושנות.")
        totalCoins >= 600 && day >= 4 -> BusinessTier("MARKET", "בסטה בשוק מחנה יהודה", "🏪", 600, 4, Color(0xFFFF9100), "רעש של בסטה, מוזיקת שוק ופיוז'ן של סלטים וטחינה סמיכה.")
        totalCoins >= 150 && day >= 2 -> BusinessTier("TRUCK", "פודטראק פלאפל מעוצב", "🚚", 150, 2, Color(0xFFFFD54F), "גלגלים, שירות קליל, מוזיקת רטרו והכנסות מעולות ברחבי העיר.")
        else -> BusinessTier("CART", "דוכן שכונתי פשוט", "🫓", 0, 1, Color(0xFFA1887F), "התחלת העסק הצנוע שלך ברחוב קטן. תהפוך את כדורי הפלאפל למכרה של זהב!")
    }
}

fun getNextBusinessTier(totalCoins: Int, day: Int): BusinessTier? {
    return when {
        totalCoins < 150 || day < 2 -> BusinessTier("TRUCK", "פודטראק פלאפל מעוצב", "🚚", 150, 2, Color(0xFFFFD54F), "")
        totalCoins < 600 || day < 4 -> BusinessTier("MARKET", "בסטה בשוק מחנה יהודה", "🏪", 600, 4, Color(0xFFFF9100), "")
        totalCoins < 1400 || day < 8 -> BusinessTier("BISTRO", "מסעדת גורמה פלאפל", "🏛️", 1400, 8, Color(0xFF00E5FF), "")
        totalCoins < 4000 || day < 15 -> BusinessTier("EMPIRE", "אימפריית פלאפל עולמית", "🌍", 4000, 15, Color(0xFFD500F9), "")
        else -> null
    }
}

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

// Sub-Component: High fidelity vector-drawn animated Customer Figure character
@Composable
fun HumanCharacterSillhouette(customer: GameCustomer, modifier: Modifier = Modifier) {
    val patience = customer.currentPatience
    
    // Shaking physics if patience is low (anger)
    val shakeTransition = rememberInfiniteTransition(label = "ShakeTransition")
    val shakeX by shakeTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(80, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShakeX"
    )
    val shakeY by shakeTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(70, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShakeY"
    )

    // Breathing and gentle balance swaying animations
    val breathTransition = rememberInfiniteTransition(label = "Breath")
    val breatheY by breathTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreatheY"
    )
    val swayAngle by breathTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Sway"
    )

    val appliedTranslationX = if (patience <= 0.25f) shakeX else 0f
    val appliedTranslationY = (if (patience <= 0.25f) shakeY else 0f) + breatheY
    val appliedRotation = if (patience <= 0.25f) (shakeX * 1.5f) else swayAngle

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = appliedTranslationX
                translationY = appliedTranslationY
                rotationZ = appliedRotation
            }
            .size(100.dp, 105.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. SKIN TONE & CLOTHING BASE COLORING BY ARCHETYPE
            val skinColor = when (customer.type) {
                CustomerType.STUDENT -> Color(0xFFFCD5B5)
                CustomerType.TOURIST -> Color(0xFFE8C39E)
                CustomerType.BUSINESSMAN -> Color(0xFFF7C3A0)
                CustomerType.CRITIC -> Color(0xFFF4D0A8)
                CustomerType.CELEBRITY -> Color(0xFFFFD54F) // Glamour gold
                CustomerType.FAMILY -> Color(0xFFFFE0BD)
            }

            val clothingColor = when (customer.type) {
                CustomerType.STUDENT -> Color(0xFF1E88E5) // Athletic blue hoodie
                CustomerType.TOURIST -> Color(0xFFFFB300) // Sunny yellow hawaiian base
                CustomerType.BUSINESSMAN -> Color(0xFF37474F) // Refined charcoal gray suit
                CustomerType.CRITIC -> Color(0xFF8D6E63) // Vintage brown trench coat
                CustomerType.CELEBRITY -> Color(0xFFD81B60) // Designer velvet magenta
                CustomerType.FAMILY -> Color(0xFF4DB6AC) // Festive warm turquoise
            }

            // 2. TORSO & SHOULDERS
            val torsoPath = Path().apply {
                moveTo(w * 0.15f, h)
                quadraticBezierTo(w * 0.2f, h * 0.62f, w * 0.5f, h * 0.62f) // neck insertion
                quadraticBezierTo(w * 0.8f, h * 0.62f, w * 0.85f, h)
                close()
            }
            drawPath(path = torsoPath, color = clothingColor)

            // Executive necktie for Businessman
            if (customer.type == CustomerType.BUSINESSMAN) {
                val tiePath = Path().apply {
                    moveTo(w * 0.48f, h * 0.65f)
                    lineTo(w * 0.52f, h * 0.65f)
                    lineTo(w * 0.53f, h * 0.84f)
                    lineTo(w * 0.5f, h * 0.9f)
                    lineTo(w * 0.47f, h * 0.84f)
                    close()
                }
                drawPath(path = tiePath, color = Color(0xFFD32F2F)) // Red tie
            } else if (customer.type == CustomerType.CELEBRITY) {
                // Gold collar chain
                drawArc(
                    color = Color(0xFFFFD54F),
                    startAngle = 10f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(w * 0.4f, h * 0.61f),
                    size = androidx.compose.ui.geometry.Size(w * 0.2f, h * 0.1f),
                    style = Stroke(width = 3f)
                )
            }

            // 3. NECK
            drawRect(
                color = skinColor.darken(0.08f),
                topLeft = Offset(w * 0.42f, h * 0.45f),
                size = androidx.compose.ui.geometry.Size(w * 0.16f, h * 0.2f)
            )

            // 4. FACE GLOBE
            val faceRadius = w * 0.26f
            val faceCenter = Offset(w * 0.5f, h * 0.35f)
            drawCircle(color = skinColor, radius = faceRadius, center = faceCenter)

            // 5. HAIR & HEADGEAR
            when (customer.type) {
                CustomerType.STUDENT -> {
                    // Modern messy haircut
                    val hairPath = Path().apply {
                        moveTo(w * 0.22f, h * 0.32f)
                        quadraticBezierTo(w * 0.3f, h * 0.06f, w * 0.5f, h * 0.09f)
                        quadraticBezierTo(w * 0.7f, h * 0.06f, w * 0.78f, h * 0.32f)
                        lineTo(w * 0.72f, h * 0.18f)
                        lineTo(w * 0.28f, h * 0.18f)
                        close()
                    }
                    drawPath(path = hairPath, color = Color(0xFF5D4037))
                }
                CustomerType.BUSINESSMAN -> {
                    // Sleek, gelled black combed hair
                    val hairPath = Path().apply {
                        moveTo(w * 0.23f, h * 0.32f)
                        quadraticBezierTo(w * 0.36f, h * 0.06f, w * 0.64f, h * 0.08f)
                        quadraticBezierTo(w * 0.74f, h * 0.18f, w * 0.77f, h * 0.32f)
                        lineTo(w * 0.68f, h * 0.22f)
                        lineTo(w * 0.32f, h * 0.2f)
                        close()
                    }
                    drawPath(path = hairPath, color = Color(0xFF212121))
                }
                CustomerType.TOURIST -> {
                    // Giant yellow vacation beach hat
                    drawCircle(
                        color = Color(0xFFFFF176),
                        radius = faceRadius * 1.3f,
                        center = Offset(faceCenter.x, faceCenter.y - faceRadius * 0.3f)
                    )
                    drawRect(
                        color = Color(0xFFF57F17), // Hat red-orange band
                        topLeft = Offset(w * 0.18f, h * 0.16f),
                        size = androidx.compose.ui.geometry.Size(w * 0.64f, h * 0.04f)
                    )
                }
                CustomerType.CRITIC -> {
                    // Tilt French designer beret
                    val beretPath = Path().apply {
                        moveTo(w * 0.18f, h * 0.26f)
                        quadraticBezierTo(w * 0.45f, h * 0.03f, w * 0.8f, h * 0.16f)
                        quadraticBezierTo(w * 0.68f, h * 0.32f, w * 0.22f, h * 0.3f)
                        close()
                    }
                    drawPath(path = beretPath, color = Color(0xFF1E1E1E))
                }
                CustomerType.CELEBRITY -> {
                    // Royal glamorous long purple curly hair cascading
                    val hairLeft = Path().apply {
                        moveTo(w * 0.26f, h * 0.28f)
                        lineTo(w * 0.12f, h * 0.7f)
                        lineTo(w * 0.32f, h * 0.48f)
                        close()
                    }
                    val hairRight = Path().apply {
                        moveTo(w * 0.74f, h * 0.28f)
                        lineTo(w * 0.88f, h * 0.7f)
                        lineTo(w * 0.68f, h * 0.48f)
                        close()
                    }
                    drawPath(path = hairLeft, color = Color(0xFFCE93D8))
                    drawPath(path = hairRight, color = Color(0xFFCE93D8))
                }
                CustomerType.FAMILY -> {
                    // Generous fluffy curls
                    drawCircle(color = Color(0xFF8D6E63), radius = faceRadius * 1.12f, center = faceCenter)
                    drawCircle(color = skinColor, radius = faceRadius, center = faceCenter)
                }
            }

            // 6. ADAPTIVE EYES THAT REACT TO PATIENCE LEVELS
            val eyeY = h * 0.33f
            val eyeRad = w * 0.04f
            
            if (patience > 0.4f) {
                // Happy sparkling eyes
                drawCircle(color = Color.Black, radius = eyeRad, center = Offset(w * 0.42f, eyeY))
                drawCircle(color = Color.Black, radius = eyeRad, center = Offset(w * 0.58f, eyeY))
                // Glint reflections
                drawCircle(color = Color.White, radius = eyeRad * 0.4f, center = Offset(w * 0.4f, eyeY - 2f))
                drawCircle(color = Color.White, radius = eyeRad * 0.4f, center = Offset(w * 0.56f, eyeY - 2f))
            } else if (patience > 0.15f) {
                // Impatient straight annoyed shut eyes
                drawLine(color = Color.Black, start = Offset(w * 0.38f, eyeY), end = Offset(w * 0.46f, eyeY), strokeWidth = 3.5f)
                drawLine(color = Color.Black, start = Offset(w * 0.54f, eyeY), end = Offset(w * 0.62f, eyeY), strokeWidth = 3.5f)
            } else {
                // Furious cross/x-shaped eyes
                drawLine(color = Color(0xFFFF1744), start = Offset(w * 0.38f, eyeY - 3f), end = Offset(w * 0.46f, eyeY + 3f), strokeWidth = 4.5f)
                drawLine(color = Color(0xFFFF1744), start = Offset(w * 0.46f, eyeY - 3f), end = Offset(w * 0.38f, eyeY + 3f), strokeWidth = 4.5f)
                drawLine(color = Color(0xFFFF1744), start = Offset(w * 0.54f, eyeY - 3f), end = Offset(w * 0.62f, eyeY + 3f), strokeWidth = 4.5f)
                drawLine(color = Color(0xFFFF1744), start = Offset(w * 0.62f, eyeY - 3f), end = Offset(w * 0.54f, eyeY + 3f), strokeWidth = 4.5f)
            }

            // 7. RESPONSIVE MOUTH PORTRAYAL
            val mouthY = h * 0.44f
            when {
                patience > 0.7f -> {
                    // Big wide curved smiley mouth
                    drawArc(
                        color = Color.Black,
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.44f, mouthY - 3f),
                        size = androidx.compose.ui.geometry.Size(w * 0.12f, h * 0.05f)
                    )
                }
                patience > 0.35f -> {
                    // Straight face line
                    drawLine(
                        color = Color.Black,
                        start = Offset(w * 0.44f, mouthY),
                        end = Offset(w * 0.56f, mouthY),
                        strokeWidth = 3f
                    )
                }
                patience > 0.15f -> {
                    // Sad frowny curve
                    drawArc(
                        color = Color.Black,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.44f, mouthY),
                        size = androidx.compose.ui.geometry.Size(w * 0.12f, h * 0.04f),
                        style = Stroke(width = 3f)
                    )
                }
                else -> {
                    // Desperate low patience screaming circle
                    drawCircle(color = Color(0xFFFF1744), radius = w * 0.055f, center = Offset(w * 0.5f, mouthY + h * 0.01f))
                    drawCircle(color = Color.Black, radius = w * 0.035f, center = Offset(w * 0.5f, mouthY + h * 0.01f))
                }
            }

            // 8. ACCESSORIES (Glasses, jewelry, hats, monocles)
            when (customer.type) {
                CustomerType.STUDENT -> {
                    // Round smart specs
                    drawCircle(color = Color.Black, radius = w * 0.075f, center = Offset(w * 0.42f, eyeY), style = Stroke(width = 2.5f))
                    drawCircle(color = Color.Black, radius = w * 0.075f, center = Offset(w * 0.58f, eyeY), style = Stroke(width = 2.5f))
                    drawLine(color = Color.Black, start = Offset(w * 0.49f, eyeY), end = Offset(w * 0.51f, eyeY), strokeWidth = 2.5f)
                }
                CustomerType.BUSINESSMAN -> {
                    // Rectangular sleek glasses
                    drawRect(color = Color(0xFF1E88E5), topLeft = Offset(w * 0.36f, eyeY - 5f), size = androidx.compose.ui.geometry.Size(w * 0.11f, h * 0.035f), style = Stroke(width = 2.5f))
                    drawRect(color = Color(0xFF1E88E5), topLeft = Offset(w * 0.53f, eyeY - 5f), size = androidx.compose.ui.geometry.Size(w * 0.11f, h * 0.035f), style = Stroke(width = 2.5f))
                    drawLine(color = Color(0xFF1E88E5), start = Offset(w * 0.47f, eyeY), end = Offset(w * 0.53f, eyeY), strokeWidth = 2.5f)
                }
                CustomerType.TOURIST -> {
                    // Deep dark cool vacation sunglasses
                    drawCircle(color = Color(0xFF263238), radius = w * 0.07f, center = Offset(w * 0.42f, eyeY))
                    drawCircle(color = Color(0xFF263238), radius = w * 0.07f, center = Offset(w * 0.58f, eyeY))
                    drawLine(color = Color(0xFF263238), start = Offset(w * 0.49f, eyeY), end = Offset(w * 0.51f, eyeY), strokeWidth = 4f)
                }
                CustomerType.CRITIC -> {
                    // Monocle & fine mustache
                    drawCircle(color = Color(0xFFFFD54F), radius = w * 0.08f, center = Offset(w * 0.4f, eyeY), style = Stroke(width = 3f))
                    drawLine(color = Color(0xFFFFD54F), start = Offset(w * 0.32f, eyeY), end = Offset(w * 0.25f, eyeY - 8f), strokeWidth = 2f)

                    val mustache = Path().apply {
                        moveTo(w * 0.35f, h * 0.43f)
                        quadraticBezierTo(w * 0.5f, h * 0.4f, w * 0.65f, h * 0.43f)
                        quadraticBezierTo(w * 0.5f, h * 0.48f, w * 0.35f, h * 0.43f)
                        close()
                    }
                    drawPath(path = mustache, color = Color(0xFF212121))
                }
                CustomerType.CELEBRITY -> {
                    // Fancy superstar star-shaped designer sunglasses
                    drawCircle(color = Color(0xFFFFD54F), radius = w * 0.08f, center = Offset(w * 0.41f, eyeY), style = Stroke(width = 3.5f))
                    drawCircle(color = Color(0xFFFFD54F), radius = w * 0.08f, center = Offset(w * 0.59f, eyeY), style = Stroke(width = 3.5f))
                }
                else -> {}
            }
        }

        // Floating Overlaid Emblems
        when (customer.type) {
            CustomerType.CELEBRITY -> {
                Text(
                    text = "👑",
                    fontSize = 22.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-3).dp)
                )
            }
            CustomerType.FAMILY -> {
                Text(
                    text = "🎈",
                    fontSize = 15.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 10.dp, y = (-42).dp)
                )
            }
            else -> {}
        }
    }
}

// Sub-Component: Customer Card
// Sub-Component: Overhauled Customer Card with dynamic standing characters & Hebrew speech bubbles
@Composable
fun CustomerVisualCard(customer: GameCustomer, isNextInQueue: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "CustomerPulse")
    
    // Scale pulse for next in line customer
    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreatheScale"
    )

    val borderGlowColor = if (isNextInQueue) {
        if (customer.isVip) FalafelRushTheme.DeepGold else FalafelRushTheme.NeonCyan
    } else {
        Color.White.copy(alpha = 0.05f)
    }

    // Comprehensive column representing the active character standing in queue
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(if (isNextInQueue) scaleFactor else 0.95f)
            .width(170.dp)
            .padding(vertical = 4.dp)
    ) {
        // 1. SPEECH BALLOON: Quote and exact required ingredients
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(14.dp))
                .background(Color.White, RoundedCornerShape(14.dp))
                .border(1.dp, Color(0xFFE5E5E5), RoundedCornerShape(14.dp))
                .padding(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Hebrew quote phrase
                Text(
                    text = "\"${customer.phrase}\"",
                    color = Color(0xFF2C2C2C),
                    fontSize = 9.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(6.dp))

                // Tags displaying food items needed
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "בקשה: ",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    customer.requiredOrder.forEach { ingredient ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 1.dp)
                                .background(Color(ingredient.colorHex).copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, Color(ingredient.colorHex), CircleShape)
                                .size(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ingredient.emoji,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Pointer triangle tail
        Canvas(
            modifier = Modifier
                .size(16.dp, 8.dp)
                .offset(y = (-1).dp)
        ) {
            val trianglePath = Path().apply {
                moveTo(size.width / 2, size.height)
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(path = trianglePath, color = Color.White)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 2. THE STANDING RETRO HUMAN ILLUSTRATION WITH GLOWS
        Box(
            modifier = Modifier.height(105.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .size(75.dp, 10.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                borderGlowColor.copy(alpha = 0.45f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Animated Figure
            HumanCharacterSillhouette(customer = customer)
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3. SECURE BLACKBOARD PANEL (Stats progress bar, level of patience)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (customer.isVip) {
                        Brush.verticalGradient(
                            listOf(Color(0xFF2A1F11), Color(0xFF140E06))
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(Color(0xFF211B35), Color(0xFF120E22))
                        )
                    }
                )
                .border(2.dp, borderGlowColor, RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = customer.name,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                    
                    Box(
                        modifier = Modifier
                            .background(
                                if (isNextInQueue) FalafelRushTheme.NeonCyan else Color.White.copy(alpha = 0.1f),
                                CircleShape
                            )
                            .size(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isNextInQueue) "1" else "•",
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = customer.type.typeName,
                    color = if (customer.isVip) FalafelRushTheme.DeepGold else FalafelRushTheme.NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(5.dp))

                // Patience meter progress bar
                val patience = customer.currentPatience
                val patienceColor = when {
                    patience > 0.6f -> FalafelRushTheme.GlowGreen
                    patience > 0.3f -> FalafelRushTheme.DeepGold
                    else -> FalafelRushTheme.CrimsonRed
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = { patience },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape),
                        color = patienceColor,
                        trackColor = Color.White.copy(alpha = 0.1f),
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "סבלנות:",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(patience * 100).toInt()}%",
                            color = patienceColor,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun unused_original_visual_card(customer: GameCustomer, isNextInQueue: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "CustomerPulse")
    
    // Scale pulse for next in line customer
    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreatheScale"
    )

    val borderGlowColor = if (isNextInQueue) {
        if (customer.isVip) FalafelRushTheme.DeepGold else FalafelRushTheme.NeonCyan
    } else {
        Color.White.copy(alpha = 0.05f)
    }

    Box(
        modifier = Modifier
            .scale(if (isNextInQueue) scaleFactor else 0.95f)
            .width(170.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (customer.isVip) {
                    Brush.verticalGradient(
                        listOf(Color(0xFF2A2010), Color(0xFF151005))
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(Color(0xFF20163A), Color(0xFF100B25))
                    )
                }
            )
            .border(2.dp, borderGlowColor, RoundedCornerShape(20.dp))
            .padding(10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Customer Meta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = customer.type.typeName,
                    color = if (customer.isVip) FalafelRushTheme.DeepGold else Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
                
                // Queuing rank badge
                Box(
                    modifier = Modifier
                        .background(
                            if (isNextInQueue) FalafelRushTheme.NeonCyan else Color.White.copy(alpha = 0.1f),
                            CircleShape
                        )
                        .size(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isNextInQueue) "1" else "•",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Patient emotional mood mapping
            val moodEmoji = when {
                customer.currentPatience > 0.75f -> "😋"
                customer.currentPatience > 0.40f -> "😐"
                customer.currentPatience > 0.15f -> "⏱️"
                else -> "😡"
            }

            // Avatar sphere
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(
                        if (customer.isVip) {
                            Brush.radialGradient(
                                colors = listOf(FalafelRushTheme.DeepGold, Color.Black)
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(FalafelRushTheme.DeepBlue, Color.Transparent)
                            )
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customer.avatar,
                    fontSize = 32.sp
                )
                // Floating emotional indicator tag
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(Color.Black, CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                        .padding(2.dp)
                ) {
                    Text(text = moodEmoji, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = customer.name,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                maxLines = 1,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Patience meter/Progress bar (Gradients changing colors automatically)
            val patienceColor = when {
                customer.currentPatience > 0.6f -> FalafelRushTheme.GlowGreen
                customer.currentPatience > 0.3f -> FalafelRushTheme.DeepGold
                else -> FalafelRushTheme.CrimsonRed
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { customer.currentPatience },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = patienceColor,
                    trackColor = Color.White.copy(alpha = 0.1f),
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "סבלנות:",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${(customer.currentPatience * 100).toInt()}%",
                        color = patienceColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ORDER REQUIREMENT DISPLAY TAGS
            Text(
                text = "הזמנה:",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right
            )
            
            Spacer(modifier = Modifier.height(2.dp))

            // Row displaying food tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                customer.requiredOrder.forEach { ingredient ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .background(Color(ingredient.colorHex).copy(alpha = 0.25f), CircleShape)
                            .border(1.dp, Color(ingredient.colorHex), CircleShape)
                            .size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ingredient.emoji,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// Sub-Component: Visual 3D Assembly Area (The Pita pocket builder with interactive Drag and Drop system)
@Composable
fun PitaAssemblyWorkspace(
    preparedIngredients: List<Ingredient>,
    onIngredientAdded: (Ingredient) -> Unit
) {
    // Tracking drag state
    var draggingIng by remember { mutableStateOf<Ingredient?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val isDraggingActive = draggingIng != null

    // Glow scale for drop target when dragging
    val targetPulse = rememberInfiniteTransition(label = "TargetPulse")
    val borderAlpha by targetPulse.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Glow"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // DROP TARGET: Beautiful golden steel serving plate with curved pita
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (isDraggingActive) {
                        FalafelRushTheme.NeonCyan.copy(alpha = 0.08f)
                    } else {
                        Color.Transparent
                    }
                )
                .border(
                    2.dp,
                    if (isDraggingActive) {
                        FalafelRushTheme.NeonCyan.copy(alpha = borderAlpha)
                    } else {
                        Color.White.copy(alpha = 0.08f)
                    },
                    RoundedCornerShape(20.dp)
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background target message when dragging
            if (isDraggingActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .background(FalafelRushTheme.NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .border(1.dp, FalafelRushTheme.NeonCyan, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🎯 שחרר כאן להוספה מנצחת!",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Central active pita container
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(width = 240.dp, height = 135.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Curved custom 3D Pita Wrapper base drawn using Canvas offsets
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Drawing beautiful outer pita pocket skin
                        drawArc(
                            color = Color(0xFFD7CCC8),
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(10f, canvasHeight - 110f),
                            size = androidx.compose.ui.geometry.Size(canvasWidth - 20f, 100f)
                        )

                        // Overlay golden toasted gradient inside pocket
                        drawArc(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFE5C185), Color(0xFFBCAAA4))
                            ),
                            startAngle = 0f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(16f, canvasHeight - 104f),
                            size = androidx.compose.ui.geometry.Size(canvasWidth - 32f, 90f)
                        )
                    }

                    // Inner stack layout populated dynamically!
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy((-8).dp) // overlapping items
                    ) {
                        if (preparedIngredients.isEmpty()) {
                            Text(
                                text = "התחל פיתה חדשה! 🫓\nמשוך/לחץ פיתה מלמטה לשולחן",
                                color = Color.White.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 15.dp)
                            )
                        } else {
                            // Display stacked ingredient layers
                            preparedIngredients.forEachIndexed { idx, ing ->
                                key(idx) {
                                    Row(
                                        modifier = Modifier
                                            .background(Color(ing.colorHex).copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                                            .border(1.5.dp, Color(ing.colorHex).lighten(0.3f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = ing.emoji,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = ing.displayName,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // INPUT BINS/PANTRY: Horizontal counter with 6 steel trays containing products
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .padding(6.dp)
        ) {
            Text(
                text = "⚡ גרור מרכיב למעלה לשולחן כדי להוסיף, או לחץ מהיר:",
                color = FalafelRushTheme.BrightGold,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )

            // Grid of trays (3 columns, 2 rows)
            val rows = Ingredient.values().toList().chunked(3)
            rows.forEach { rowIngredients ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowIngredients.forEach { ing ->
                        // Determine if addition is allowed (pita must be active unless adding pita itself)
                        val isPitaActive = preparedIngredients.contains(Ingredient.PITA)
                        val allowed = ing == Ingredient.PITA || isPitaActive

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (allowed) Color(0xFF231F32) else Color.Gray.copy(alpha = 0.2f)
                                )
                                .border(
                                    1.dp,
                                    if (allowed) Color(ing.colorHex).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable(enabled = allowed) {
                                    onIngredientAdded(ing)
                                }
                                .pointerInput(allowed, ing) {
                                    if (!allowed) return@pointerInput
                                    detectDragGestures(
                                        onDragStart = {
                                            draggingIng = ing
                                            dragOffset = Offset.Zero
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount
                                        },
                                        onDragEnd = {
                                            // Trigger drop if dragged up significantly (negative Y)
                                            if (dragOffset.y < -120f) {
                                                onIngredientAdded(ing)
                                            }
                                            draggingIng = null
                                            dragOffset = Offset.Zero
                                        },
                                        onDragCancel = {
                                            draggingIng = null
                                            dragOffset = Offset.Zero
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = ing.emoji,
                                    fontSize = 18.sp,
                                    modifier = Modifier.scale(if (draggingIng == ing) 1.25f else 1.0f)
                                )
                                Text(
                                    text = ing.displayName,
                                    color = if (allowed) Color.White else Color.White.copy(alpha = 0.3f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }

    // GHOST FLOAT: Floating overlay emoji following user cursor/touch while active dragging
    if (draggingIng != null) {
        val density = LocalDensity.current
        val ing = draggingIng!!
        val floatX = with(density) { dragOffset.x.toDp() }
        val floatY = with(density) { dragOffset.y.toDp() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .offset(x = floatX, y = floatY)
                    .align(Alignment.Center)
                    .shadow(12.dp, CircleShape)
                    .background(Color(ing.colorHex).copy(alpha = 0.9f), CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .size(54.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = ing.emoji, fontSize = 28.sp)
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

// SCREEN 3: DAY SUMMARY LEDGER & REVIEWS
@Composable
fun DaySummaryScreen(viewModel: GameViewModel, state: GameUiState) {
    var logsTickerIndex by remember { mutableStateOf(0) }
    
    val funnyLoadingPhrases = listOf(
        "קוצץ עגבניות בצורה קוונטית...",
        "מזהה שאריות טחינה על הגג...",
        "مחשב מדד תקינות פיתות לפי חוקי הפיזיקה...",
        "ממיין ביקורות של אנשים קשוחים...",
        "מתדיין אסטרונומית עם משרדי כשרות...",
        "מעדכן קצב טיגון כדורים בבריכת שמן..."
    )

    LaunchedEffect(state.generatingReviewsWithGemini) {
        if (state.generatingReviewsWithGemini) {
            while (true) {
                delay(800)
                logsTickerIndex = (logsTickerIndex + 1) % funnyLoadingPhrases.size
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "סיכום יום מספר ${state.saveState.currentDay - 1}",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(
                        color = FalafelRushTheme.NeonCyan,
                        offset = Offset(0f, 4f),
                        blurRadius = 0f
                    )
                )
            )
            
            Text(
                text = "פרנסה של כבוד מדוכן פלאפל רחוב",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Financial ledger card
        item {
            ThreeDStaticCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0xFFF7F4EB), // retro yellow organic paper receipts style
                borderColor = FalafelRushTheme.DeepGold
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🧾 דו\"ח רווחי הדוכן",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LedgerRow("לקוחות שסונג'רו בהצלחה:", "🧆 ${state.servedCountToday}", Color.Black)
                    LedgerRow("מנות שנזרקו באדישות לפח:", "🗑️ ${state.failedCountToday}", FalafelRushTheme.CrimsonRed)
                    LedgerRow("לקוחות שהתעצבנו ועזבו קשר עין:", "😡 ${state.impatientLeftToday}", FalafelRushTheme.HotOrange)
                    
                    Divider(color = Color.Black.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "הכנסות יומיות ברוטו:",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "+${state.revenueEarnedToday} 🪙",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "הכסף נגרס ונשמר בקופה בבטחה!",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Live Reviews area
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💬 ביקורות גוגל מפות שנוצרו ברשת:",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    textAlign = TextAlign.Right
                )

                if (state.generatingReviewsWithGemini) {
                    ThreeDStaticCard(
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        backgroundColor = FalafelRushTheme.GlassCardBg
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = FalafelRushTheme.NeonCyan)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = funnyLoadingPhrases[logsTickerIndex],
                                color = FalafelRushTheme.NeonCyan,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "מפעיל אינטליגנציה מלאכותית Google Gemini ⚡",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    // Loaded Reviews list
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (state.generatedTodayReviews.isEmpty()) {
                            Text(
                                text = "אין ביקורות להצגה היום.",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        } else {
                            state.generatedTodayReviews.forEach { review ->
                                ReviewCardItem(review)
                            }
                        }
                    }
                }
            }
        }

        // NEXT DAY BUTTONS FOOTER
        item {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Bouncy3DButton(
                    onClick = { viewModel.setScreen(GameScreen.UPGRADES) },
                    backgroundColor = FalafelRushTheme.DeepGold,
                    height = 54.dp,
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "שדרג ציוד / הכנסות לקראת מחר 🛠️",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                Bouncy3DButton(
                    onClick = { viewModel.startNewDay() },
                    backgroundColor = FalafelRushTheme.GlowGreen,
                    height = 58.dp,
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "פתח יום חדש: יום מספר ${state.saveState.currentDay}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun LedgerRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Black.copy(alpha = 0.7f), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun ReviewCardItem(review: CustomerReview) {
    ThreeDStaticCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = FalafelRushTheme.GlassCardBg,
        borderColor = Color.White.copy(alpha = 0.1f)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.customerName,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
                
                // Show stars row using alpha shading instead of custom StarBorder (completely compiling-safe)
                Row {
                    repeat(review.rating) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = FalafelRushTheme.DeepGold,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    repeat(5 - review.rating) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = review.comment,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// SCREEN 4: EQUIPMENT & OPERATIONS UPGRADES
@Composable
fun UpgradesScreen(viewModel: GameViewModel, state: GameUiState) {
    val speedCost = state.saveState.speedUpgradeLevel * 45 + 20
    val priceCost = state.saveState.priceUpgradeLevel * 60 + 35
    val patienceCost = state.saveState.patienceUpgradeLevel * 50 + 25
    val marketingCost = state.saveState.marketingUpgradeLevel * 80 + 50
    val robotCost = 150

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "מחסן שדרוגים 🛠️",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(
                        color = FalafelRushTheme.DeepGold,
                        offset = Offset(0f, 4f),
                        blurRadius = 0f
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "שדרג מהירות, רכיבים ושיווק קהל להכנסות שיא",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        // Live Cash stats panel
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(
                        Brush.horizontalGradient(
                            listOf(FalafelRushTheme.GlassCardBg, FalafelRushTheme.DeepBlue)
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .border(2.dp, FalafelRushTheme.NeonCyan, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💵 קופה נוכחית:",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    
                    Text(
                        text = "${state.saveState.totalCoins} מטבעות",
                        color = FalafelRushTheme.GlowGreen,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
            }
        }

        // Upgrade item 1: SPEED
        item {
            UpgradeCardWrapper(
                title = "⚡ מהירות מיקסר פלאפל",
                desc = "מאיץ הכנה! מוזיל כדורי פלאפל ומפחית שגיאות בהכנה מהירה במטבח",
                level = state.saveState.speedUpgradeLevel,
                cost = speedCost,
                currentCoins = state.saveState.totalCoins,
                onBuyClicked = { viewModel.purchaseUpgrade("SPEED") }
            )
        }

        // Upgrade item 2: PRICE
        item {
            UpgradeCardWrapper(
                title = "🎁 פיתת פרימיום משודרגת",
                desc = "פותח סודיות במרכיבים! מעלה משמעותית את שווי הבסיס של כל מנה בדוכן (+4 🪙 לרמה)",
                level = state.saveState.priceUpgradeLevel,
                cost = priceCost,
                currentCoins = state.saveState.totalCoins,
                onBuyClicked = { viewModel.purchaseUpgrade("PRICE") }
            )
        }

        // Upgrade item 3: PATIENCE
        item {
            UpgradeCardWrapper(
                title = "🧘 סבלנות ברזל ללקוחות",
                desc = "הלקוחות נהיים שלווים ורגועים יותר! מספק ומאריך את זמן ההמתנה ב-+20% ברחוב",
                level = state.saveState.patienceUpgradeLevel,
                cost = patienceCost,
                currentCoins = state.saveState.totalCoins,
                onBuyClicked = { viewModel.purchaseUpgrade("PATIENCE") }
            )
        }

        // Upgrade item 4: MARKETING
        item {
            UpgradeCardWrapper(
                title = "📣 קמפיין טיק-טוק ורשתות",
                desc = "מביא קונים עשירים ופילנתרופים! מגדיל אקראיות של טיפים גדולים ומושך לקוחות VIP חדשים",
                level = state.saveState.marketingUpgradeLevel,
                cost = marketingCost,
                currentCoins = state.saveState.totalCoins,
                onBuyClicked = { viewModel.purchaseUpgrade("MARKETING") }
            )
        }

        // Upgrade item 5: AUTO_SAUCE
        item {
            val owned = state.saveState.autoSauceUpgradeLevel > 0
            UpgradeCardWrapper(
                title = "🤖 מתקן פטנט טחינה אוטומטית",
                desc = "מפזר טחינה באופן מכני בכל פלאפל שהכנת, חוסך זמן ונפח לחיצות יקר!",
                level = state.saveState.autoSauceUpgradeLevel,
                cost = robotCost,
                currentCoins = state.saveState.totalCoins,
                onBuyClicked = { viewModel.purchaseUpgrade("AUTO_SAUCE") },
                maxOut = owned
            )
        }

        // FOOTER RETURN ACTIONS
        item {
            Bouncy3DButton(
                onClick = { viewModel.setScreen(GameScreen.START_SCREEN) },
                backgroundColor = FalafelRushTheme.NeonCyan,
                height = 56.dp,
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(0.85f)
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "חזור לתפריט הראשי",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun UpgradeCardWrapper(
    title: String,
    desc: String,
    level: Int,
    cost: Int,
    currentCoins: Int,
    onBuyClicked: () -> Unit,
    maxOut: Boolean = false
) {
    ThreeDStaticCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = FalafelRushTheme.GlassCardBg,
        borderColor = Color.White.copy(alpha = 0.08f)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (maxOut) "רמה מקסימלית" else "רמה נוכחית: $level",
                        color = FalafelRushTheme.NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                
                // Show current levels dots progress bars
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    val filledDots = if (maxOut) 5 else level.coerceIn(1, 5)
                    repeat(filledDots) {
                        Box(
                            modifier = Modifier
                                .background(FalafelRushTheme.NeonCyan, CircleShape)
                                .size(8.dp)
                        )
                    }
                    repeat(5 - filledDots) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                .size(8.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = desc,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!maxOut) {
                    val canAfford = currentCoins >= cost
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "עלות השדרוג:",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$cost 🪙",
                            color = if (canAfford) FalafelRushTheme.GlowGreen else FalafelRushTheme.CrimsonRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Bouncy3DButton(
                        onClick = onBuyClicked,
                        backgroundColor = if (canAfford) FalafelRushTheme.GlowGreen else Color.Gray,
                        height = 38.dp,
                        modifier = Modifier.width(100.dp)
                    ) {
                        Text(
                            text = "רכוש ✨",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                } else {
                    Text(
                        text = "נרכש בעלים! מקסימלי 🏆",
                        color = FalafelRushTheme.DeepGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// SCREEN 5: HISTORIC REVIEWS SCROLLER
@Composable
fun HistoricReviewsScreen(viewModel: GameViewModel, state: GameUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            Text(
                text = "ארכיון ביקורות 🗃️",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                style = TextStyle(
                    shadow = Shadow(
                        color = FalafelRushTheme.NeonCyan,
                        offset = Offset(0f, 4f),
                        blurRadius = 0f
                    )
                )
            )
            
            Text(
                text = "כאן נשמרים הלוגים וביקורות של כל ימי העבודה",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            if (state.allHistoryReviews.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "טרם התקבלו ביקורות. פתח את הדוכן וסיים יום עבודה כדי לצבור לוגים! 🧆",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.allHistoryReviews) { review ->
                        ReviewCardItem(review)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Bouncy3DButton(
            onClick = { viewModel.setScreen(GameScreen.START_SCREEN) },
            backgroundColor = FalafelRushTheme.NeonCyan,
            height = 54.dp,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "חזור לתפריט",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}
