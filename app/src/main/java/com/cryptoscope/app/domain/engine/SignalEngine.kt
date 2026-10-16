package com.cryptoscope.app.domain.engine

import kotlin.math.pow
import kotlin.math.sqrt

data class SignalResult(
    val score: Int, // 0 - 100
    val label: String, // STRONG_BUY, BUY, NEUTRAL, SELL, STRONG_SELL
    val rsi: Double,
    val macdStatus: String,
    val bbStatus: String,
    val emaStatus: String,
    val summary: String
)

object SignalEngine {

    /**
     * Calculates Relative Strength Index (RSI) for a list of close prices.
     */
    fun calculateRSI(prices: List<Double>, period: Int = 14): Double {
        if (prices.size <= period) return 50.0

        var gains = 0.0
        var losses = 0.0

        for (i in 1..period) {
            val change = prices[i] - prices[i - 1]
            if (change >= 0) gains += change else losses += -change
        }

        var avgGain = gains / period
        var avgLoss = losses / period

        for (i in (period + 1) until prices.size) {
            val change = prices[i] - prices[i - 1]
            val gain = if (change > 0) change else 0.0
            val loss = if (change < 0) -change else 0.0

            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period
        }

        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100.0 - (100.0 / (1.0 + rs))
    }

    /**
     * Calculates Exponential Moving Average (EMA).
     */
    fun calculateEMA(prices: List<Double>, period: Int): List<Double> {
        if (prices.size < period) return emptyList()

        val multiplier = 2.0 / (period + 1)
        val emaList = mutableListOf<Double>()

        // Initial SMA as starting point
        val initialSma = prices.take(period).average()
        emaList.add(initialSma)

        for (i in period until prices.size) {
            val currentEma = (prices[i] - emaList.last()) * multiplier + emaList.last()
            emaList.add(currentEma)
        }
        return emaList
    }

    /**
     * Calculates Bollinger Bands (Upper, Middle, Lower).
     */
    fun calculateBollingerBands(prices: List<Double>, period: Int = 20, numStdDev: Double = 2.0): Triple<Double, Double, Double> {
        if (prices.size < period) {
            val p = prices.lastOrNull() ?: 0.0
            return Triple(p * 1.05, p, p * 0.95)
        }

        val slice = prices.takeLast(period)
        val sma = slice.average()
        val variance = slice.map { (it - sma).pow(2) }.average()
        val stdDev = sqrt(variance)

        val upper = sma + (stdDev * numStdDev)
        val lower = sma - (stdDev * numStdDev)
        return Triple(upper, sma, lower)
    }

    /**
     * Evaluates comprehensive technical score (0 - 100).
     */
    fun evaluate(prices: List<Double>): SignalResult {
        if (prices.size < 20) {
            return SignalResult(
                score = 50,
                label = "NEUTRAL",
                rsi = 50.0,
                macdStatus = "Insufficient Data",
                bbStatus = "Neutral",
                emaStatus = "Neutral",
                summary = "Collecting price history for indicators."
            )
        }

        val currentPrice = prices.last()
        val rsi = calculateRSI(prices, 14)
        val (bbUpper, bbMiddle, bbLower) = calculateBollingerBands(prices, 20)
        val ema20 = calculateEMA(prices, 20).lastOrNull() ?: currentPrice
        val ema50 = calculateEMA(prices, 50).lastOrNull() ?: currentPrice

        var score = 50.0

        // 1. RSI Factor (Weight: 25%)
        val rsiScore = when {
            rsi < 30.0 -> 90.0 // Strong Oversold -> Buy opportunity
            rsi in 30.0..45.0 -> 75.0
            rsi in 45.0..55.0 -> 50.0
            rsi in 55.0..70.0 -> 35.0
            else -> 15.0 // Overbought -> Sell danger
        }
        score += (rsiScore - 50.0) * 0.25

        // 2. Bollinger Bands Factor (Weight: 25%)
        val bbScore = when {
            currentPrice <= bbLower -> 85.0 // Rebound from lower band
            currentPrice >= bbUpper -> 20.0 // Rejection near upper band
            currentPrice > bbMiddle -> 60.0
            else -> 40.0
        }
        score += (bbScore - 50.0) * 0.25

        // 3. EMA Trend Factor (Weight: 30%)
        val emaScore = when {
            currentPrice > ema20 && ema20 > ema50 -> 85.0 // Bullish Golden Alignment
            currentPrice > ema20 -> 65.0
            currentPrice < ema20 && ema20 < ema50 -> 20.0 // Bearish Death Alignment
            else -> 45.0
        }
        score += (emaScore - 50.0) * 0.30

        // Clamp to 0..100
        val finalScore = score.toInt().coerceIn(0, 100)

        val label = when {
            finalScore >= 80 -> "STRONG_BUY"
            finalScore >= 60 -> "BUY"
            finalScore in 40..59 -> "NEUTRAL"
            finalScore in 20..39 -> "SELL"
            else -> "STRONG_SELL"
        }

        val macdDesc = if (ema20 > ema50) "Bullish Trend" else "Bearish Trend"
        val bbDesc = if (currentPrice < bbMiddle) "Near Lower Support" else "Near Upper Resistance"
        val emaDesc = if (currentPrice > ema20) "Above EMA20" else "Below EMA20"

        val summary = when (label) {
            "STRONG_BUY" -> "High confluence: RSI oversold ($rsi), price at BB support, bullish momentum."
            "BUY" -> "Favorable entry setup with positive momentum and moderate RSI ($rsi)."
            "NEUTRAL" -> "Consolidating in range. Awaiting directional breakout."
            "SELL" -> "Bearish momentum or overbought RSI ($rsi). Consider taking profit."
            else -> "Heavy rejection risk. Capital preservation advised."
        }

        return SignalResult(
            score = finalScore,
            label = label,
            rsi = String.format("%.1f", rsi).toDoubleOrNull() ?: rsi,
            macdStatus = macdDesc,
            bbStatus = bbDesc,
            emaStatus = emaDesc,
            summary = summary
        )
    }
}
