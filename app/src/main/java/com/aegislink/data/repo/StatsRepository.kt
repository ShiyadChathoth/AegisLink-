package com.aegislink.data.repo

import com.aegislink.data.db.AegisDatabase
import com.aegislink.data.db.StatEntity

class StatsRepository(private val db: AegisDatabase) {
    suspend fun increment(key: String) {
        val current = db.statsDao().getCount(key) ?: 0L
        db.statsDao().upsert(StatEntity(key = key, count = current + 1))
    }

    suspend fun read(key: String): Long = db.statsDao().getCount(key) ?: 0L
}
