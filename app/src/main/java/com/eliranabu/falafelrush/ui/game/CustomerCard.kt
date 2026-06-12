package com.eliranabu.falafelrush.ui.game

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
