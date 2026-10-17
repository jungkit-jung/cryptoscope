package com.cryptoscope.app.domain.copytrade

import com.cryptoscope.app.core.database.CopyTradeEntity
import com.cryptoscope.app.domain.antitrap.AntiTrapGuard
import com.cryptoscope.app.domain.antitrap.TokenSecurityCheck
import java.util.UUID

object PaperTradingSimulator {
    const val STARTING_BALANCE_USD = 10_000.0
    const val ALLOCATION_PER_TRADE_USD = 500.0

    fun getInitialTrades(): List<CopyTradeEntity> {
        // Start fresh with zero active trades so user begins cleanly
        return emptyList()
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
