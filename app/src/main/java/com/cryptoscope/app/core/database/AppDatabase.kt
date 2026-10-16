package com.cryptoscope.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SmartWalletEntity::class,
        CopyTradeEntity::class,
        TrapLogEntity::class,
        TokenPriceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun smartWalletDao(): SmartWalletDao
    abstract fun copyTradeDao(): CopyTradeDao
    abstract fun trapLogDao(): TrapLogDao
    abstract fun tokenPriceDao(): TokenPriceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cryptoscope.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
