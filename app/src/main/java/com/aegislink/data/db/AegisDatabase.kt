package com.aegislink.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BlacklistEntity::class, WhitelistEntity::class, SettingsEntity::class, StatEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AegisDatabase : RoomDatabase() {
    abstract fun blacklistDao(): BlacklistDao
    abstract fun whitelistDao(): WhitelistDao
    abstract fun settingsDao(): SettingsDao
    abstract fun statsDao(): StatsDao

    companion object {
        @Volatile
        private var INSTANCE: AegisDatabase? = null

        fun get(context: Context): AegisDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AegisDatabase::class.java,
                    "aegislink.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
