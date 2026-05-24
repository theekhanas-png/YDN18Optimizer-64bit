package com.yodhan18.ydn18optimizer

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yodhan18.ydn18optimizer.fragments.HomeFragment
import com.yodhan18.ydn18optimizer.fragments.OptimizerFragment
import com.yodhan18.ydn18optimizer.fragments.SensitivityFragment

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var bottomNav: BottomNavigationView
    lateinit var soundManager: SoundManager

    private var currentFragmentId = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        soundManager = SoundManager(this)
        loadSoundPrefs()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        toolbar.title = "YDN18 OPTIMIZER"

        bottomNav = findViewById(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { item ->
            val newId = item.itemId
            if (newId != currentFragmentId) {
                soundManager.playClick()
                navigateTo(newId)
                currentFragmentId = newId
            }
            true
        }

        // Load default fragment
        if (savedInstanceState == null) {
            navigateTo(R.id.nav_home)
        }

        // Start foreground service
        try {
            val serviceIntent = Intent(this, OptimizerForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            // Service start failed silently
        }

        if (soundManager.musicEnabled) {
            soundManager.startMusic()
        }
    }

    private fun navigateTo(itemId: Int) {
        val fragment: Fragment = when (itemId) {
            R.id.nav_home -> HomeFragment()
            R.id.nav_optimizer -> OptimizerFragment()
            R.id.nav_sensitivity -> SensitivityFragment()
            else -> HomeFragment()
        }

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                soundManager.playClick()
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadSoundPrefs() {
        val prefs = getSharedPreferences("ydn18_prefs", MODE_PRIVATE)
        soundManager.clickEnabled = prefs.getBoolean("click_sounds", true)
        soundManager.musicEnabled = prefs.getBoolean("bg_music", true)
        soundManager.setVolume(prefs.getInt("volume", 70) / 100f)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
