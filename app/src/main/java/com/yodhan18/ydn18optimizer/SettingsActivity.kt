package com.yodhan18.ydn18optimizer

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.settingsToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val prefs = getSharedPreferences("ydn18_prefs", MODE_PRIVATE)

        // Theme switch (dark = checked)
        val switchTheme = findViewById<SwitchMaterial>(R.id.switchTheme)
        val isDark = prefs.getBoolean("dark_theme", true)
        switchTheme.isChecked = isDark
        switchTheme.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("dark_theme", checked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // BG Music
        val switchBgMusic = findViewById<SwitchMaterial>(R.id.switchBgMusic)
        switchBgMusic.isChecked = prefs.getBoolean("bg_music", true)
        switchBgMusic.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("bg_music", checked).apply()
        }

        // Click sounds
        val switchClick = findViewById<SwitchMaterial>(R.id.switchClickSounds)
        switchClick.isChecked = prefs.getBoolean("click_sounds", true)
        switchClick.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("click_sounds", checked).apply()
        }

        // Haptic
        val switchHaptic = findViewById<SwitchMaterial>(R.id.switchHaptic)
        switchHaptic.isChecked = prefs.getBoolean("haptic", true)
        switchHaptic.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("haptic", checked).apply()
        }

        // Volume
        val seekVolume = findViewById<SeekBar>(R.id.seekVolume)
        seekVolume.progress = prefs.getInt("volume", 70)
        seekVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar, progress: Int, fromUser: Boolean) {
                prefs.edit().putInt("volume", progress).apply()
            }
            override fun onStartTrackingTouch(sb: SeekBar) {}
            override fun onStopTrackingTouch(sb: SeekBar) {}
        })

        // Notification settings
        findViewById<android.widget.Button>(R.id.btnNotifSettings).setOnClickListener {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
            startActivity(intent)
        }
    }
}
