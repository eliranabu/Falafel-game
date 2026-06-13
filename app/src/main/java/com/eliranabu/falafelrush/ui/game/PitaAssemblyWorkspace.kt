package com.eliranabu.falafelrush.ui.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

    // One-shot success pulse: gold flash + scale pop whenever an ingredient lands
    val dropPulse = remember { Animatable(0f) }
    var lastIngredientCount by remember { mutableStateOf(preparedIngredients.size) }
    LaunchedEffect(preparedIngredients.size) {
        if (preparedIngredients.size > lastIngredientCount) {
            dropPulse.snapTo(1f)
            dropPulse.animateTo(0f, animationSpec = tween(280, easing = EaseOut))
        }
        lastIngredientCount = preparedIngredients.size
    }

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
                    when {
                        // Gold success flash takes priority — distinct from the cyan drag glow
                        dropPulse.value > 0.02f -> FalafelRushTheme.DeepGold.copy(alpha = dropPulse.value)
                        isDraggingActive -> FalafelRushTheme.NeonCyan.copy(alpha = borderAlpha)
                        else -> Color.White.copy(alpha = 0.08f)
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
                    modifier = Modifier
                        .size(width = 240.dp, height = 135.dp)
                        .scale(1f + dropPulse.value * 0.08f),
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
                text = "⚡ הוסף את מה שהלקוח ביקש (הפיתה כבר מוכנה):",
                color = FalafelRushTheme.BrightGold,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )

            // Grid of trays — fillings only; pita is an automatic base, always tappable
            val rows = Ingredient.values().filter { it != Ingredient.PITA }.chunked(3)
            rows.forEach { rowIngredients ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowIngredients.forEach { ing ->
                        val allowed = true

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
