package com.eliranabu.falafelrush.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
