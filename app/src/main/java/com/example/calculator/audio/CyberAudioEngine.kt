package com.example.calculator.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class CyberAudioEngine(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val audioScope = CoroutineScope(Dispatchers.Default)

    fun playKeyClick(enabled: Boolean) {
        if (!enabled) return
        audioScope.launch {
            generateTone(frequency = 1200.0, durationMs = 12, volume = 0.25f)
        }
    }

    fun playOperator(enabled: Boolean) {
        if (!enabled) return
        audioScope.launch {
            generateTone(frequency = 1600.0, durationMs = 20, volume = 0.3f)
        }
    }

    fun playEquals(enabled: Boolean) {
        if (!enabled) return
        audioScope.launch {
            generateDualTone(freq1 = 880.0, freq2 = 1320.0, durationMs = 70, volume = 0.4f)
        }
    }

    fun playClear(enabled: Boolean) {
        if (!enabled) return
        audioScope.launch {
            generateSweep(startFreq = 1400.0, endFreq = 400.0, durationMs = 50, volume = 0.35f)
        }
    }

    fun playError(enabled: Boolean) {
        if (!enabled) return
        audioScope.launch {
            generateSquareTone(frequency = 220.0, durationMs = 45, volume = 0.4f)
        }
    }

    fun playBoot(enabled: Boolean) {
        if (!enabled) return
        audioScope.launch {
            generateSweep(startFreq = 300.0, endFreq = 2200.0, durationMs = 200, volume = 0.5f)
        }
    }

    fun triggerHaptic(enabled: Boolean, strong: Boolean = false) {
        if (!enabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val ms = if (strong) 30L else 12L
                val amp = if (strong) VibrationEffect.DEFAULT_AMPLITUDE else 80
                vibrator.vibrate(VibrationEffect.createOneShot(ms, amp))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(if (strong) 30L else 12L)
            }
        } catch (_: Exception) {
        }
    }

    private fun generateTone(frequency: Double, durationMs: Int, volume: Float) {
        val sampleRate = 22050
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = (1.0 - (i.toDouble() / numSamples)) // Linear decay
            val sample = sin(2.0 * PI * frequency * t) * envelope
            buffer[i] = (sample * Short.MAX_VALUE * volume).toInt().toShort()
        }

        playBuffer(buffer, sampleRate)
    }

    private fun generateDualTone(freq1: Double, freq2: Double, durationMs: Int, volume: Float) {
        val sampleRate = 22050
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = (1.0 - (i.toDouble() / numSamples))
            val sample = (sin(2.0 * PI * freq1 * t) * 0.5 + sin(2.0 * PI * freq2 * t) * 0.5) * envelope
            buffer[i] = (sample * Short.MAX_VALUE * volume).toInt().toShort()
        }

        playBuffer(buffer, sampleRate)
    }

    private fun generateSweep(startFreq: Double, endFreq: Double, durationMs: Int, volume: Float) {
        val sampleRate = 22050
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            val t = i.toDouble() / sampleRate
            val envelope = sin(PI * progress) // smooth arc envelope
            val sample = sin(2.0 * PI * currentFreq * t) * envelope
            buffer[i] = (sample * Short.MAX_VALUE * volume).toInt().toShort()
        }

        playBuffer(buffer, sampleRate)
    }

    private fun generateSquareTone(frequency: Double, durationMs: Int, volume: Float) {
        val sampleRate = 22050
        val numSamples = (durationMs * sampleRate) / 1000
        val buffer = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val raw = sin(2.0 * PI * frequency * t)
            val sq = if (raw >= 0) 1.0 else -1.0
            val envelope = (1.0 - (i.toDouble() / numSamples))
            buffer[i] = (sq * envelope * Short.MAX_VALUE * volume).toInt().toShort()
        }

        playBuffer(buffer, sampleRate)
    }

    private fun playBuffer(buffer: ShortArray, sampleRate: Int) {
        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            track.setNotificationMarkerPosition(buffer.size)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(trackRef: AudioTrack?) {
                    trackRef?.release()
                }
                override fun onPeriodicNotification(trackRef: AudioTrack?) {}
            })
        } catch (_: Exception) {
        }
    }
}
