package com.aegislink.data.repo

import android.content.Context
import com.aegislink.data.db.BlacklistEntity
import com.aegislink.data.db.WhitelistEntity
import com.aegislink.data.db.AegisDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ListRepository(private val context: Context, private val db: AegisDatabase) {

    suspend fun isBlacklisted(domain: String): BlacklistEntity? = db.blacklistDao().find(domain)

    suspend fun isWhitelisted(domain: String): WhitelistEntity? = db.whitelistDao().find(domain)

    suspend fun addWhitelist(domain: String) {
        db.whitelistDao().insert(WhitelistEntity(domain = domain))
    }

    suspend fun addBlacklist(domain: String, reason: String = "Manually added by user") {
        db.blacklistDao().insert(BlacklistEntity(domain = domain, reason = reason))
    }

    suspend fun removeWhitelist(domain: String): Boolean {
        return db.whitelistDao().deleteByDomain(domain) > 0
    }

    suspend fun removeBlacklist(domain: String): Boolean {
        return db.blacklistDao().deleteByDomain(domain) > 0
    }

    suspend fun listWhitelist(limit: Int = 100): List<WhitelistEntity> {
        return db.whitelistDao().list(limit)
    }

    suspend fun listBlacklist(limit: Int = 100): List<BlacklistEntity> {
        return db.blacklistDao().list(limit)
    }

    suspend fun seedBlacklistIfNeeded() = withContext(Dispatchers.IO) {
        val seeded = db.settingsDao().get(KEY_SEEDED) == "true"
        if (seeded) return@withContext

        val lines = context.assets.open("blacklist_seed.txt")
            .bufferedReader()
            .use { it.readLines() }
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("#") }

        val entries = lines.map { BlacklistEntity(domain = it) }
        db.blacklistDao().insertAll(entries)
        db.settingsDao().upsert(com.aegislink.data.db.SettingsEntity(KEY_SEEDED, "true"))
    }

    companion object {
        private const val KEY_SEEDED = "blacklist_seeded"
    }
}
