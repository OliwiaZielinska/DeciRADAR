package com.example.deciradar

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
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

/**
 * Usługa monitorowania dźwięku działająca w tle, która:
 * - monitoruje natężenie dźwięku i wysyła powiadomienia, gdy zostaną przekroczone określone progi,
 * - zapisuje pomiary do bazy Firestore automatycznie – co 2 godziny w trybie dziennym oraz raz w trybie nocnym.
 */
class SoundMonitorService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var soundMeter: SoundMeter

    // Domyślne godziny trybu nocnego; mogą być później pobrane z bazy
    private var userNightStart: String = "22:00"
    private var userNightEnd: String = "06:00"

    // Zmienna kontroli powiadomień – minimalny odstęp między powiadomieniami (np. 60 sekund)
    private var lastNotificationTime: Long = 0
    private val notificationInterval = 60 * 1000L

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun onCreate() {
        super.onCreate()
        soundMeter = SoundMeter()
        soundMeter.start()

        // Pobierz ustawienia trybu nocnego użytkownika
        fetchUserNightMode()

        // Uruchomienie dwóch zadań: zapisywanie do bazy oraz monitorowanie poziomu dźwięku
        startPeriodicSaving()
        startSoundLevelMonitoring()
    }

    /**
     * Pobiera z Firestore godziny trybu nocnego ustawione przez użytkownika.
     */
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

    /**
     * Uruchamia cykliczne zapisywanie wyników do Firestore.
     * W trybie dziennym zapisywanie odbywa się co 2 godziny,
     * natomiast w trybie nocnym – raz (o 2 w nocy).
     */
    private fun startPeriodicSaving() {
        handler.post(object : Runnable {
            override fun run() {
                val currentTime = getCurrentTime()

                if (isNightMode(currentTime)) {
                    // W trybie nocnym zapisujemy tylko raz (przykładowo o drugiej w nocy)
                    if (currentTime == "02:00") {
                        saveDataToFirestore()
                    }
                    // Sprawdzamy co godzinę
                    handler.postDelayed(this, 60 * 60 * 1000L)
                } else {
                    // W trybie dziennym zapisujemy co 2 godziny
                    saveDataToFirestore()
                    handler.postDelayed(this, 2 * 60 * 60 * 1000L)
                }
            }
        })
    }

    /**
     * Uruchamia cykliczne sprawdzanie poziomu dźwięku w celu wysłania alertu,
     * jeśli poziom dźwięku przekroczy określone progi.
     */
    private fun startSoundLevelMonitoring() {
        handler.post(object : Runnable {
            override fun run() {
                checkSoundLevel()
                handler.postDelayed(this, 5000)
            }
        })
    }

    /**
     * Sprawdza aktualny poziom dźwięku i wysyła powiadomienia, gdy poziom przekracza:
     * - 120 dB – granica bólu (włączine zarówno w trybie dziennym jak i nowcnym),
     * - 85 dB – poziom szkodliwy dla zdrowia (tylko w trybie dziennym włączone).
     * źródło wartości: https://www.audika.pl/blog/poziom-halasu
     * cyt: Powyżej 85 decybeli hałas jest szkodliwy dla zdrowia i może powodować trwałe uszkodzenie słuchu, zaburzenia funkcjonowania układu krążenia i nerwowego, a także problemy z równowagą.
     * Powyżej 120 decybeli to granica bólu, może powodować drgawki, a nawet utratę przytomności
     */
    private fun checkSoundLevel() {
        val amplitude = soundMeter.getAmplitude()
        val soundIntensity = String.format("%.3f", amplitude)
        val currentTimeMillis = System.currentTimeMillis()
        val currentTime = getCurrentTime()

        // Sprawdzamy, czy jesteśmy w trybie nocnym
        val isNight = isNightMode(currentTime)

        // Zapewniamy minimalny odstęp między kolejnymi powiadomieniami
        if (currentTimeMillis - lastNotificationTime >= notificationInterval) {
            when {
                amplitude >= 120 -> {
                    sendAlertNotification("Ostrzeżenie! Osiągnięto granicę bólu - pomiar: $soundIntensity dB")
                    lastNotificationTime = currentTimeMillis
                }
                amplitude >= 85 && !isNight -> { // Powiadomienia >85 dB wyłączone w trybie nocnym
                    sendAlertNotification("Uwaga! Przebywasz w szkodliwym hałasie - pomiar: $soundIntensity dB")
                    lastNotificationTime = currentTimeMillis
                }
            }
        }
    }

    /**
     * Wysyła powiadomienie alertowe z określonym komunikatem.
     */
    private fun sendAlertNotification(message: String) {
        val channelId = "SoundAlertChannel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Sound Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Powiadomienia o wysokim poziomie dźwięku"
                enableLights(true)
                lightColor = Color.RED
                setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("DeciRADAR")
            .setContentText(message)
            .setSmallIcon(R.drawable.ear) // Użycie własnej ikony ucha do powiadomienia
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI) // Domyślny dźwięk powiadomienia
            .setAutoCancel(true)
            .build()

        // Używamy stałego ID, aby aktualizować to samo powiadomienie
        notificationManager.notify(999, notification)
    }
    /**
     * Zapisuje pomiar do bazy Firestore.
     */
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
        db.collection("measurements").add(measurement)
    }

    /**
     * Określa, czy aktualny czas mieści się w przedziale trybu nocnego.
     */
    private fun isNightMode(currentTime: String): Boolean {
        return currentTime >= userNightStart || currentTime < userNightEnd
    }

    /**
     * Zwraca aktualny czas w formacie HH:mm.
     */
    private fun getCurrentTime(): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(Date())
    }
    /**
     * Uruchamia usługę jako usługę pierwszoplanową z powiadomieniem.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createServiceNotification())
        return START_STICKY
    }

    /**
     * Tworzy powiadomienie informujące o działaniu usługi.
     */
    private fun createServiceNotification(): Notification {
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
            .setSmallIcon(R.drawable.radar)
            .build()
    }
    /**
     * Zatrzymuje usługę i usuwa zarejestrowane zadania.
     */
    override fun onDestroy() {
        super.onDestroy()
        soundMeter.stop()
        handler.removeCallbacksAndMessages(null)
    }
    /**
     * Ta usługa nie obsługuje komunikacji z innymi komponentami, dlatego zwraca null.
     */
    override fun onBind(intent: Intent?): IBinder? = null
}
