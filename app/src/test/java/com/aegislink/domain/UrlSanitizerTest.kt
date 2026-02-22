package com.aegislink.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class UrlSanitizerTest {
    @Test
    fun removesTrackingParams() {
        val sanitizer = UrlSanitizer(setOf("utm_source", "fbclid"))
        val out = sanitizer.sanitize("https://example.com/page?a=1&utm_source=x&fbclid=y")
        assertEquals("https://example.com/page?a=1", out)
    }

    @Test
    fun keepsFunctionalParams() {
        val sanitizer = UrlSanitizer(setOf("utm_source"))
        val out = sanitizer.sanitize("https://example.com/page?id=42&utm_source=x")
        assertEquals("https://example.com/page?id=42", out)
    }
}
