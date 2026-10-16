package com.cryptoscope.app

import android.app.Application
import com.cryptoscope.app.core.database.AppDatabase
import com.cryptoscope.app.core.database.TrapLogEntity
import com.cryptoscope.app.domain.copytrade.PaperTradingSimulator
import com.cryptoscope.app.domain.discovery.SmartWalletDiscoveryEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CryptoScopeApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()

        // Seed initial data asynchronously on first run
        CoroutineScope(Dispatchers.IO).launch {
            val walletDao = database.smartWalletDao()
            val tradeDao = database.copyTradeDao()
            val trapDao = database.trapLogDao()

            // Seed Wallets
            val initialWallets = SmartWalletDiscoveryEngine.getInitialSmartWallets()
            walletDao.insertWallets(initialWallets)

            // Seed Initial Active Copy Trades
            if (tradeDao.getOpenCount() == 0) {
                PaperTradingSimulator.getInitialTrades().forEach { trade ->
                    tradeDao.insertTrade(trade)
                }
            }

            // Seed Sample Intercepted Traps
            if (trapDao.getTotalTrapsCount() == 0) {
                trapDao.insertTrapLog(
                    TrapLogEntity(
                        walletAddress = "0xDEAD9928198302187319A82194821938102391",
                        tokenAddress = "0x1111029102910291029102910291029102910291",
                        tokenSymbol = "MOONPUMP",
                        trapType = "LOW_LIQUIDITY_DUMP",
                        severity = "CRITICAL",
                        estimatedLossAvoided = 500.0,
                        reason = "Liquidity pool was only $8,200. Whale dumped entire supply in 2 minutes."
                    )
                )
                trapDao.insertTrapLog(
                    TrapLogEntity(
                        walletAddress = "0xBAD10293184918239019283910283019283019",
                        tokenAddress = "0x2222029102910291029102910291029102910292",
                        tokenSymbol = "SAFEETH",
                        trapType = "HONEYPOT_TAX",
                        severity = "CRITICAL",
                        estimatedLossAvoided = 500.0,
                        reason = "Contract sell tax was modified to 99%. Wallet blacklisted permanently."
                    )
                )
                trapDao.insertTrapLog(
                    TrapLogEntity(
                        walletAddress = "0xDEAD9928198302187319A82194821938102391",
                        tokenAddress = "0x3333029102910291029102910291029102910293",
                        tokenSymbol = "DEFIYIELD",
                        trapType = "MINT_EXPLOIT",
                        severity = "CRITICAL",
                        estimatedLossAvoided = 500.0,
                        reason = "Uncapped hidden mint function detected. Prevented 100% loss."
                    )
                )
            }
        }
    }
}
