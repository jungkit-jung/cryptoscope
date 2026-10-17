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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryptoscope.app.core.database.CopyTradeEntity
import com.cryptoscope.app.core.database.SmartWalletEntity
import com.cryptoscope.app.presentation.components.ScoreBadge
import com.cryptoscope.app.presentation.theme.CryptoGold
import com.cryptoscope.app.presentation.theme.CryptoGreen
import com.cryptoscope.app.presentation.theme.CryptoRed
import com.cryptoscope.app.presentation.theme.DarkBackground
import com.cryptoscope.app.presentation.theme.DarkCard
import com.cryptoscope.app.presentation.theme.TextPrimary
import com.cryptoscope.app.presentation.theme.TextSecondary

@Composable
fun DashboardScreen(
    followedWallets: List<SmartWalletEntity>,
    openTrades: List<CopyTradeEntity>,
    onSelectTrade: (CopyTradeEntity) -> Unit,
    onNavigateToDiscover: () -> Unit,
    onResetTrades: () -> Unit
) {
    val totalEquity = 10000.0 + openTrades.sumOf { it.pnlUsd }
    val totalPnl = openTrades.sumOf { it.pnlUsd }
    val totalPnlPct = if (openTrades.isEmpty()) 0.0 else (totalPnl / 10000.0) * 100.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner: Virtual Paper Portfolio
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VIRTUAL PAPER PORTFOLIO",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(CryptoGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("SIMULATOR ACTIVE", color = CryptoGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$${String.format("%,.2f", totalEquity)}",
                        color = TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val pnlSign = if (totalPnl >= 0) "+$" else "-$"
                    val pctSign = if (totalPnlPct >= 0) "+%" else ""
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (openTrades.isEmpty()) "$0.00 (0.00%) P&L" else "$pnlSign${String.format("%,.2f", kotlin.math.abs(totalPnl))} (${String.format("%.2f", totalPnlPct)}%) P&L",
                            color = if (totalPnl >= 0) CryptoGreen else CryptoRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (openTrades.isNotEmpty()) {
                            androidx.compose.material3.TextButton(
                                onClick = onResetTrades,
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text("Reset Portfolio", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // 2. Active Defense Status
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131A26), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = CryptoGreen)
                Column {
                    Text("7-Layer Anti-Trap Guard Active", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Auto-screening tokens & whale traps before copying.", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        // 3. Followed Smart Wallets Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("FOLLOWED SMART WALLETS", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${followedWallets.size} Active", color = CryptoGold, fontSize = 12.sp)
            }
        }

        items(followedWallets.take(3)) { wallet ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(wallet.label, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF242C3D), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(wallet.chain, color = Color.LightGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Win Rate: ${wallet.winRate}%  •  Profit Factor: ${wallet.profitFactor}x",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    ScoreBadge(score = wallet.score, category = wallet.category)
                }
            }
        }

        // 4. Active Copied Trades (Tap to view interactive chart)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("COPIED TRADES (TAP TO VIEW CHART)", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${openTrades.size} Open", color = CryptoGreen, fontSize = 12.sp)
            }
        }

        if (openTrades.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = CryptoGold,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Portofolio Bersih ($10,000)",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Belum ada trade yang di-copy. Jelajahi Smart Wallet untuk mulai paper-trading otomatis tanpa risiko modal riil.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        androidx.compose.material3.Button(
                            onClick = onNavigateToDiscover,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = CryptoGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cari Smart Wallet untuk Di-Copy", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            items(openTrades) { trade ->
                Card(
                    onClick = { onSelectTrade(trade) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(trade.tokenSymbol, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("via ${trade.walletLabel.take(15)}...", color = TextSecondary, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Entry: $${trade.entryPrice}  ➔  Now: $${trade.currentPrice}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val isWin = trade.pnlUsd >= 0
                            Text(
                                text = if (isWin) "+$${String.format("%.2f", trade.pnlUsd)}" else "-$${String.format("%.2f", kotlin.math.abs(trade.pnlUsd))}",
                                color = if (isWin) CryptoGreen else CryptoRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (isWin) "+${String.format("%.1f", trade.pnlPercent)}%" else "${String.format("%.1f", trade.pnlPercent)}%",
                                color = if (isWin) CryptoGreen else CryptoRed,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
