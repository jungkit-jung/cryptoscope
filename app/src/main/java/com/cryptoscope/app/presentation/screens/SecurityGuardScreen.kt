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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
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
import com.cryptoscope.app.core.database.TrapLogEntity
import com.cryptoscope.app.presentation.theme.CryptoGold
import com.cryptoscope.app.presentation.theme.CryptoGreen
import com.cryptoscope.app.presentation.theme.CryptoRed
import com.cryptoscope.app.presentation.theme.DarkBackground
import com.cryptoscope.app.presentation.theme.DarkCard
import com.cryptoscope.app.presentation.theme.TextPrimary
import com.cryptoscope.app.presentation.theme.TextSecondary

@Composable
fun SecurityGuardScreen(
    trapLogs: List<TrapLogEntity>
) {
    val totalSaved = trapLogs.sumOf { it.estimatedLossAvoided }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Banner
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
                        Text("ANTI-TRAP DEFENSE STATUS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Security, contentDescription = null, tint = CryptoGreen)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("PROTECTED", color = CryptoGreen, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text("All 7 security layers active against whale baiting bots.", color = TextSecondary, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("TRAPS BLOCKED", color = TextSecondary, fontSize = 10.sp)
                            Text("${trapLogs.size}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Column {
                            Text("CAPITAL SAVED", color = TextSecondary, fontSize = 10.sp)
                            Text("+$${String.format("%,.2f", totalSaved)}", color = CryptoGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        // 2. Active 7-Layer Matrix
        item {
            Text("ACTIVE DEFENSE MATRIX", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val layers = listOf(
                        "Layer 1: Pre-Copy Checklist (Liquidity >= $50K, Age >= 24h)",
                        "Layer 2: Liquidity Trap & Market Impact (< 2%)",
                        "Layer 3: Wallet Anomaly (Spike Size / Bait-and-Switch)",
                        "Layer 4: Contract Security (Honeypot, Mint, Blacklist)",
                        "Layer 5: Coordination & Sybil Cluster Detection",
                        "Layer 6: Post-Copy Monitor (Whale Early Dump < 5m)",
                        "Layer 7: Historical Pattern Learning & Auto-Blacklist"
                    )
                    layers.forEach { layer ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CryptoGreen)
                            Text(layer, color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 3. Trap Intercept Logs
        item {
            Text("RECENT TRAPS INTERCEPTED", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        items(trapLogs) { log ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Block, contentDescription = null, tint = CryptoRed)
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${log.tokenSymbol} — ${log.trapType}", color = CryptoRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("+$${log.estimatedLossAvoided.toInt()} Saved", color = CryptoGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(log.reason, color = TextPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Wallet: ${log.walletAddress.take(12)}...", color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
