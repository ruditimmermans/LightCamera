package com.light.lightcamera

import android.content.Context
import androidx.preference.PreferenceManager
import java.text.SimpleDateFormat
import java.util.Locale

object FileUtils {
    const val DEFAULT_FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    const val KEY_FILENAME_FORMAT = "filename_format"

    fun getFormattedFilename(
        context: Context,
        prefix: String = "",
        defaultFormat: String = DEFAULT_FILENAME_FORMAT,
    ): String {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val pattern = sharedPrefs.getString(KEY_FILENAME_FORMAT, defaultFormat) ?: defaultFormat
        val timestamp = System.currentTimeMillis()
        val formatted = try {
            val sdf = SimpleDateFormat(pattern, Locale.US)
            sdf.format(timestamp)
        } catch (_: Exception) {
            val sdf = SimpleDateFormat(defaultFormat, Locale.US)
            sdf.format(timestamp)
        }
        val sanitized = formatted.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        return if (prefix.isNotEmpty()) "$prefix$sanitized" else sanitized
    }
}
