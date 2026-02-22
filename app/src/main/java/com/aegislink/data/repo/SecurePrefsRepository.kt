package com.aegislink.data.repo

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePrefsRepository(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "aegis_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun setVirusTotalApiKey(apiKey: String) {
        prefs.edit().putString(KEY_VT_API, apiKey).apply()
    }

    fun getVirusTotalApiKey(): String? {
        val stored = prefs.getString(KEY_VT_API, null)?.trim().orEmpty()
        return stored.ifEmpty { null }
    }

    companion object {
        private const val KEY_VT_API = "virustotal_api_key"
    }
}
