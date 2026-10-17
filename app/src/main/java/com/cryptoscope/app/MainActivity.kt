package com.cryptoscope.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.cryptoscope.app.core.database.CopyTradeEntity
import com.cryptoscope.app.presentation.screens.AiCopilotScreen
import com.cryptoscope.app.presentation.screens.DashboardScreen
import com.cryptoscope.app.presentation.screens.DiscoverScreen
import com.cryptoscope.app.presentation.screens.SecurityGuardScreen
import com.cryptoscope.app.presentation.screens.TradeChartDetailScreen
import com.cryptoscope.app.presentation.theme.CryptoGreen
import com.cryptoscope.app.presentation.theme.CryptoScopeTheme
import com.cryptoscope.app.presentation.theme.DarkBackground
import com.cryptoscope.app.presentation.theme.DarkSurface
import com.cryptoscope.app.presentation.theme.TextSecondary
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as CryptoScopeApp
        val database = app.database

        setContent {
            CryptoScopeTheme {
                MainContent(database)
            }
        }
    }
}

@Composable
fun MainContent(database: com.cryptoscope.app.core.database.AppDatabase) {
    val coroutineScope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf("DASHBOARD") }
    var selectedTradeForChart by remember { mutableStateOf<CopyTradeEntity?>(null) }

    val wallets by database.smartWalletDao().getAllWallets().collectAsState(initial = emptyList())
    val followedWallets by database.smartWalletDao().getFollowedWallets().collectAsState(initial = emptyList())
    val openTrades by database.copyTradeDao().getOpenTrades().collectAsState(initial = emptyList())
    val trapLogs by database.trapLogDao().getAllTrapLogs().collectAsState(initial = emptyList())

    // If user clicked on a trade to view chart, display the detailed chart screen
    selectedTradeForChart?.let { trade ->
        TradeChartDetailScreen(
            trade = trade,
            onBack = { selectedTradeForChart = null },
            onCloseTrade = { tradeToClose ->
                coroutineScope.launch {
                    val closed = tradeToClose.copy(
                        status = "CLOSED_PROFIT",
                        exitPrice = tradeToClose.currentPrice,
                        exitTimestamp = System.currentTimeMillis(),
                        exitReason = "Manual close (paper profit taken)."
                    )
                    database.copyTradeDao().updateTrade(closed)
                    selectedTradeForChart = null
                }
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface
            ) {
                NavigationBarItem(
                    selected = currentTab == "DASHBOARD",
                    onClick = { currentTab = "DASHBOARD" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CryptoGreen,
                        selectedTextColor = CryptoGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = CryptoGreen.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "DISCOVER",
                    onClick = { currentTab = "DISCOVER" },
                    icon = { Icon(Icons.Default.Explore, contentDescription = "Discover") },
                    label = { Text("Discover") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CryptoGreen,
                        selectedTextColor = CryptoGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = CryptoGreen.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "CHART",
                    onClick = {
                        val firstTrade = openTrades.firstOrNull()
                        if (firstTrade != null) {
                            selectedTradeForChart = firstTrade
                        } else {
                            // Default market view chart if no copy trades exist yet
                            selectedTradeForChart = CopyTradeEntity(
                                id = "market-btc",
                                walletAddress = "0xMARKET",
                                walletLabel = "Global Market Index",
                                tokenAddress = "0x2260fac5e5542a773aa44fbcfedf7c193bc2c599",
                                tokenSymbol = "BTC",
                                entryPrice = 67200.0,
                                currentPrice = 67890.0,
                                virtualAmountUsd = 500.0,
                                pnlUsd = 5.13,
                                pnlPercent = 1.02,
                                status = "MARKET_VIEW",
                                signalScoreAtEntry = 82
                            )
                        }
                    },
                    icon = { Icon(Icons.Default.CandlestickChart, contentDescription = "Live Chart") },
                    label = { Text("Charts") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CryptoGreen,
                        selectedTextColor = CryptoGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = CryptoGreen.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "GUARD",
                    onClick = { currentTab = "GUARD" },
                    icon = { Icon(Icons.Default.Security, contentDescription = "Anti-Trap") },
                    label = { Text("Guard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CryptoGreen,
                        selectedTextColor = CryptoGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = CryptoGreen.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == "COPILOT",
                    onClick = { currentTab = "COPILOT" },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Copilot") },
                    label = { Text("AI Copilot") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CryptoGreen,
                        selectedTextColor = CryptoGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = CryptoGreen.copy(alpha = 0.15f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "DASHBOARD" -> DashboardScreen(
                    followedWallets = followedWallets,
                    openTrades = openTrades,
                    onSelectTrade = { trade -> selectedTradeForChart = trade },
                    onNavigateToDiscover = { currentTab = "DISCOVER" },
                    onResetTrades = {
                        coroutineScope.launch {
                            database.copyTradeDao().deleteAllTrades()
                        }
                    }
                )
                "DISCOVER" -> DiscoverScreen(
                    wallets = wallets,
                    onToggleFollow = { wallet ->
                        coroutineScope.launch {
                            database.smartWalletDao().setFollowed(wallet.address, !wallet.isFollowed)
                        }
                    },
                    onCopyTradeFromWallet = { wallet ->
                        coroutineScope.launch {
                            val (symbol, price) = when (wallet.chain) {
                                "SOL" -> "SOL" to 154.20
                                "BASE" -> "AERO" to 1.18
                                "ARB" -> "ARB" to 0.58
                                else -> "ETH" to 2640.0
                            }
                            val newTrade = CopyTradeEntity(
                                id = java.util.UUID.randomUUID().toString(),
                                walletAddress = wallet.address,
                                walletLabel = wallet.label,
                                tokenAddress = "0x" + wallet.address.takeLast(40),
                                tokenSymbol = symbol,
                                entryPrice = price,
                                currentPrice = price,
                                virtualAmountUsd = 500.0,
                                pnlUsd = 0.0,
                                pnlPercent = 0.0,
                                status = "OPEN",
                                entryTimestamp = System.currentTimeMillis(),
                                signalScoreAtEntry = wallet.score
                            )
                            database.copyTradeDao().insertTrade(newTrade)
                            currentTab = "DASHBOARD"
                        }
                    }
                )
                "GUARD" -> SecurityGuardScreen(
                    trapLogs = trapLogs
                )
                "COPILOT" -> AiCopilotScreen(
                    database = database,
                    followedWallets = followedWallets,
                    openTrades = openTrades,
                    trapLogs = trapLogs
                )
            }
        }
    }
}
