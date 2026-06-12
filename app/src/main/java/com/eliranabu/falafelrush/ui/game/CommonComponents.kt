package com.eliranabu.falafelrush.ui.game

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eliranabu.falafelrush.audio.SoundId
import com.eliranabu.falafelrush.data.database.CustomerReview

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
    val soundManager = LocalSoundManager.current

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
                onClick = {
                    soundManager?.play(SoundId.BUTTON_TICK)
                    onClick()
                }
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
