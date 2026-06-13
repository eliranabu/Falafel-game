package com.eliranabu.falafelrush.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// SCREEN 3: DAY SUMMARY LEDGER & REVIEWS
@Composable
fun DaySummaryScreen(viewModel: GameViewModel, state: GameUiState) {
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

            // Star rating vs the daily goal + perfect-day badge
            Text(
                text = "★".repeat(state.dayStars) + "☆".repeat(3 - state.dayStars),
                color = FalafelRushTheme.DeepGold,
                fontSize = 30.sp,
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            Text(
                text = if (state.dayStars == 0) "לא הגעת ליעד היומי 😞"
                else if (state.impatientLeftToday == 0) "🌟 יום מושלם! בונוס +50% · רצף נקי: ${state.saveState.cleanStreak}"
                else "יעד הושג! ${state.dayStars} כוכבים",
                color = if (state.dayStars == 0) FalafelRushTheme.HotOrange else FalafelRushTheme.GlowGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
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
                    text = "💬 ביקורות הלקוחות של היום:",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    textAlign = TextAlign.Right
                )

                // Reviews list
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
