package com.cryptoscope.app.domain.antitrap

data class TokenSecurityCheck(
    val tokenAddress: String,
    val tokenSymbol: String,
    val liquidityUsd: Double,
    val tokenAgeHours: Double,
    val holderCount: Int,
    val buyTaxPercent: Double,
    val sellTaxPercent: Double,
    val isProxy: Boolean = false,
    val isMintable: Boolean = false,
    val hasBlacklist: Boolean = false,
    val isLiquidityLocked: Boolean = true,
    val top10HoldersPercent: Double = 35.0
)

data class GuardVerdict(
    val isSafeToCopy: Boolean,
    val reason: String,
    val trapType: String? = null,
    val severity: String = "CLEAN",
    val riskScore: Int = 0 // 0 = Safe, 100 = Guaranteed Trap
)

object AntiTrapGuard {
    const val MIN_LIQUIDITY_USD = 50_000.0
    const val MIN_TOKEN_AGE_HOURS = 24.0
    const val MIN_HOLDERS_COUNT = 100
    const val MAX_TAX_PERCENT = 10.0
    const val MAX_TOP_10_SUPPLY_PERCENT = 60.0

    /**
     * Layer 1 & 4: Evaluates token contract, liquidity, and security before copying.
     */
    fun evaluateToken(token: TokenSecurityCheck): GuardVerdict {
        // 1. Honeypot & Tax Trap Check
        if (token.sellTaxPercent > MAX_TAX_PERCENT) {
            return GuardVerdict(
                isSafeToCopy = false,
                reason = "Sell tax too high (${token.sellTaxPercent}%). Likely honeypot or malicious fee.",
                trapType = "HONEYPOT_TAX",
                severity = "CRITICAL",
                riskScore = 95
            )
        }

        // 2. Hidden Mint / Exploit Backdoor
        if (token.isMintable) {
            return GuardVerdict(
                isSafeToCopy = false,
                reason = "Uncapped mint function detected. Deployer can dilute supply 100%.",
                trapType = "MINT_EXPLOIT",
                severity = "CRITICAL",
                riskScore = 90
            )
        }

        // 3. Blacklist Trap (can freeze wallet funds)
        if (token.hasBlacklist) {
            return GuardVerdict(
                isSafeToCopy = false,
                reason = "Blacklist function present in contract. Wallet addresses can be banned from selling.",
                trapType = "BLACKLIST_TRAP",
                severity = "CRITICAL",
                riskScore = 85
            )
        }

        // 4. Low Liquidity Trap (< $50k)
        if (token.liquidityUsd < MIN_LIQUIDITY_USD) {
            return GuardVerdict(
                isSafeToCopy = false,
                reason = "Insufficient liquidity pool ($${token.liquidityUsd.toInt()}). Easily manipulated by whale dump.",
                trapType = "LOW_LIQUIDITY",
                severity = "CRITICAL",
                riskScore = 80
            )
        }

        // 5. Fresh Token / Rug Pull Window (< 24 hours)
        if (token.tokenAgeHours < MIN_TOKEN_AGE_HOURS) {
            return GuardVerdict(
                isSafeToCopy = false,
                reason = "Token age is only ${token.tokenAgeHours.toInt()}h. Most rug pulls occur in first 24h.",
                trapType = "NEW_TOKEN_RUG",
                severity = "WARNING",
                riskScore = 70
            )
        }

        // 6. Extreme Concentration (> 60% in top 10)
        if (token.top10HoldersPercent > MAX_TOP_10_SUPPLY_PERCENT) {
            return GuardVerdict(
                isSafeToCopy = false,
                reason = "Top 10 holders control ${token.top10HoldersPercent}% of total supply. Sybil dump danger.",
                trapType = "SYBIL_CONCENTRATION",
                severity = "WARNING",
                riskScore = 65
            )
        }

        // 7. Proxy Upgradeable Contract
        if (token.isProxy) {
            return GuardVerdict(
                isSafeToCopy = false,
                reason = "Contract is upgradeable via proxy. Logic can be altered after purchase.",
                trapType = "PROXY_BACKDOOR",
                severity = "WARNING",
                riskScore = 60
            )
        }

        return GuardVerdict(
            isSafeToCopy = true,
            reason = "Passed all 7 Anti-Trap security checks.",
            severity = "CLEAN",
            riskScore = 5
        )
    }

    /**
     * Layer 3: Wallet Anomaly Detection.
     * Detects if a historically good wallet is abruptly acting erratic (e.g. bait & switch).
     */
    fun evaluateWalletAnomaly(
        avgTradeSize: Double,
        currentTradeSize: Double,
        walletTrustScore: Double
    ): GuardVerdict {
        if (walletTrustScore < 40.0) {
            return GuardVerdict(
                isSafeToCopy = false,
                reason = "Wallet trust score dropped to ${walletTrustScore.toInt()}%. Auto-unfollow triggered.",
                trapType = "DECAYED_TRUST",
                severity = "CRITICAL",
                riskScore = 88
            )
        }

        // Trade size spike > 400%
        if (avgTradeSize > 0 && currentTradeSize > (avgTradeSize * 4.0)) {
            return GuardVerdict(
                isSafeToCopy = false,
                reason = "Anomaly: Current trade ($${currentTradeSize.toInt()}) is >4x wallet average ($${avgTradeSize.toInt()}). Possible bait-and-switch.",
                trapType = "BAIT_AND_SWITCH",
                severity = "WARNING",
                riskScore = 75
            )
        }

        return GuardVerdict(isSafeToCopy = true, reason = "Wallet behavior within normal parameters.", riskScore = 10)
    }

    /**
     * Layer 6: Post-Copy Emergency Exit Check.
     * Evaluates open position for sudden malicious action.
     */
    fun shouldEmergencyExit(
        entryPrice: Double,
        currentPrice: Double,
        holdingMinutes: Long,
        whaleDumpedWithin5Min: Boolean,
        liquidityDroppedPercent: Double
    ): Pair<Boolean, String> {
        // Source whale dumped in under 5 minutes -> immediate exit
        if (whaleDumpedWithin5Min) {
            return true to "Whale exited position within 5m. Emergency exit executed."
        }

        // Sudden liquidity rug pull (> 30% drop in liquidity)
        if (liquidityDroppedPercent >= 30.0) {
            return true to "Liquidity pool drained by ${liquidityDroppedPercent.toInt()}%. Emergency rug exit."
        }

        // Hard stop-loss at -15%
        val drawdownPercent = ((currentPrice - entryPrice) / entryPrice) * 100.0
        if (drawdownPercent <= -15.0) {
            return true to "Stop-loss reached (-15.0%). Safeguarding capital."
        }

        return false to ""
    }
}
