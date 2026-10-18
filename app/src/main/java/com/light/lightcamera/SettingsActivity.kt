package com.light.lightcamera

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.light.lightcamera.databinding.ActivitySettingsBinding
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.appBarLayout.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            binding.settings.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            setupGalleryPreference()

            findPreference<Preference>("button_color")?.setOnPreferenceClickListener {
                showColorPickerDialog()
                true
            }

            findPreference<Preference>("check_for_updates")?.setOnPreferenceClickListener {
                checkForUpdates()
                true
            }
        }

        private fun setupGalleryPreference() {
            val galleryPref = findPreference<ListPreference>("default_gallery") ?: return
            val context = requireContext()

            val entriesList = mutableListOf<String>()
            val entryValuesList = mutableListOf<String>()

            entriesList.add(getString(R.string.default_gallery_system))
            entryValuesList.add("default")

            val galleryApps = getInstalledGalleryApps(context)
            for ((packageName, label) in galleryApps) {
                entriesList.add(label)
                entryValuesList.add(packageName)
            }

            galleryPref.entries = entriesList.toTypedArray()
            galleryPref.entryValues = entryValuesList.toTypedArray()

            if (galleryPref.value == null || !entryValuesList.contains(galleryPref.value)) {
                galleryPref.value = "default"
            }
        }

        private fun getInstalledGalleryApps(context: Context): List<Pair<String, String>> {
            val pm = context.packageManager
            val galleryAppsMap = mutableMapOf<String, String>()

            val intent = Intent(Intent.ACTION_VIEW).apply {
                type = "image/*"
            }

            val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            }

            for (resolveInfo in resolveInfos) {
                val packageName = resolveInfo.activityInfo.packageName
                if (packageName != context.packageName) {
                    val label = resolveInfo.loadLabel(pm).toString()
                    galleryAppsMap[packageName] = label
                }
            }

            val galleryCategoryIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_GALLERY)
            }
            val galleryCategoryInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(galleryCategoryIntent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(galleryCategoryIntent, PackageManager.MATCH_DEFAULT_ONLY)
            }

            for (resolveInfo in galleryCategoryInfos) {
                val packageName = resolveInfo.activityInfo.packageName
                if (packageName != context.packageName) {
                    val label = resolveInfo.loadLabel(pm).toString()
                    galleryAppsMap[packageName] = label
                }
            }

            return galleryAppsMap.toList().sortedBy { it.second }
        }

        private fun checkForUpdates() {
            val updateManager = UpdateManager(requireContext())
            
            lifecycleScope.launch {
                Toast.makeText(requireContext(), R.string.checking_updates, Toast.LENGTH_SHORT).show()
                val result = updateManager.checkForUpdates()
                
                when (result) {
                    is UpdateManager.UpdateResult.NewVersionAvailable -> {
                        val dialog = MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.check_for_updates)
                            .setMessage(getString(R.string.update_available, result.version))
                            .setPositiveButton(R.string.download_update) { _, _ ->
                                updateManager.downloadOrOpenUpdate(result.downloadUrl, result.version)
                            }
                            .setNegativeButton(android.R.string.cancel, null)
                            .create()

                        dialog.window?.let { window ->
                            window.setGravity(android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL)
                            val layoutParams = window.attributes
                            layoutParams.y = (32 * resources.displayMetrics.density).toInt()
                            window.attributes = layoutParams
                        }

                        dialog.show()
                    }
                    is UpdateManager.UpdateResult.UpToDate -> {
                        Toast.makeText(requireContext(), R.string.update_not_available, Toast.LENGTH_SHORT).show()
                    }
                    is UpdateManager.UpdateResult.Error -> {
                        Toast.makeText(requireContext(), R.string.update_error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        private fun showColorPickerDialog() {
            val context = requireContext()
            val colorsMap = linkedMapOf(
                getString(R.string.color_white) to Color.WHITE,
                getString(R.string.color_red) to Color.RED,
                getString(R.string.color_green) to Color.GREEN,
                getString(R.string.color_blue) to Color.BLUE,
                getString(R.string.color_yellow) to Color.YELLOW,
                getString(R.string.color_cyan) to Color.CYAN,
                getString(R.string.color_magenta) to Color.MAGENTA,
                getString(R.string.color_orange) to Color.parseColor("#FFA500"),
                getString(R.string.color_purple) to Color.parseColor("#800080"),
                getString(R.string.custom_color) to Color.TRANSPARENT
            )
            val names = colorsMap.keys.toTypedArray()

            MaterialAlertDialogBuilder(context)
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
            val context = requireContext()
            val input = EditText(context)
            input.hint = "#RRGGBB"

            val container = FrameLayout(context)
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            val margin = (16 * resources.displayMetrics.density).toInt()
            params.leftMargin = margin
            params.rightMargin = margin
            input.layoutParams = params
            container.addView(input)

            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.enter_hex_color)
                .setView(container)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val hex = input.text.toString().trim()
                    try {
                        val colorHex = if (hex.startsWith("#")) hex else "#$hex"
                        val color = Color.parseColor(colorHex)
                        saveButtonColor(color)
                    } catch (e: Exception) {
                        Toast.makeText(context, R.string.invalid_color, Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        private fun saveButtonColor(color: Int) {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
            with(sharedPrefs.edit()) {
                putInt("button_color", color)
                apply()
            }
            Toast.makeText(requireContext(), getString(R.string.color_updated), Toast.LENGTH_SHORT).show()
        }
    }
}
