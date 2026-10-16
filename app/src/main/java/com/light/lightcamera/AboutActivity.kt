package com.light.lightcamera

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.light.lightcamera.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBarLayout.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            binding.nestedScrollView.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val versionName = try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName
        } catch (_: Exception) {
            getString(R.string.unknown)
        }

        binding.versionText.text = getString(R.string.version_format, versionName)

        binding.donateButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, getString(R.string.donate_url).toUri())
            try {
                startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(this, R.string.no_app_to_open_url, Toast.LENGTH_SHORT).show()
            }
        }

        binding.donatePaypalButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, getString(R.string.donate_paypal_url).toUri())
            try {
                startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(this, R.string.no_app_to_open_url, Toast.LENGTH_SHORT).show()
            }
        }

        binding.reportIssuesButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, getString(R.string.report_issues_url).toUri())
            try {
                startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(this, R.string.no_app_to_open_url, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
