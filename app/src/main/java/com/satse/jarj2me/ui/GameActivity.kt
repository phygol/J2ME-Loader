package com.satse.jarj2me.ui

import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import com.satse.jarj2me.R
import com.satse.jarj2me.emulator.J2MEEmulator

class GameActivity : AppCompatActivity() {

    private lateinit var emulator: J2MEEmulator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val gamePath = intent.getStringExtra("game_path") ?: return
        val gameTitle = intent.getStringExtra("game_title") ?: "J2ME Game"

        supportActionBar?.title = gameTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        emulator = findViewById(R.id.emulatorView)
        emulator.loadGame(gamePath, null)
    }

    override fun onResume() {
        super.onResume()
        emulator.start()
    }

    override fun onPause() {
        super.onPause()
        emulator.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        emulator.stop()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun hideSystemBars() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}