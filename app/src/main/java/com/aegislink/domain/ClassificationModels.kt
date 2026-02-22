package com.aegislink.domain

sealed class ClassificationResult {
    data class Blocked(val domain: String, val reason: String) : ClassificationResult()
    data class Trusted(val sanitizedUrl: String) : ClassificationResult()
    data class Unknown(val originalUrl: String, val sanitizedUrl: String, val domain: String) : ClassificationResult()
    data class Invalid(val message: String) : ClassificationResult()
}
