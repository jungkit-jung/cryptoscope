package com.cryptoscope.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryptoscope.app.presentation.theme.CryptoGold
import com.cryptoscope.app.presentation.theme.CryptoGreen
import com.cryptoscope.app.presentation.theme.CryptoRed

@Composable
fun ScoreBadge(
    score: Int,
    category: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when {
        category == "REJECTED" || score < 40 -> Triple(
            CryptoRed.copy(alpha = 0.15f),
            CryptoRed,
            "TRAP: $score"
        )
        score >= 80 -> Triple(
            CryptoGold.copy(alpha = 0.15f),
            CryptoGold,
            "🏆 ELITE $score"
        )
        score >= 60 -> Triple(
            CryptoGreen.copy(alpha = 0.15f),
            CryptoGreen,
            "🟢 GOOD $score"
        )
        else -> Triple(
            Color.Gray.copy(alpha = 0.15f),
            Color.LightGray,
            "🟡 MID $score"
        )
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
