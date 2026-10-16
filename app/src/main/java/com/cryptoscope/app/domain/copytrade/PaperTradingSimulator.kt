package com.cryptoscope.app.domain.copytrade

import com.cryptoscope.app.core.database.CopyTradeEntity
import com.cryptoscope.app.domain.antitrap.AntiTrapGuard
import com.cryptoscope.app.domain.antitrap.TokenSecurityCheck
import java.util.UUID

object PaperTradingSimulator {
    const val STARTING_BALANCE_USD = 10_000.0
    const val ALLOCATION_PER_TRADE_USD = 500.0

    fun getInitialTrades(): List<CopyTradeEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            CopyTradeEntity(
                id = UUID.randomUUID().toString(),
                walletAddress = "0xA3f829B838029d91C9B76A2b109D9455ec308c2",
                walletLabel = "Alpha Swapper #1 (Score: 92)",
                tokenAddress = "0x6982508145454ce325ddbe47a25d4ec3d2311933",
                tokenSymbol = "PEPE",
                entryPrice = 0.0000089,
                currentPrice = 0.0000124,
                virtualAmountUsd = 500.0,
                pnlUsd = 196.63,
                pnlPercent = 39.3,
                status = "OPEN",
                entryTimestamp = now - (14 * 3600 * 1000),
                signalScoreAtEntry = 85
            ),
            CopyTradeEntity(
                id = UUID.randomUUID().toString(),
                walletAddress = "0x7Bf91244e8C59218F83Ac321aB87C21449b1a9",
                walletLabel = "DeFi Early Accumulator (Score: 87)",
                tokenAddress = "0x912ce59144191c1204e64559fe8253a0e49e6548",
                tokenSymbol = "ARB",
                entryPrice = 1.12,
                currentPrice = 1.08,
                virtualAmountUsd = 500.0,
                pnlUsd = -17.85,
                pnlPercent = -3.6,
                status = "OPEN",
                entryTimestamp = now - (32 * 3600 * 1000),
                signalScoreAtEntry = 72
            ),
            CopyTradeEntity(
                id = UUID.randomUUID().toString(),
                walletAddress = "0xC129487b33C87a718D8319aF919293121094e7",
                walletLabel = "Solana Momentum Hunter (Score: 76)",
                tokenAddress = "0x1b19482194819041289412841928419284192841",
                tokenSymbol = "ONDO",
                entryPrice = 0.74,
                currentPrice = 0.83,
                virtualAmountUsd = 500.0,
                pnlUsd = 60.81,
                pnlPercent = 12.16,
                status = "OPEN",
                entryTimestamp = now - (8 * 3600 * 1000),
                signalScoreAtEntry = 78
            ),
            // Example of a Closed Win
            CopyTradeEntity(
                id = UUID.randomUUID().toString(),
                walletAddress = "0xA3f829B838029d91C9B76A2b109D9455ec308c2",
                walletLabel = "Alpha Swapper #1",
                tokenAddress = "0x4444508145454ce325ddbe47a25d4ec3d2311933",
                tokenSymbol = "WIF",
                entryPrice = 1.45,
                currentPrice = 2.85,
                exitPrice = 2.85,
                virtualAmountUsd = 500.0,
                pnlUsd = 482.75,
                pnlPercent = 96.55,
                status = "CLOSED_PROFIT",
                entryTimestamp = now - (7 * 86400 * 1000),
                exitTimestamp = now - (5 * 86400 * 1000),
                exitReason = "Whale exited position with +96.5% gain.",
                signalScoreAtEntry = 88
            )
        )
    }

    /**
     * Attempts to simulate a copy-trade.
     * Evaluates through AntiTrapGuard first.
     */
    fun attemptCopyTrade(
        walletAddress: String,
        walletLabel: String,
        token: TokenSecurityCheck,
        entryPrice: Double,
        signalScore: Int
    ): Pair<CopyTradeEntity?, String> {
        val verdict = AntiTrapGuard.evaluateToken(token)
        if (!verdict.isSafeToCopy) {
            return null to "BLOCKED BY ANTI-TRAP: ${verdict.reason}"
        }

        val trade = CopyTradeEntity(
            id = UUID.randomUUID().toString(),
            walletAddress = walletAddress,
            walletLabel = walletLabel,
            tokenAddress = token.tokenAddress,
            tokenSymbol = token.tokenSymbol,
            entryPrice = entryPrice,
            currentPrice = entryPrice,
            virtualAmountUsd = ALLOCATION_PER_TRADE_USD,
            status = "OPEN",
            signalScoreAtEntry = signalScore
        )
        return trade to "Paper trade successfully placed for ${token.tokenSymbol}!"
    }
}
