package com.eliranabu.falafelrush.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
