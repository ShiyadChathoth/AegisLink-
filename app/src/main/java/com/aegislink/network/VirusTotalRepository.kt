package com.aegislink.network

import android.util.Base64
import kotlinx.coroutines.delay
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Retrofit
import java.net.SocketTimeoutException

class VirusTotalRepository {
    private val service = Retrofit.Builder()
        .baseUrl("https://www.virustotal.com/")
        .build()
        .create(VirusTotalService::class.java)

    suspend fun scanUrl(apiKey: String, url: String): ScanResult {
        if (apiKey.isBlank()) return ScanResult.Error("Missing VirusTotal API key")

        return try {
            val encodedId = encodeUrlId(url)
            val cachedStats = service.getUrlReport(apiKey, encodedId).use { body ->
                val root = JSONObject(body.string().ifBlank { "{}" })
                root.optJSONObject("data")
                    ?.optJSONObject("attributes")
                    ?.optJSONObject("last_analysis_stats")
                    ?.let(::parseStats)
            }
            if (cachedStats != null) return fromStats(cachedStats, url)

            val submissionId = service.submitUrl(apiKey, url).use { body ->
                val root = JSONObject(body.string().ifBlank { "{}" })
                root.optJSONObject("data")?.optString("id").orEmpty()
            }
            if (submissionId.isBlank()) {
                return ScanResult.Error("Submitted to VirusTotal. Try scan again shortly.")
            }

            repeat(5) {
                delay(1200)
                val (status, stats) = service.getAnalysis(apiKey, submissionId).use { body ->
                    val root = JSONObject(body.string().ifBlank { "{}" })
                    val attributes = root.optJSONObject("data")?.optJSONObject("attributes")
                    val parsedStatus = attributes?.optString("status").orEmpty()
                    val parsedStats = attributes?.optJSONObject("stats")?.let(::parseStats)
                    parsedStatus to parsedStats
                }
                if (status == "completed" && stats != null) {
                    return fromStats(stats, url)
                }
            }
            ScanResult.Error("Submitted to VirusTotal. Analysis pending, try scan again.")
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> ScanResult.Error("Invalid VirusTotal API key")
                429 -> ScanResult.Error("VirusTotal rate limit reached")
                else -> ScanResult.Error("VirusTotal error: HTTP ${e.code()}")
            }
        } catch (_: SocketTimeoutException) {
            ScanResult.Error("VirusTotal request timed out")
        } catch (e: Exception) {
            ScanResult.Error("VirusTotal error: ${e.message ?: "unknown"}")
        }
    }

    private fun parseStats(statsJson: JSONObject): VtStats {
        return VtStats(
            malicious = statsJson.optInt("malicious", 0),
            suspicious = statsJson.optInt("suspicious", 0),
            harmless = statsJson.optInt("harmless", 0),
            undetected = statsJson.optInt("undetected", 0)
        )
    }

    private fun fromStats(stats: VtStats, scannedUrl: String): ScanResult.Success {
        val total =
            stats.malicious + stats.suspicious + stats.harmless + stats.undetected
        val riskLevel = when {
            stats.malicious > 0 -> "High risk"
            stats.suspicious > 0 -> "Suspicious"
            else -> "Low risk"
        }
        return ScanResult.Success(
            malicious = stats.malicious,
            suspicious = stats.suspicious,
            harmless = stats.harmless,
            undetected = stats.undetected,
            totalEngines = total,
            riskLevel = riskLevel,
            reportUrl = "https://www.virustotal.com/gui/url/${encodeUrlId(scannedUrl)}/detection"
        )
    }

    private fun encodeUrlId(url: String): String {
        return Base64.encodeToString(url.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
            .trimEnd('=')
    }
}

data class VtStats(
    val malicious: Int = 0,
    val suspicious: Int = 0,
    val harmless: Int = 0,
    val undetected: Int = 0
)

sealed class ScanResult {
    data class Success(
        val malicious: Int,
        val suspicious: Int,
        val harmless: Int,
        val undetected: Int,
        val totalEngines: Int,
        val riskLevel: String,
        val reportUrl: String
    ) : ScanResult()
    data class Error(val message: String) : ScanResult()
}
