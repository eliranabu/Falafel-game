package com.eliranabu.falafelrush.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Full-screen pause overlay: freezes the day, offers resume / sound / exit
@Composable
fun PauseMenuOverlay(
    soundEnabled: Boolean,
    onResume: () -> Unit,
    onToggleSound: () -> Unit,
    onExitDay: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            // Consume taps so the game underneath can't be poked while paused
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {},
        contentAlignment = Alignment.Center
    ) {
        ThreeDStaticCard(
            modifier = Modifier.fillMaxWidth(0.82f),
            backgroundColor = FalafelRushTheme.DeepBlue.copy(alpha = 0.95f),
            borderColor = FalafelRushTheme.NeonCyan.copy(alpha = 0.6f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "⏸️", fontSize = 42.sp)
                Text(
                    text = "המשחק מושהה",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "הלקוחות מחכים בסבלנות (באופן חריג)...",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Bouncy3DButton(
                    onClick = onResume,
                    backgroundColor = FalafelRushTheme.GlowGreen,
                    height = 54.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "▶️ המשך משחק",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Bouncy3DButton(
                    onClick = onToggleSound,
                    backgroundColor = FalafelRushTheme.DeepGold,
                    height = 48.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (soundEnabled) "🔊 צלילים: פועלים" else "🔇 צלילים: כבויים",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                TextButton(onClick = onExitDay) {
                    Text(
                        text = "יציאה מהיום (ההתקדמות היומית תאבד) 🚪",
                        color = FalafelRushTheme.CrimsonRed.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private data class TutorialStep(val emoji: String, val title: String, val body: String)

// First-launch tutorial: 4 short Hebrew steps, game stays frozen until finished
@Composable
fun TutorialOverlay(onFinished: () -> Unit) {
    val steps = remember {
        listOf(
            TutorialStep(
                "🧎", "לקוחות מגיעים לתור",
                "כל לקוח מבקש פיתה עם מרכיבים מסוימים — תראה אותם בבועת הדיבור שלו. שים לב למד הסבלנות: כשהוא מתרוקן, הלקוח מתעצבן ועוזב!"
            ),
            TutorialStep(
                "🫓", "מרכיבים את הפיתה",
                "לחץ (או גרור למעלה) מרכיבים מהמגשים שלמטה. תמיד מתחילים מפיתה! טעית? אפשר לזרוק לפח ולהתחיל מחדש."
            ),
            TutorialStep(
                "🛎️", "מגישים ללקוח הראשון",
                "כשהמנה תואמת בדיוק את הבקשה — לחץ ׳הגש מנה׳. הגשה מהירה = בונוס! רצף הגשות מושלמות בונה קומבו ששווה נקודות."
            ),
            TutorialStep(
                "🛠️", "משדרגים ומתקדמים",
                "בסוף כל יום תקבל מטבעות וביקורות. השקע בשדרוגים — סבלנות, שיווק ומחירים — כדי לבנות אימפריית פלאפל אמיתית! 🌍"
            )
        )
    }

    var stepIndex by remember { mutableStateOf(0) }
    val step = steps[stepIndex]
    val isLast = stepIndex == steps.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {},
        contentAlignment = Alignment.Center
    ) {
        ThreeDStaticCard(
            modifier = Modifier.fillMaxWidth(0.86f),
            backgroundColor = FalafelRushTheme.DeepBlue.copy(alpha = 0.97f),
            borderColor = FalafelRushTheme.DeepGold.copy(alpha = 0.7f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = step.emoji, fontSize = 48.sp)
                Text(
                    text = step.title,
                    color = FalafelRushTheme.BrightGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = step.body,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp
                )

                // Step indicator dots
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    steps.indices.forEach { i ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (i == stepIndex) FalafelRushTheme.NeonCyan
                                    else Color.White.copy(alpha = 0.25f),
                                    CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Bouncy3DButton(
                    onClick = { if (isLast) onFinished() else stepIndex++ },
                    backgroundColor = if (isLast) FalafelRushTheme.GlowGreen else FalafelRushTheme.NeonCyan,
                    height = 52.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isLast) "יאללה, מתחילים! 🧆" else "הבא ⬅️",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                if (!isLast) {
                    TextButton(onClick = onFinished) {
                        Text(
                            text = "דלג על ההדרכה",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
