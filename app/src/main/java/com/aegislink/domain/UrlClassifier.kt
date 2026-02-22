package com.aegislink.domain

import android.net.Uri
import com.aegislink.data.repo.ListRepository

class UrlClassifier(
    private val sanitizer: UrlSanitizer,
    private val listRepository: ListRepository
) {
    suspend fun classify(inputUrl: String): ClassificationResult {
        val sanitized = sanitizer.sanitize(inputUrl)
        val domain = runCatching { Uri.parse(sanitized).host?.lowercase() }.getOrNull()
            ?: return ClassificationResult.Invalid("Malformed URL")

        val blacklisted = listRepository.isBlacklisted(domain)
        if (blacklisted != null) {
            return ClassificationResult.Blocked(domain, blacklisted.reason)
        }

        val whitelisted = listRepository.isWhitelisted(domain)
        if (whitelisted != null) {
            return ClassificationResult.Trusted(sanitized)
        }

        return ClassificationResult.Unknown(inputUrl, sanitized, domain)
    }
}
