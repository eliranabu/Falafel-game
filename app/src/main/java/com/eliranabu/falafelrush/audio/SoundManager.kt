package com.eliranabu.falafelrush.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Pure-synthesis arcade audio engine. No media assets: every effect is rendered
 * to a PCM buffer once at init (background thread) and played back through a
 * dedicated MODE_STATIC AudioTrack for near-zero-latency triggering.
 */
enum class SoundId {
    COIN_CHIME,      // serve reward / upgrade purchase
    PLOP,            // ingredient added to the pita
    SERVE_ARPEGGIO,  // perfect serve celebration
    ANGRY_BUZZ,      // wrong recipe / trash / customer left
    RUSH_ALARM,      // rush hour begins
    BUTTON_TICK,     // generic UI tap
    DAY_END_JINGLE   // day summary fanfare
}

class SoundManager(scope: CoroutineScope) {

    @Volatile
    var enabled: Boolean = true

    private val sampleRate = 44100
    private val tracks = HashMap<SoundId, AudioTrack>()

    @Volatile
    private var ready = false

    init {
        // Heavy PCM rendering happens off the main thread exactly once
        scope.launch(Dispatchers.Default) {
            try {
                val rendered = linkedMapOf(
                    SoundId.COIN_CHIME to renderCoinChime(),
                    SoundId.PLOP to renderPlop(),
                    SoundId.SERVE_ARPEGGIO to renderServeArpeggio(),
                    SoundId.ANGRY_BUZZ to renderAngryBuzz(),
                    SoundId.RUSH_ALARM to renderRushAlarm(),
                    SoundId.BUTTON_TICK to renderButtonTick(),
                    SoundId.DAY_END_JINGLE to renderDayEndJingle()
                )
                rendered.forEach { (id, pcm) ->
                    tracks[id] = buildStaticTrack(pcm)
                }
                ready = true
            } catch (_: Exception) {
                // Audio is a nice-to-have: never crash the game over it
            }
        }
    }

    fun play(id: SoundId) {
        if (!enabled || !ready) return
        val track = tracks[id] ?: return
        try {
            track.stop()
            track.reloadStaticData()
            track.play()
        } catch (_: Exception) {
            // Ignore IllegalStateException from rapid re-triggering on some devices
        }
    }

    fun release() {
        tracks.values.forEach { runCatching { it.release() } }
        tracks.clear()
        ready = false
    }

    // ---------- AudioTrack plumbing ----------

