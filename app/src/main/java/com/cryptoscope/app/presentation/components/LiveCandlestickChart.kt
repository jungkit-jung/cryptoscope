package com.cryptoscope.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

data class CandleBar(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double = 100.0,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun LiveCandlestickChart(
    candles: List<CandleBar>,
    entryPrice: Double,
    currentPrice: Double,
    exitPrice: Double? = null,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(320.dp)
) {
    if (candles.isEmpty()) {
        Box(
            modifier = modifier.background(Color(0xFF131722)),
            contentAlignment = Alignment.Center
        ) {
            Text("Awaiting price stream...", color = Color.Gray)
        }
        return
    }

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .background(Color(0xFF131722))
            .padding(top = 16.dp, bottom = 24.dp, start = 8.dp, end = 64.dp)
    ) {
        val width = size.width
        val height = size.height

        // Calculate global min and max prices including entry and exit markers
        val candleMins = candles.minOf { it.low }
        val candleMaxs = candles.maxOf { it.high }
        val minPrice = min(candleMins, min(entryPrice, exitPrice ?: entryPrice)) * 0.99
        val maxPrice = max(candleMaxs, max(entryPrice, exitPrice ?: entryPrice)) * 1.01
        val priceRange = if (maxPrice - minPrice <= 0.0) 1.0 else maxPrice - minPrice

        fun priceToY(price: Double): Float {
            val fraction = (price - minPrice) / priceRange
            return (height - (fraction * height)).toFloat()
        }

        val candleWidth = width / candles.size
        val barWidth = candleWidth * 0.7f

        // 1. Draw P&L Shaded Zone between Entry Price and Current Price
        val entryY = priceToY(entryPrice)
        val currentY = priceToY(currentPrice)
        val isProfit = currentPrice >= entryPrice
        val zoneColor = if (isProfit) Color(0x3300C087) else Color(0x33FF3B30)

        drawRect(
            color = zoneColor,
            topLeft = Offset(0f, min(entryY, currentY)),
            size = Size(width, kotlin.math.abs(currentY - entryY))
        )

        // 2. Draw Candlesticks and Volume Bars
        val greenColor = Color(0xFF00C087)
        val redColor = Color(0xFFFF3B30)
        val maxVolume = candles.maxOfOrNull { it.volume }?.coerceAtLeast(1.0) ?: 1.0

        candles.forEachIndexed { index, candle ->
            val x = (index * candleWidth) + (candleWidth / 2f)
            val isBullish = candle.close >= candle.open
            val candleColor = if (isBullish) greenColor else redColor

            val openY = priceToY(candle.open)
            val closeY = priceToY(candle.close)
            val highY = priceToY(candle.high)
            val lowY = priceToY(candle.low)

            // Draw wick
            drawLine(
                color = candleColor,
                start = Offset(x, highY),
                end = Offset(x, lowY),
                strokeWidth = 1.5f
            )

            // Draw body
            val topBody = min(openY, closeY)
            val bodyHeight = max(2f, kotlin.math.abs(closeY - openY))
            drawRect(
                color = candleColor,
                topLeft = Offset(x - (barWidth / 2f), topBody),
                size = Size(barWidth, bodyHeight)
            )

            // Draw Volume Bar at bottom (max 40dp high)
            val volHeight = (candle.volume / maxVolume) * 40f
            drawRect(
                color = candleColor.copy(alpha = 0.35f),
                topLeft = Offset(x - (barWidth / 2f), height - volHeight.toFloat()),
                size = Size(barWidth, volHeight.toFloat())
            )
        }

        // 3. Draw ENTRY PRICE LINE & MARKER (Green dashed)
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
        drawLine(
            color = greenColor,
            start = Offset(0f, entryY),
            end = Offset(width, entryY),
            strokeWidth = 2f,
            pathEffect = dashEffect
        )

        // Entry Badge Label
        drawText(
            textMeasurer = textMeasurer,
            text = "🟢 ENTRY: ${String.format("%.6f", entryPrice)}",
            style = TextStyle(color = greenColor, fontSize = 11.sp),
            topLeft = Offset(8f, entryY - 20f)
        )

        // 4. Draw CURRENT LIVE PRICE LINE & MARKER
        drawLine(
            color = if (isProfit) greenColor else redColor,
            start = Offset(0f, currentY),
            end = Offset(width, currentY),
            strokeWidth = 2f
        )
        val pctChange = ((currentPrice - entryPrice) / entryPrice) * 100.0
        val pnlText = if (pctChange >= 0) "+${String.format("%.2f", pctChange)}%" else "${String.format("%.2f", pctChange)}%"
        drawText(
            textMeasurer = textMeasurer,
            text = "📍 LIVE: ${String.format("%.6f", currentPrice)} ($pnlText)",
            style = TextStyle(color = if (isProfit) greenColor else redColor, fontSize = 11.sp),
            topLeft = Offset(8f, currentY + 4f)
        )

        // 5. Draw EXIT PRICE MARKER if trade closed (Red dashed)
        exitPrice?.let { exit ->
            val exitY = priceToY(exit)
            drawLine(
                color = redColor,
                start = Offset(0f, exitY),
                end = Offset(width, exitY),
                strokeWidth = 2f,
                pathEffect = dashEffect
            )
            drawText(
                textMeasurer = textMeasurer,
                text = "🔴 EXIT: ${String.format("%.6f", exit)}",
                style = TextStyle(color = redColor, fontSize = 11.sp),
                topLeft = Offset(8f, exitY - 20f)
            )
        }
    }
}
