package com.cryptoscope.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smart_wallets")
data class SmartWalletEntity(
    @PrimaryKey val address: String,
    val chain: String, // ETH, BSC, SOL, POLYGON
    val label: String,
    val score: Int, // 0 - 100
    val category: String, // ELITE, GOOD, AVERAGE, REJECTED
    val winRate: Double, // e.g. 78.5%
    val profitFactor: Double, // e.g. 3.2
    val totalPnlUsd: Double,
    val totalTrades: Int,
    val avgHoldingHours: Double,
    val avgTradeSize: Double,
    val trustScore: Double = 100.0,
    val isFollowed: Boolean = false,
    val isBlacklisted: Boolean = false,
    val blacklistReason: String? = null,
    val lastActiveAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "copy_trades")
data class CopyTradeEntity(
    @PrimaryKey val id: String,
    val walletAddress: String,
    val walletLabel: String,
    val tokenAddress: String,
    val tokenSymbol: String,
    val entryPrice: Double,
    val currentPrice: Double,
    val exitPrice: Double? = null,
    val virtualAmountUsd: Double = 500.0,
    val pnlUsd: Double = 0.0,
    val pnlPercent: Double = 0.0,
    val status: String, // OPEN, CLOSED_PROFIT, CLOSED_STOP_LOSS, EMERGENCY_EXIT
    val entryTimestamp: Long = System.currentTimeMillis(),
    val exitTimestamp: Long? = null,
    val exitReason: String? = null,
    val signalScoreAtEntry: Int = 75
)

@Entity(tableName = "trap_logs")
data class TrapLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val walletAddress: String,
    val tokenAddress: String,
    val tokenSymbol: String,
    val trapType: String, // HONEYPOT, WASH_TRADING, RUG_PULL, PUMP_AND_DUMP, SYBIL, LOW_LIQUIDITY
    val severity: String, // CRITICAL, WARNING
    val estimatedLossAvoided: Double,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tokens")
data class TokenPriceEntity(
    @PrimaryKey val symbol: String,
    val name: String,
    val address: String,
    val chain: String,
    val priceUsd: Double,
    val change24h: Double,
    val high24h: Double,
    val low24h: Double,
    val volume24h: Double,
    val rsi14: Double = 50.0,
    val signalScore: Int = 50,
    val lastUpdated: Long = System.currentTimeMillis()
)
