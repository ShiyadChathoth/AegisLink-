package com.aegislink.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aegislink.AegisApplication
import com.aegislink.R
import com.aegislink.data.db.SettingsEntity
import com.aegislink.databinding.ActivitySettingsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val container = (application as AegisApplication).container
        val repo = container.securePrefsRepository
        binding.etApiKey.setText(repo.getVirusTotalApiKey().orEmpty())
        lifecycleScope.launch {
            val pkg = withContext(Dispatchers.IO) {
                container.db.settingsDao().get("preferred_browser_pkg").orEmpty()
            }
            binding.etBrowserPackage.setText(pkg)
            refreshListPreviews()
        }

        binding.btnSave.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                repo.setVirusTotalApiKey(binding.etApiKey.text.toString().trim())
                container.db.settingsDao().upsert(
                    SettingsEntity("preferred_browser_pkg", binding.etBrowserPackage.text.toString().trim())
                )
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingsActivity, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        binding.btnAddWhitelist.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val domain = normalizeDomain(binding.etWhitelistDomain.text.toString())
                if (domain == null) {
                    showToastOnMain("Enter a valid domain for whitelist")
                    return@launch
                }
                container.listRepository.addWhitelist(domain)
                withContext(Dispatchers.Main) { binding.etWhitelistDomain.setText("") }
                refreshListPreviews()
                showToastOnMain("Whitelisted: $domain")
            }
        }

        binding.btnRemoveWhitelist.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val domain = normalizeDomain(binding.etWhitelistDomain.text.toString())
                if (domain == null) {
                    showToastOnMain("Enter a valid domain to remove from whitelist")
                    return@launch
                }
                val removed = container.listRepository.removeWhitelist(domain)
                refreshListPreviews()
                showToastOnMain(if (removed) "Removed from whitelist: $domain" else "Not found in whitelist")
            }
        }

        binding.btnAddBlacklist.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val domain = normalizeDomain(binding.etBlacklistDomain.text.toString())
                if (domain == null) {
                    showToastOnMain("Enter a valid domain for blacklist")
                    return@launch
                }
                container.listRepository.addBlacklist(domain)
                withContext(Dispatchers.Main) { binding.etBlacklistDomain.setText("") }
                refreshListPreviews()
                showToastOnMain("Blacklisted: $domain")
            }
        }

        binding.btnRemoveBlacklist.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val domain = normalizeDomain(binding.etBlacklistDomain.text.toString())
                if (domain == null) {
                    showToastOnMain("Enter a valid domain to remove from blacklist")
                    return@launch
                }
                val removed = container.listRepository.removeBlacklist(domain)
                refreshListPreviews()
                showToastOnMain(if (removed) "Removed from blacklist: $domain" else "Not found in blacklist")
            }
        }
    }

    private suspend fun refreshListPreviews() {
        val container = (application as AegisApplication).container
        val whitelist = withContext(Dispatchers.IO) { container.listRepository.listWhitelist(limit = 20) }
        val blacklist = withContext(Dispatchers.IO) { container.listRepository.listBlacklist(limit = 20) }

        withContext(Dispatchers.Main) {
            binding.tvWhitelistPreview.text = formatPreview(
                getString(R.string.settings_whitelist_preview_title),
                whitelist.map { it.domain }
            )
            binding.tvBlacklistPreview.text = formatPreview(
                getString(R.string.settings_blacklist_preview_title),
                blacklist.map { it.domain }
            )
        }
    }

    private fun formatPreview(label: String, entries: List<String>): String {
        if (entries.isEmpty()) return "$label\n(empty)"
        return "$label\n${entries.joinToString(separator = "\n")}"
    }

    private suspend fun showToastOnMain(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@SettingsActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun normalizeDomain(value: String): String? {
        val cleaned = value.trim().lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .trimEnd('/')
        if (cleaned.isBlank() || cleaned.contains(" ")) return null
        return cleaned
    }
}
