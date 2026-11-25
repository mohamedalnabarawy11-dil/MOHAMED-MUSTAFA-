package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class StudyAudioEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var musicJob: Job? = null
    private var clickEnabled: Boolean = true
    private var musicEnabled: Boolean = false
    private var isPaused: Boolean = false

    fun setClickEnabled(enabled: Boolean) {
        clickEnabled = enabled
    }

    fun setMusicEnabled(enabled: Boolean) {
        if (musicEnabled == enabled) return
        musicEnabled = enabled
        updateMusicPlayback()
    }

    fun onPause() {
        isPaused = true
        stopMusicLoop()
    }

    fun onResume() {
        isPaused = false
        updateMusicPlayback()
    }

    fun playClick() {
        if (!clickEnabled) return
        scope.launch {
            runCatching {
                val sampleRate = 22050
                val durationMs = 22
                val numSamples = (sampleRate * durationMs) / 1000
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = 720.0 - (360.0 * (i.toDouble() / numSamples))
                    val env = exp(-t * 175.0)
                    val sample = sin(2.0 * PI * freq * t) * env * 0.24
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
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
                kotlinx.coroutines.delay(45)
                track.stop()
                track.release()
            }
        }
    }

    private fun updateMusicPlayback() {
        if (musicEnabled && !isPaused) {
            startMusicLoop()
        } else {
            stopMusicLoop()
        }
    }

    private fun startMusicLoop() {
        if (musicJob?.isActive == true) return
        musicJob = scope.launch {
            var track: AudioTrack? = null
            try {
                val sampleRate = 22050
                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(4096)

                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                track.play()

                // Warm calming study chord progression (Dm9 -> Bbmaj7 -> Fmaj7 -> C6)
                val chords = listOf(
                    doubleArrayOf(146.83, 220.00, 261.63, 329.63),
                    doubleArrayOf(116.54, 174.61, 220.00, 293.66),
                    doubleArrayOf(174.61, 220.00, 261.63, 329.63),
                    doubleArrayOf(130.81, 196.00, 220.00, 261.63)
                )
                val chunkSamples = 2048
                val pcm = ShortArray(chunkSamples)
                val chordDurationSec = 4.5
                var globalSampleIndex = 0L

                while (isActive && musicEnabled && !isPaused) {
                    for (i in 0 until chunkSamples) {
                        val t = globalSampleIndex.toDouble() / sampleRate
                        val cyclePos = (t / chordDurationSec)
                        val chordIdx = (cyclePos.toInt()) % chords.size
                        val localPhase = cyclePos - cyclePos.toInt()
                        val chord = chords[chordIdx]

                        // Smooth fade envelope per chord so there are zero clicks
                        val fadeIn = (localPhase / 0.18).coerceIn(0.0, 1.0)
                        val fadeOut = ((1.0 - localPhase) / 0.18).coerceIn(0.0, 1.0)
                        val padEnv = fadeIn * fadeOut

                        var mix = 0.0
                        for (f in chord) {
                            mix += sin(2.0 * PI * f * t)
                        }
                        // Gentle warm bell pulse twice per chord
                        val bellPhase = (localPhase * 2.0) - (localPhase * 2.0).toInt()
                        val bellFreq = chord[(localPhase * 2.0).toInt().coerceIn(0, chord.lastIndex)] * 2.0
                        val bellEnv = exp(-bellPhase * 4.5) * 0.35
                        mix = (mix / chord.size) * 0.75 + sin(2.0 * PI * bellFreq * t) * bellEnv

                        val out = mix * padEnv * 0.11
                        pcm[i] = (out * Short.MAX_VALUE)
                            .toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                            .toShort()
                        globalSampleIndex++
                    }
                    track.write(pcm, 0, chunkSamples)
                }
            } catch (_: Throwable) {
                // Safe fallback if audio hardware is unavailable
            } finally {
                runCatching {
                    track?.pause()
                    track?.flush()
                    track?.stop()
                    track?.release()
                }
            }
        }
    }

    private fun stopMusicLoop() {
        musicJob?.cancel()
        musicJob = null
    }

    fun release() {
        stopMusicLoop()
        scope.cancel()
    }
}
