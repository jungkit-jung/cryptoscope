package com.cryptoscope.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryptoscope.app.core.llm.ChatMessage
import com.cryptoscope.app.core.llm.CustomLlmClient
import com.cryptoscope.app.core.llm.LlmConfigManager
import kotlinx.coroutines.launch
import com.cryptoscope.app.core.database.CopyTradeEntity
import com.cryptoscope.app.domain.engine.SignalEngine
import com.cryptoscope.app.presentation.components.CandleBar
import com.cryptoscope.app.presentation.components.LiveCandlestickChart
import com.cryptoscope.app.presentation.theme.CryptoGold
import com.cryptoscope.app.presentation.theme.CryptoGreen
import com.cryptoscope.app.presentation.theme.CryptoRed
import com.cryptoscope.app.presentation.theme.DarkBackground
import com.cryptoscope.app.presentation.theme.DarkCard
import com.cryptoscope.app.presentation.theme.TextPrimary
import com.cryptoscope.app.presentation.theme.TextSecondary
import kotlin.random.Random

@Composable
fun TradeChartDetailScreen(
    trade: CopyTradeEntity,
    onBack: () -> Unit,
    onCloseTrade: (CopyTradeEntity) -> Unit
) {
    // Generate realistic price action candles leading up to entry and current price
    val candles = remember(trade.id) {
        val list = mutableListOf<CandleBar>()
        var price = trade.entryPrice * 0.94
        val rnd = Random(trade.id.hashCode())

        // 20 historical candles before entry
        for (i in 0 until 18) {
            val delta = (rnd.nextDouble() - 0.47) * (price * 0.02)
            val open = price
            val close = (open + delta).coerceAtLeast(0.000001)
            val high = maxOf(open, close) + (rnd.nextDouble() * price * 0.01)
            val low = minOf(open, close) - (rnd.nextDouble() * price * 0.01)
            list.add(CandleBar(open, high, low, close, volume = rnd.nextDouble() * 500 + 100))
            price = close
        }

        // Candle representing entry
        list.add(
            CandleBar(
                open = trade.entryPrice * 0.99,
                high = trade.entryPrice * 1.015,
                low = trade.entryPrice * 0.985,
                close = trade.entryPrice,
                volume = 850.0
            )
        )

        // 12 candles following entry towards current price
        val steps = 12
        val priceDiff = (trade.currentPrice - trade.entryPrice) / steps
        for (i in 1..steps) {
            val noise = (rnd.nextDouble() - 0.48) * (trade.currentPrice * 0.015)
            val open = list.last().close
            val close = if (i == steps) trade.currentPrice else (open + priceDiff + noise).coerceAtLeast(0.000001)
            val high = maxOf(open, close) + (rnd.nextDouble() * close * 0.008)
            val low = minOf(open, close) - (rnd.nextDouble() * close * 0.008)
            list.add(CandleBar(open, high, low, close, volume = rnd.nextDouble() * 400 + 200))
        }
        list
    }

    val technicals = remember(candles) {
        SignalEngine.evaluate(candles.map { it.close })
    }

    val isProfit = trade.pnlUsd >= 0
    val pnlColor = if (isProfit) CryptoGreen else CryptoRed

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${trade.tokenSymbol}/USDT",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF242C3D), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(trade.status, color = if (trade.status == "OPEN") CryptoGreen else CryptoGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = "Copied from: ${trade.walletLabel}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // 2. Price & PnL Summary Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text("CURRENT PRICE", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "$${String.format("%.6f", trade.currentPrice)}",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("UNREALIZED P&L", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "${if (isProfit) "+$" else "-$"}${String.format("%.2f", kotlin.math.abs(trade.pnlUsd))} (${String.format("%.2f", trade.pnlPercent)}%)",
                    color = pnlColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 3. The Interactive Candlestick Chart with Entry & Exit Markers!
        LiveCandlestickChart(
            candles = candles,
            entryPrice = trade.entryPrice,
            currentPrice = trade.currentPrice,
            exitPrice = trade.exitPrice,
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Position & Performance Details
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("TRADE METRICS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("🟢 Entry Price (Bought here)", color = TextSecondary, fontSize = 13.sp)
                    Text("$${String.format("%.6f", trade.entryPrice)}", color = CryptoGreen, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("📍 Current Price", color = TextSecondary, fontSize = 13.sp)
                    Text("$${String.format("%.6f", trade.currentPrice)}", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("💰 Position Value", color = TextSecondary, fontSize = 13.sp)
                    val currentValue = trade.virtualAmountUsd + trade.pnlUsd
                    Text("$${trade.virtualAmountUsd.toInt()} ➔ $${String.format("%.2f", currentValue)}", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("🛡️ Whale Guard Status", color = TextSecondary, fontSize = 13.sp)
                    Text("Whale Still Holding ✅", color = CryptoGreen, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Technical Indicators Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("LIVE TECHNICAL INDICATORS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Signal Score", color = TextSecondary, fontSize = 13.sp)
                    Text("${technicals.score}/100 (${technicals.label})", color = CryptoGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("RSI (14)", color = TextSecondary, fontSize = 13.sp)
                    Text("${technicals.rsi}", color = TextPrimary, fontSize = 13.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Trend (EMA 20/50)", color = TextSecondary, fontSize = 13.sp)
                    Text(technicals.emaStatus, color = TextPrimary, fontSize = 13.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Bollinger Bands", color = TextSecondary, fontSize = 13.sp)
                    Text(technicals.bbStatus, color = TextPrimary, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5b. AI Custom LLM Quick Analyst
        val context = androidx.compose.ui.platform.LocalContext.current
        val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
        var aiAnalysisText by remember { mutableStateOf<String?>(null) }
        var isAnalyzing by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161E2E))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(androidx.compose.material.icons.Icons.Default.AutoAwesome, contentDescription = null, tint = CryptoGreen, modifier = Modifier.size(16.dp))
                        Text("AI COPILOT ANALYST", color = CryptoGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    if (isAnalyzing) {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(14.dp), color = CryptoGreen, strokeWidth = 2.dp)
                    }
                }

                if (aiAnalysisText == null && !isAnalyzing) {
                    Text(
                        "Dapatkan analisis mendalam dari Custom LLM untuk token ${trade.tokenSymbol} berdasarkan RSI, Trend EMA, dan posisi entry saat ini.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            isAnalyzing = true
                            aiAnalysisText = null
                            coroutineScope.launch {
                                val config = com.cryptoscope.app.core.llm.LlmConfigManager.getConfig(context)
                                val prompt = "Tolong analisis token ${trade.tokenSymbol}. " +
                                        "Data saat ini: Entry Price: $${trade.entryPrice}, Current Price: $${trade.currentPrice}, " +
                                        "PnL: ${trade.pnlPercent}%, RSI(14): ${technicals.rsi}, Trend: ${technicals.emaStatus}, " +
                                        "Bollinger Bands: ${technicals.bbStatus}, Signal Score: ${technicals.score}/100. " +
                                        "Apakah sebaiknya Take Profit, Hold, atau Cut Loss? Berikan analisa ringkas dan padat."
                                val result = com.cryptoscope.app.core.llm.CustomLlmClient.sendChat(
                                    listOf(com.cryptoscope.app.core.llm.ChatMessage("user", prompt)),
                                    config
                                )
                                isAnalyzing = false
                                result.onSuccess { reply ->
                                    aiAnalysisText = reply
                                }.onFailure { err ->
                                    aiAnalysisText = "⚠️ Gagal menghubungi LLM: ${err.localizedMessage}. Pastikan server LLM aktif di menu AI Copilot."
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = CryptoGreen)
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Minta Analisis AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isAnalyzing) {
                    Text("Menghubungi Custom LLM dan menganalisis chart...", color = TextSecondary, fontSize = 12.sp)
                } else {
                    androidx.compose.foundation.text.selection.SelectionContainer {
                        Text(
                            text = aiAnalysisText.orEmpty(),
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                    androidx.compose.material3.TextButton(
                        onClick = { aiAnalysisText = null },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text("Tutup Analisis", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 6. Action Button: Manual Close (Paper)
        if (trade.status == "OPEN") {
            Button(
                onClick = { onCloseTrade(trade) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3242))
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.LightGray)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("Close Position Now (Paper Sim)", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