    private fun buildStaticTrack(pcm: ShortArray): AudioTrack {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
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
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * 2)
            .build()
        track.write(pcm, 0, pcm.size)
        return track
    }

    // ---------- Synthesis primitives ----------

    private fun samples(ms: Int): Int = sampleRate * ms / 1000

    /** 5ms linear attack + exponential release envelope, soft amplitude. */
    private fun envelope(i: Int, total: Int, releaseSharpness: Float = 4.5f): Float {
        val attackSamples = samples(5).coerceAtLeast(1)
        val attack = if (i < attackSamples) i / attackSamples.toFloat() else 1f
        val release = exp((-releaseSharpness * i / total).toDouble()).toFloat()
        return attack * release
    }

    private fun sine(freq: Float, i: Int): Float =
        sin(2.0 * PI * freq * i / sampleRate).toFloat()

    private fun square(freq: Float, i: Int): Float =
        if (sin(2.0 * PI * freq * i / sampleRate) >= 0) 1f else -1f

    private fun toPcm(buffer: FloatArray, gain: Float = 0.5f): ShortArray {
        return ShortArray(buffer.size) { i ->
            val v = (buffer[i] * gain).coerceIn(-1f, 1f)
            (v * Short.MAX_VALUE).toInt().toShort()
        }
    }

    /** Render a single decaying tone segment into [out] starting at [offset]. */
    private fun renderTone(
        out: FloatArray,
        offset: Int,
        freq: Float,
        durMs: Int,
        shape: (Float, Int) -> Float = ::sine,
        releaseSharpness: Float = 4.5f
    ) {
        val n = samples(durMs)
        for (i in 0 until n) {
            val idx = offset + i
            if (idx >= out.size) break
            out[idx] += shape(freq, i) * envelope(i, n, releaseSharpness)
        }
    }

    // ---------- Sound recipes ----------

    /** Two bright sine pings rising 1318Hz (E6) -> 1760Hz (A6). */
    private fun renderCoinChime(): ShortArray {
        val out = FloatArray(samples(240))
        renderTone(out, 0, 1318.5f, 110)
        renderTone(out, samples(90), 1760f, 150)
        return toPcm(out, 0.45f)
    }

    /** Quick downward frequency sweep 300Hz -> 80Hz, soft plop. */
    private fun renderPlop(): ShortArray {
        val n = samples(90)
        val out = FloatArray(n)
        for (i in 0 until n) {
            val progress = i / n.toFloat()
            val freq = 300f - (220f * progress)
            // Manual phase accumulation for a smooth pitch glide
            out[i] = sin(2.0 * PI * freq * i / sampleRate).toFloat() * envelope(i, n, 3.5f)
        }
        return toPcm(out, 0.5f)
    }

    /** Rising C-major arpeggio C5-E5-G5-C6 — the perfect-serve fanfare. */
    private fun renderServeArpeggio(): ShortArray {
        val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.5f)
        val noteMs = 70
        val out = FloatArray(samples(noteMs * notes.size + 120))
        notes.forEachIndexed { idx, freq ->
            renderTone(out, samples(noteMs * idx), freq, noteMs + 110, releaseSharpness = 3.5f)
        }
        return toPcm(out, 0.42f)
    }

    /** Low square-wave growl mixed with white noise — anger / failure. */
    private fun renderAngryBuzz(): ShortArray {
        val n = samples(260)
        val out = FloatArray(n)
        val noise = Random(42)
        for (i in 0 until n) {
            val sq = square(110f, i) * 0.7f
            val nz = (noise.nextFloat() * 2f - 1f) * 0.3f
            out[i] = (sq + nz) * envelope(i, n, 2.8f)
        }
        return toPcm(out, 0.4f)
    }

    /** Alternating two-tone klaxon 880/660Hz — rush hour begins. */
    private fun renderRushAlarm(): ShortArray {
        val segMs = 110
        val segments = 4 // hi-lo-hi-lo
        val out = FloatArray(samples(segMs * segments))
        for (s in 0 until segments) {
            val freq = if (s % 2 == 0) 880f else 660f
            val offset = samples(segMs * s)
            val n = samples(segMs)
            for (i in 0 until n) {
                val idx = offset + i
                if (idx >= out.size) break
                out[idx] += square(freq, i) * 0.6f * envelope(i, n, 1.6f)
            }
        }
        return toPcm(out, 0.35f)
    }

    /** Tiny 2kHz tick for UI taps. */
    private fun renderButtonTick(): ShortArray {
        val out = FloatArray(samples(35))
        renderTone(out, 0, 2000f, 35, releaseSharpness = 6f)
        return toPcm(out, 0.3f)
    }

    /** Five-note victory jingle: C5 E5 G5 E5 C6. */
    private fun renderDayEndJingle(): ShortArray {
        val melody = listOf(
            523.25f to 140, // C5
            659.25f to 140, // E5
            783.99f to 140, // G5
            659.25f to 140, // E5
            1046.5f to 420  // C6 (held)
        )
        var totalMs = 0
        melody.forEach { totalMs += it.second }
        val out = FloatArray(samples(totalMs + 200))
        var cursorMs = 0
        melody.forEach { (freq, durMs) ->
            renderTone(out, samples(cursorMs), freq, durMs + 160, releaseSharpness = 3f)
            cursorMs += durMs
        }
        return toPcm(out, 0.42f)
    }
}
