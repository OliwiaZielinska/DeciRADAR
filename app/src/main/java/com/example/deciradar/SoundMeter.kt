package com.example.deciradar
import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import kotlin.math.log10
/**
 * Klasa `SoundMeter` służy do pomiaru poziomu dźwięku przy użyciu mikrofonu urządzenia.
 */
class SoundMeter {
    private var recorder: AudioRecord? = null
    private val sampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT)
    /**
     * Rozpoczyna nagrywanie dźwięku przy użyciu mikrofonu.
     * Wymaga uprawnienia RECORD_AUDIO.
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun start() {
        recorder = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
        recorder?.startRecording()
    }
    /**
    * Oblicza poziom głośności dźwięku na podstawie wartości amplitudy próbek.
     */
fun getAmplitude(): Double {
        val buffer = ShortArray(bufferSize)
        recorder?.read(buffer, 0, bufferSize)

        val amplitude = buffer.map { it.toInt() * it.toInt() }.average()
        return 10 * log10(amplitude)
    }
    /**
     * Zatrzymuje nagrywanie i zwalnia zasoby.
     */
    fun stop() {
        recorder?.stop()
        recorder?.release()
        recorder = null
    }
}
