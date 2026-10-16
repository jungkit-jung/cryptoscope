package com.cryptoscope.app.domain.discovery

import com.cryptoscope.app.core.database.SmartWalletEntity

object SmartWalletDiscoveryEngine {

    /**
     * Seeds initial curated smart wallets and discovered blacklist traps for testing and initial boot.
     */
    fun getInitialSmartWallets(): List<SmartWalletEntity> {
        return listOf(
            SmartWalletEntity(
                address = "0xA3f829B838029d91C9B76A2b109D9455ec308c2",
                chain = "ETH",
                label = "Alpha Swapper #1",
                score = 92,
                category = "ELITE",
                winRate = 78.5,
                profitFactor = 3.2,
                totalPnlUsd = 48230.0,
                totalTrades = 142,
                avgHoldingHours = 48.0,
                avgTradeSize = 2500.0,
                trustScore = 98.0,
                isFollowed = true
            ),
            SmartWalletEntity(
                address = "0x7Bf91244e8C59218F83Ac321aB87C21449b1a9",
                chain = "ETH",
                label = "DeFi Early Accumulator",
                score = 87,
                category = "ELITE",
                winRate = 71.2,
                profitFactor = 2.8,
                totalPnlUsd = 31100.0,
                totalTrades = 89,
                avgHoldingHours = 72.0,
                avgTradeSize = 3200.0,
                trustScore = 94.0,
                isFollowed = true
            ),
            SmartWalletEntity(
                address = "0xC129487b33C87a718D8319aF919293121094e7",
                chain = "SOL",
                label = "Solana Momentum Hunter",
                score = 76,
                category = "GOOD",
                winRate = 65.0,
                profitFactor = 2.4,
                totalPnlUsd = 19450.0,
                totalTrades = 110,
                avgHoldingHours = 24.0,
                avgTradeSize = 1800.0,
                trustScore = 86.0,
                isFollowed = false
            ),
            SmartWalletEntity(
                address = "0x44D912b719329C3a58F2834bB1902834019488",
                chain = "BSC",
                label = "PancakeSwap Swing Trader",
                score = 68,
                category = "GOOD",
                winRate = 61.8,
                profitFactor = 2.1,
                totalPnlUsd = 12800.0,
                totalTrades = 64,
                avgHoldingHours = 36.0,
                avgTradeSize = 1200.0,
                trustScore = 80.0,
                isFollowed = false
            ),
            // Blacklisted Trap Wallets (Whale Trap Examples filtered out)
            SmartWalletEntity(
                address = "0xDEAD9928198302187319A82194821938102391",
                chain = "ETH",
                label = "Bait & Dump Bot (Blacklisted)",
                score = 15,
                category = "REJECTED",
                winRate = 22.0,
                profitFactor = 0.4,
                totalPnlUsd = -8400.0,
                totalTrades = 51,
                avgHoldingHours = 0.2,
                avgTradeSize = 15000.0,
                trustScore = 12.0,
                isFollowed = false,
                isBlacklisted = true,
                blacklistReason = "Rug Pull / Bait-and-switch pattern detected. Dumps within 3 minutes of copy."
            ),
            SmartWalletEntity(
                address = "0xBAD10293184918239019283910283019283019",
                chain = "BSC",
                label = "Wash Trading Sybil Cluster",
                score = 20,
                category = "REJECTED",
                winRate = 18.0,
                profitFactor = 0.3,
                totalPnlUsd = -14000.0,
                totalTrades = 120,
                avgHoldingHours = 0.5,
                avgTradeSize = 5000.0,
                trustScore = 5.0,
                isFollowed = false,
                isBlacklisted = true,
                blacklistReason = "Wash trading across 8 linked addresses with identical funding source."
            )
        )
    }

    /**
     * Scores an arbitrary wallet candidate (0 - 100) based on mathematical performance & anti-trap safety.
     */
    fun scoreWallet(
        winRate: Double,
        profitFactor: Double,
        totalTrades: Int,
        activeMonths: Double,
        hasCriticalTrapFlag: Boolean
    ): Int {
        if (hasCriticalTrapFlag) return 10 // Immediate rejection

        var score = 0.0

        // 1. Win Rate (30%)
        val wrScore = (winRate / 100.0).coerceIn(0.0, 1.0) * 100.0
        score += wrScore * 0.30

        // 2. Profit Factor (25%) -> 1.0 = 50, 2.0 = 75, 3.0+ = 100
        val pfScore = when {
            profitFactor >= 3.0 -> 100.0
            profitFactor >= 2.0 -> 80.0
            profitFactor >= 1.5 -> 65.0
            profitFactor >= 1.0 -> 50.0
            else -> 20.0
        }
        score += pfScore * 0.25

        // 3. Trade Experience / Sample size (20%)
        val expScore = when {
            totalTrades >= 100 -> 100.0
            totalTrades >= 50 -> 80.0
            totalTrades >= 20 -> 60.0
            else -> 30.0
        }
        score += expScore * 0.20

        // 4. Track Record Longevity (15%)
        val ageScore = when {
            activeMonths >= 6.0 -> 100.0
            activeMonths >= 3.0 -> 75.0
            else -> 40.0
        }
        score += ageScore * 0.15

        // 5. Anti-Trap Cleanliness (10%)
        score += 10.0

        return score.toInt().coerceIn(0, 100)
    }
}
