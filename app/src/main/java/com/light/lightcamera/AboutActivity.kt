package com.light.lightcamera

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

        binding.colorButton.setOnClickListener {
            showColorPickerDialog()
        }
    }

    private fun showColorPickerDialog() {
        val colorsMap = linkedMapOf(
            "White" to Color.WHITE,
            "Red" to Color.RED,
            "Green" to Color.GREEN,
            "Blue" to Color.BLUE,
            "Yellow" to Color.YELLOW,
            "Cyan" to Color.CYAN,
            "Magenta" to Color.MAGENTA,
            "Orange" to Color.parseColor("#FFA500"),
            "Purple" to Color.parseColor("#800080"),
            getString(R.string.custom_color) to Color.TRANSPARENT
        )
        val names = colorsMap.keys.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.select_color)
            .setItems(names) { _, which ->
                val name = names[which]
                if (name == getString(R.string.custom_color)) {
                    showCustomColorDialog()
                } else {
                    val selectedColor = colorsMap[name] ?: Color.WHITE
                    saveButtonColor(selectedColor)
                }
            }
            .show()
    }

    private fun showCustomColorDialog() {
        val input = EditText(this)
        input.hint = "#RRGGBB"
        
        // Add padding to the EditText
        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        val margin = (16 * resources.displayMetrics.density).toInt()
        params.leftMargin = margin
        params.rightMargin = margin
        input.layoutParams = params
        container.addView(input)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.enter_hex_color)
            .setView(container)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val hex = input.text.toString().trim()
                try {
                    val colorHex = if (hex.startsWith("#")) hex else "#$hex"
                    val color = Color.parseColor(colorHex)
                    saveButtonColor(color)
                } catch (e: Exception) {
                    Toast.makeText(this, R.string.invalid_color, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun saveButtonColor(color: Int) {
        val sharedPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putInt("button_color", color)
            apply()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
