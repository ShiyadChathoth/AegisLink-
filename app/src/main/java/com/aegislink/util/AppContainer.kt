package com.aegislink.util

import android.content.Context
import com.aegislink.data.db.AegisDatabase
import com.aegislink.data.repo.ListRepository
import com.aegislink.data.repo.SecurePrefsRepository
import com.aegislink.data.repo.StatsRepository
import com.aegislink.domain.UrlClassifier
import com.aegislink.domain.UrlSanitizer
import com.aegislink.network.VirusTotalRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val db = AegisDatabase.get(appContext)
    val listRepository = ListRepository(appContext, db)
    val statsRepository = StatsRepository(db)
    val securePrefsRepository = SecurePrefsRepository(appContext)
    val virusTotalRepository = VirusTotalRepository()

    private val sanitizer: UrlSanitizer by lazy {
        val json = runCatching {
            appContext.assets.open("clearurls_rules.json").bufferedReader().use { it.readText() }
        }.getOrDefault("{}")
        UrlSanitizer.fromClearUrlsJson(json)
    }

    fun classifier() = UrlClassifier(sanitizer, listRepository)
}
