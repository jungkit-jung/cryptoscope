package com.cryptoscope.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SmartWalletDao {
    @Query("SELECT * FROM smart_wallets ORDER BY score DESC")
    fun getAllWallets(): Flow<List<SmartWalletEntity>>

    @Query("SELECT * FROM smart_wallets WHERE isFollowed = 1 ORDER BY score DESC")
    fun getFollowedWallets(): Flow<List<SmartWalletEntity>>

    @Query("SELECT * FROM smart_wallets WHERE isBlacklisted = 1")
    fun getBlacklistedWallets(): Flow<List<SmartWalletEntity>>

    @Query("SELECT * FROM smart_wallets WHERE address = :address LIMIT 1")
    suspend fun getWallet(address: String): SmartWalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallets(wallets: List<SmartWalletEntity>)

    @Update
    suspend fun updateWallet(wallet: SmartWalletEntity)

    @Query("UPDATE smart_wallets SET isFollowed = :followed WHERE address = :address")
    suspend fun setFollowed(address: String, followed: Boolean)

    @Query("UPDATE smart_wallets SET isBlacklisted = 1, blacklistReason = :reason, isFollowed = 0 WHERE address = :address")
    suspend fun blacklistWallet(address: String, reason: String)
}

@Dao
interface CopyTradeDao {
    @Query("SELECT * FROM copy_trades ORDER BY entryTimestamp DESC")
    fun getAllTrades(): Flow<List<CopyTradeEntity>>

    @Query("SELECT * FROM copy_trades WHERE status = 'OPEN' ORDER BY entryTimestamp DESC")
    fun getOpenTrades(): Flow<List<CopyTradeEntity>>

    @Query("SELECT * FROM copy_trades WHERE id = :id LIMIT 1")
    suspend fun getTradeById(id: String): CopyTradeEntity?

    @Query("SELECT * FROM copy_trades WHERE status != 'OPEN' ORDER BY exitTimestamp DESC")
    fun getClosedTrades(): Flow<List<CopyTradeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: CopyTradeEntity)

    @Update
    suspend fun updateTrade(trade: CopyTradeEntity)

    @Query("SELECT COUNT(*) FROM copy_trades WHERE status = 'OPEN'")
    suspend fun getOpenCount(): Int
}

@Dao
interface TrapLogDao {
    @Query("SELECT * FROM trap_logs ORDER BY timestamp DESC")
    fun getAllTrapLogs(): Flow<List<TrapLogEntity>>

    @Insert
    suspend fun insertTrapLog(log: TrapLogEntity)

    @Query("SELECT COUNT(*) FROM trap_logs")
    suspend fun getTotalTrapsCount(): Int

    @Query("SELECT COALESCE(SUM(estimatedLossAvoided), 0.0) FROM trap_logs")
    suspend fun getTotalLossAvoided(): Double
}

@Dao
interface TokenPriceDao {
    @Query("SELECT * FROM tokens ORDER BY volume24h DESC")
    fun getAllTokens(): Flow<List<TokenPriceEntity>>

    @Query("SELECT * FROM tokens WHERE symbol = :symbol LIMIT 1")
    suspend fun getToken(symbol: String): TokenPriceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTokens(tokens: List<TokenPriceEntity>)

    @Update
    suspend fun updateToken(token: TokenPriceEntity)
}
