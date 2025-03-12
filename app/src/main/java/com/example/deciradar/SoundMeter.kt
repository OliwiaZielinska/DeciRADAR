package com.example.deciradar
import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import kotlin.math.log10

class SoundMeter {
    private var recorder: AudioRecord? = null
    private val sampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT)

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun start() {
        recorder = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
        recorder?.startRecording()
    }

    fun getAmplitude(): Double {
        val buffer = ShortArray(bufferSize)
        recorder?.read(buffer, 0, bufferSize)

        val amplitude = buffer.map { it.toInt() * it.toInt() }.average()
        return 10 * log10(amplitude)
    }

    fun stop() {
        recorder?.stop()
        recorder?.release()
        recorder = null
    }
}
