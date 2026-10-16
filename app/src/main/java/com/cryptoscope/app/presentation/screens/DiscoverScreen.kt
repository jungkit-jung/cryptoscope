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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun DiscoverScreen(
    wallets: List<SmartWalletEntity>,
    onToggleFollow: (SmartWalletEntity) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredWallets = remember(wallets, selectedFilter) {
        when (selectedFilter) {
            "ELITE" -> wallets.filter { it.score >= 80 && !it.isBlacklisted }
            "GOOD" -> wallets.filter { it.score in 60..79 && !it.isBlacklisted }
            "TRAPS" -> wallets.filter { it.isBlacklisted }
            else -> wallets
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        Text("DISCOVER SMART WALLETS", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text("On-Chain Smart Money & Trap Filter", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val filters = listOf("ALL", "ELITE", "GOOD", "TRAPS")
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (filter == "TRAPS") CryptoRed.copy(alpha = 0.2f) else CryptoGreen.copy(alpha = 0.2f),
                        selectedLabelColor = if (filter == "TRAPS") CryptoRed else CryptoGreen,
                        containerColor = DarkCard,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredWallets) { wallet ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(wallet.label, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF242C3D), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(wallet.chain, color = Color.LightGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            ScoreBadge(score = wallet.score, category = wallet.category)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${wallet.address.take(10)}...${wallet.address.takeLast(6)}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (wallet.isBlacklisted) {
                            // Display Trap Warning Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CryptoRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = CryptoRed)
                                    Column {
                                        Text("WHALE TRAP DETECTED", color = CryptoRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(wallet.blacklistReason ?: "Malicious behavior detected.", color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }
                        } else {
                            // Display Normal Performance Metrics
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("WIN RATE", color = TextSecondary, fontSize = 10.sp)
                                    Text("${wallet.winRate}%", color = CryptoGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Column {
                                    Text("PROFIT FACTOR", color = TextSecondary, fontSize = 10.sp)
                                    Text("${wallet.profitFactor}x", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Column {
                                    Text("TOTAL TRADES", color = TextSecondary, fontSize = 10.sp)
                                    Text("${wallet.totalTrades}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Column {
                                    Text("REALIZED P&L", color = TextSecondary, fontSize = 10.sp)
                                    Text("+$${wallet.totalPnlUsd.toInt()}", color = CryptoGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Follow / Unfollow Button
                            Button(
                                onClick = { onToggleFollow(wallet) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (wallet.isFollowed) Color(0xFF242C3D) else CryptoGreen
                                )
                            ) {
                                if (wallet.isFollowed) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = CryptoGreen)
                                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                    Text("FOLLOWING (AUTO-COPY ACTIVE)", color = CryptoGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                } else {
                                    Text("+ FOLLOW THIS WALLET", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
