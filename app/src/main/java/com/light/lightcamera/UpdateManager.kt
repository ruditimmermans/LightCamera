package com.light.lightcamera

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(private val context: Context) {

    companion object {
        private const val LATEST_RELEASE_URL = "https://api.github.com/repos/ruditimmermans/BenOSCamera/releases/latest"
        private const val RELEASES_PAGE_URL = "https://github.com/ruditimmermans/BenOSCamera/releases"
    }

    suspend fun checkForUpdates(): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val url = URL(LATEST_RELEASE_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connect()

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val latestVersion = json.getString("tag_name").replace("v", "")
                val currentVersion = getCurrentVersion()

                if (isNewerVersion(latestVersion, currentVersion)) {
                    val assets = json.getJSONArray("assets")
                    var downloadUrl = RELEASES_PAGE_URL
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        if (asset.getString("name").endsWith(".apk")) {
                            downloadUrl = asset.getString("browser_download_url")
                            break
                        }
                    }
                    UpdateResult.NewVersionAvailable(latestVersion, downloadUrl)
                } else {
                    UpdateResult.UpToDate
                }
            } else {
                UpdateResult.Error("Server returned code ${connection.responseCode}")
            }
        } catch (e: Exception) {
            Log.e("UpdateManager", "Error checking for updates", e)
            UpdateResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun getCurrentVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "0.0"
        } catch (e: Exception) {
            "0.0"
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

        val size = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until size) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    fun openDownloadUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    sealed class UpdateResult {
        data class NewVersionAvailable(val version: String, val downloadUrl: String) : UpdateResult()
        object UpToDate : UpdateResult()
        data class Error(val message: String) : UpdateResult()
    }
}
