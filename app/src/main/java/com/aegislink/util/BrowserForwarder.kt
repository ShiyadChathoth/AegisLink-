package com.aegislink.util

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

object BrowserForwarder {
    fun open(activity: Activity, url: String, preferredBrowserPackage: String?): Boolean {
        val pm = activity.packageManager
        val ownPackage = activity.packageName
        val uri = Uri.parse(url)

        if (!preferredBrowserPackage.isNullOrBlank() && preferredBrowserPackage != ownPackage) {
            if (launchWithPackage(activity, uri, preferredBrowserPackage)) return true
        }

        val baseBrowseIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val defaultPkg = pm.resolveActivity(baseBrowseIntent, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo?.packageName
        if (!defaultPkg.isNullOrBlank() && defaultPkg != ownPackage) {
            if (launchWithPackage(activity, uri, defaultPkg)) return true
        }

        val candidates = pm.queryIntentActivities(baseBrowseIntent, 0)
            .map { it.activityInfo.packageName }
            .distinct()
            .filter { it != ownPackage }

        for (pkg in candidates) {
            if (launchWithPackage(activity, uri, pkg)) return true
        }

        return false
    }

    private fun launchWithPackage(activity: Activity, uri: Uri, pkg: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            setPackage(pkg)
        }
        return runCatching {
            activity.startActivity(intent)
            activity.finish()
            true
        }.getOrDefault(false)
    }
}
