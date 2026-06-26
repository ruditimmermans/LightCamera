package com.light.lightcamera

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.light.lightcamera.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.about)

        val versionName = try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (_: Exception) {
            "Unknown"
        }

        binding.versionText.text = getString(R.string.version_format, versionName)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
