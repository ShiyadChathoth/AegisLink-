package com.aegislink.ui.intercept

import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.aegislink.AegisApplication
import com.aegislink.R
import com.aegislink.domain.ClassificationResult
import com.aegislink.network.ScanResult
import com.aegislink.ui.blocked.BlockedUrlActivity
import com.aegislink.util.BrowserForwarder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InterceptActivity : AppCompatActivity() {

    private val vm: InterceptViewModel by viewModels {
        val container = (application as AegisApplication).container
        InterceptViewModelFactory(
            classifier = container.classifier(),
            listRepository = container.listRepository,
            securePrefsRepository = container.securePrefsRepository,
            virusTotalRepository = container.virusTotalRepository,
            statsRepository = container.statsRepository
        )
    }

    private var originalUrl: String? = null
    private var preferredBrowserPackage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as AegisApplication).container
        val url = intent?.dataString
        if (url.isNullOrBlank()) {
            finish()
            return
        }
        originalUrl = url

        observeState()
        lifecycleScope.launch {
            val enabled = withContext(Dispatchers.IO) { container.db.settingsDao().get("enabled") != "false" }
            preferredBrowserPackage = withContext(Dispatchers.IO) { container.db.settingsDao().get("preferred_browser_pkg") }
            if (!enabled) {
                forwardToBrowserOrNotify(url)
                return@launch
            }

            container.listRepository.seedBlacklistIfNeeded()
            vm.classify(url)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            vm.classification.collectLatest { result ->
                when (result) {
                    is ClassificationResult.Blocked -> showBlocked(result)
                    is ClassificationResult.Trusted -> forwardToBrowserOrNotify(result.sanitizedUrl)
                    is ClassificationResult.Unknown -> showUnknownDialog(result)
                    is ClassificationResult.Invalid -> {
                        Toast.makeText(this@InterceptActivity, result.message, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    null -> Unit
                }
            }
        }

        lifecycleScope.launch {
            vm.scanResult.collectLatest { scan ->
                when (scan) {
                    is ScanResult.Success -> {
                        vm.clearScanResult()
                        showVtResultDialog(scan)
                    }
                    is ScanResult.Error -> {
                        vm.clearScanResult()
                        Toast.makeText(this@InterceptActivity, scan.message, Toast.LENGTH_LONG).show()
                    }
                    null -> Unit
                }
            }
        }
    }

    private fun showBlocked(result: ClassificationResult.Blocked) {
        startActivity(
            Intent(this, BlockedUrlActivity::class.java)
                .putExtra("blocked_url", originalUrl)
                .putExtra("reason", result.reason)
        )
        finish()
    }

    private fun showUnknownDialog(result: ClassificationResult.Unknown) {
        val messageView = TextView(this).apply {
            text = getString(R.string.dialog_original_sanitized, result.originalUrl, result.sanitizedUrl)
            setPadding(0, 0, 0, dp(8))
        }
        val whitelistCheck = CheckBox(this).apply {
            text = getString(R.string.whitelist_on_proceed)
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(messageView)
            addView(whitelistCheck)
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.unknown_title))
            .setView(content)
            .setPositiveButton(getString(R.string.action_proceed)) { _, _ ->
                if (whitelistCheck.isChecked) {
                    vm.whitelist(result.domain)
                }
                forwardToBrowserOrNotify(result.sanitizedUrl)
            }
            .setNeutralButton(getString(R.string.action_scan)) { _, _ -> vm.scanWithVirusTotal(result.originalUrl) }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .show()
    }

    private fun showVtResultDialog(scan: ScanResult.Success) {
        val summaryView = TextView(this).apply {
            text = getString(R.string.vt_risk_summary, scan.riskLevel, scan.totalEngines)
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 0, 0, dp(10))
        }

        val statsView = TextView(this).apply {
            text = getString(
                R.string.vt_result_details,
                scan.malicious,
                scan.suspicious,
                scan.harmless,
                scan.undetected
            )
            textSize = 15f
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
            addView(summaryView)
            addView(statsView)
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.vt_result_title))
            .setView(content)
            .setPositiveButton(getString(R.string.vt_view_full_report)) { _, _ ->
                openVtReport(scan.reportUrl)
            }
            .setNeutralButton(getString(R.string.vt_copy_report_link)) { _, _ ->
                copyReportLink(scan.reportUrl)
            }
            .setNegativeButton(getString(R.string.ok), null)
            .show()
    }

    private fun openVtReport(url: String) {
        val opened = BrowserForwarder.open(this, url, preferredBrowserPackage)
        if (!opened) {
            copyReportLink(url)
            Toast.makeText(this, getString(R.string.vt_open_failed_link_copied), Toast.LENGTH_LONG).show()
        }
    }

    private fun copyReportLink(url: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("VirusTotal report", url))
        Toast.makeText(this, getString(R.string.vt_report_link_copied), Toast.LENGTH_SHORT).show()
    }

    private fun forwardToBrowserOrNotify(url: String) {
        val opened = BrowserForwarder.open(this, url, preferredBrowserPackage)
        if (!opened) {
            Toast.makeText(this, getString(R.string.no_external_browser), Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun dp(value: Int): Int {
        val density = resources.displayMetrics.density
        return (value * density).toInt()
    }
}
