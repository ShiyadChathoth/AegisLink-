package com.aegislink.domain

import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class UrlSanitizer(private val removableParams: Set<String>) {

    fun sanitize(url: String): String {
        val hashIndex = url.indexOf('#')
        val queryEnd = if (hashIndex >= 0) hashIndex else url.length
        val queryStart = url.indexOf('?')
        if (queryStart < 0 || queryStart >= queryEnd) return url

        val base = url.substring(0, queryStart)
        val fragment = if (hashIndex >= 0) url.substring(hashIndex) else ""
        val query = url.substring(queryStart + 1, queryEnd)
        if (query.isBlank()) return url

        val kept = query.split('&')
            .filter { it.isNotBlank() }
            .filterNot { pair ->
                val encodedName = pair.substringBefore('=', pair)
                val decodedName = runCatching {
                    URLDecoder.decode(encodedName, StandardCharsets.UTF_8.name())
                }.getOrDefault(encodedName)
                decodedName in removableParams
            }

        return when {
            kept.isEmpty() -> base + fragment
            else -> "$base?${kept.joinToString("&")}$fragment"
        }
    }

    companion object {
        private val FALLBACK_PARAMS = setOf(
            "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content",
            "fbclid", "gclid", "igshid", "mc_cid", "mc_eid"
        )

        fun fromClearUrlsJson(raw: String): UrlSanitizer {
            val params = mutableSetOf<String>()
            runCatching {
                val root = JSONObject(raw)
                val providers = root.optJSONArray("providers") ?: JSONArray()
                for (i in 0 until providers.length()) {
                    val p = providers.getJSONObject(i)
                    val rules = p.optJSONArray("rules") ?: continue
                    for (j in 0 until rules.length()) {
                        val rule = rules.getString(j)
                        if (rule.matches(Regex("^[A-Za-z0-9_]+$"))) {
                            params.add(rule)
                        }
                    }
                }
            }

            if (params.isEmpty()) params.addAll(FALLBACK_PARAMS)
            return UrlSanitizer(params)
        }
    }
}
