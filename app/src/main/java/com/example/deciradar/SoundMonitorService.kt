package com.example.deciradar

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.example.deciradar.CloudFirestore.Measurements
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class SoundMonitorService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var soundMeter: SoundMeter
    private var userNightStart: String = "22:00" // Domyślne godziny, zmieniane po pobraniu z bazy
    private var userNightEnd: String = "06:00"

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun onCreate() {
        super.onCreate()
        soundMeter = SoundMeter()
        soundMeter.start()

        // Pobranie godzin trybu nocnego użytkownika
        fetchUserNightMode()

        // Uruchomienie rejestrowania dźwięku
        startMonitoring()
    }

    private fun fetchUserNightMode() {
        val userID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userID).get()
            .addOnSuccessListener { document ->
                document?.let {
                    userNightStart = it.getString("startNightMode") ?: "22:00"
                    userNightEnd = it.getString("endNightMode") ?: "06:00"
                }
            }
    }

    private fun startMonitoring() {
        handler.post(object : Runnable {
            override fun run() {
                val currentTime = getCurrentTime()

                // Sprawdzamy, czy jesteśmy w trybie nocnym
                if (isNightMode(currentTime)) {
                    if (currentTime == "00:00") {
                        saveDataToFirestore()
                    }
                } else {
                    saveDataToFirestore()
                }

                // Ustal interwał: noc - raz dziennie, dzień - co 2 godziny
                val delay = if (isNightMode(currentTime)) 60 * 60 * 1000 else 2 * 60 * 60 * 1000
                handler.postDelayed(this, delay.toLong())
            }
        })
    }

    private fun saveDataToFirestore() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val currentTime = timeFormat.format(Date())
        val soundIntensity = String.format("%.3f", soundMeter.getAmplitude())

        val userID = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val measurement = Measurements(
            userID = userID,
            date = currentDate,
            hour = currentTime,
            soundIntensity = soundIntensity
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("measurements")
            .add(measurement)
    }

    private fun isNightMode(currentTime: String): Boolean {
        return currentTime >= userNightStart || currentTime < userNightEnd
    }

    private fun getCurrentTime(): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(Date())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "SoundMonitorServiceChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "DeciRADAR Sound Monitoring",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("DeciRADAR")
            .setContentText("Pomiar dźwięku w toku...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundMeter.stop()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
