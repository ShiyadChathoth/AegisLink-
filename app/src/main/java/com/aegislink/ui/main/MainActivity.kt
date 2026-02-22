package com.aegislink.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aegislink.AegisApplication
import com.aegislink.R
import com.aegislink.data.db.SettingsEntity
import com.aegislink.databinding.ActivityMainBinding
import com.aegislink.ui.auth.LoginActivity
import com.aegislink.ui.settings.SettingsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var defaultPromptShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = (application as AegisApplication).container.db

        lifecycleScope.launch {
            val blocked = withContext(Dispatchers.IO) { db.statsDao().getCount("blocked") ?: 0L }
            val trusted = withContext(Dispatchers.IO) { db.statsDao().getCount("trusted") ?: 0L }
            val unknown = withContext(Dispatchers.IO) { db.statsDao().getCount("unknown") ?: 0L }
            binding.tvStats.text = getString(
                com.aegislink.R.string.stats_summary,
                blocked,
                trusted,
                unknown
            )
        }

        lifecycleScope.launch {
            val isEnabled = withContext(Dispatchers.IO) { db.settingsDao().get("enabled") != "false" }
            binding.switchEnabled.isChecked = isEnabled
            maybePromptSetDefault(isEnabled)
        }

        binding.switchEnabled.setOnCheckedChangeListener { _, checked ->
            lifecycleScope.launch(Dispatchers.IO) {
                db.settingsDao().upsert(SettingsEntity("enabled", checked.toString()))
            }
            maybePromptSetDefault(checked)
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun maybePromptSetDefault(interceptionEnabled: Boolean) {
        if (!interceptionEnabled || defaultPromptShown || isDefaultLinkHandler()) return

        defaultPromptShown = true
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.default_app_prompt_title))
            .setMessage(getString(R.string.default_app_prompt_message))
            .setPositiveButton(getString(R.string.default_app_prompt_action)) { _, _ ->
                openDefaultAppSettings()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun isDefaultLinkHandler(): Boolean {
        val testIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com")).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }
        val resolved = packageManager.resolveActivity(testIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
            ?: return false
        return resolved.activityInfo?.packageName == packageName
    }

    private fun openDefaultAppSettings() {
        val appSettingsIntent = Intent(
            Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
            Uri.parse("package:$packageName")
        )
        val fallbackIntent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        runCatching { startActivity(appSettingsIntent) }
            .onFailure { runCatching { startActivity(fallbackIntent) } }
    }
}
