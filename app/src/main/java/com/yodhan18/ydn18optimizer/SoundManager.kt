package com.yodhan18.ydn18optimizer

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

class SoundManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var clickSoundId: Int = 0
    private var enterSoundId: Int = 0
    private var exitSoundId: Int = 0
    private var mediaPlayer: MediaPlayer? = null

    var clickEnabled = true
    var musicEnabled = true
    var volume = 0.7f

    init {
        val audioAttr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttr)
            .build()
        loadSounds()
    }

    private fun loadSounds() {
        try {
            clickSoundId = soundPool?.load(context, R.raw.click, 1) ?: 0
            enterSoundId = soundPool?.load(context, R.raw.enter, 1) ?: 0
            exitSoundId = soundPool?.load(context, R.raw.exit_sound, 1) ?: 0
        } catch (e: Exception) {
            // Sound files not found; silently ignore
        }
    }

    fun playClick() {
        if (clickEnabled && clickSoundId != 0) {
            soundPool?.play(clickSoundId, volume, volume, 1, 0, 1f)
        }
    }

    fun playEnter() {
        if (clickEnabled && enterSoundId != 0) {
            soundPool?.play(enterSoundId, volume, volume, 1, 0, 1f)
        }
    }

    fun playExit() {
        if (clickEnabled && exitSoundId != 0) {
            soundPool?.play(exitSoundId, volume, volume, 1, 0, 1f)
        }
    }

    fun startMusic() {
        if (!musicEnabled) return
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, R.raw.music)
            mediaPlayer?.isLooping = true
            mediaPlayer?.setVolume(volume * 0.4f, volume * 0.4f)
            mediaPlayer?.start()
        } catch (e: Exception) {
            // Music file not available
        }
    }

    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun setVolume(v: Float) {
        volume = v.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(volume * 0.4f, volume * 0.4f)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
