package com.example.deciradar

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Usługa działająca w tle, która monitoruje lokalizację użytkownika
 * i sprawdza, czy znajduje się w pobliżu zgłoszonych punktów hałasu.
 * Jeśli użytkownik zbliży się na odległość mniejszą lub równą poziomowi hałasu (w metrach),
 * otrzymuje powiadomienie.
 */
class NoiseMonitoringService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var firestore: FirebaseFirestore
    private lateinit var locationCallback: LocationCallback

    // Zestaw lokalizacji, dla których już wysłano powiadomienie, aby nie duplikować alertów
    private val notifiedLocations = mutableSetOf<String>()

    companion object {
        const val CHANNEL_ID = "NoiseMonitorChannel"
    }

    /**
     * Inicjalizuje komponenty usługi, uruchamia monitorowanie lokalizacji i powiadomienie pierwszoplanowe.
     */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        firestore = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startForegroundNotification()
        startLocationMonitoring()
    }

    /**
     * Tworzy i uruchamia powiadomienie pierwszoplanowe informujące o aktywności usługi.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundNotification() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Monitorowanie hałasu",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DeciRadar aktywny")
            .setContentText("Trwa monitorowanie hałasu w tle")
            .setSmallIcon(R.drawable.ear)
            .build()

        startForeground(1, notification)
    }

    /**
     * Rozpoczyna monitorowanie lokalizacji użytkownika z wysoką dokładnością.
     * Co kilka sekund sprawdzana jest ostatnia znana lokalizacja.
     */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun startLocationMonitoring() {
        val request = LocationRequest.create().apply {
            interval = 10000              // Co 10 sekund
            fastestInterval = 5000       // Najszybciej co 5 sekund
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                checkNearbyNoise(location)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            mainLooper
        )
    }

    /**
     * Sprawdza zgłoszenia hałasu w bazie danych i porównuje z aktualną lokalizacją użytkownika.
     * Jeśli użytkownik znajduje się w pobliżu (odległość ≤ poziom dB w metrach), zostaje powiadomiony.
     */
    private fun checkNearbyNoise(currentLocation: Location) {
        firestore.collection("noiseReports")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val lat = document.getDouble("lat") ?: continue
                    val lng = document.getDouble("lng") ?: continue
                    val levelStr = document.getString("level")?.replace(" dB", "") ?: continue
                    val level = levelStr.toIntOrNull() ?: continue

                    val reportLocation = Location("").apply {
                        latitude = lat
                        longitude = lng
                    }

                    val distance = currentLocation.distanceTo(reportLocation)
                    val locationId = "$lat,$lng"

                    // Powiadom jeśli użytkownik zbliża się do zgłoszenia hałasu
                    if (distance <= level && !notifiedLocations.contains(locationId)) {
                        sendNoiseAlert(level, distance.toInt())
                        notifiedLocations.add(locationId)
                    }

                    // Usuń lokalizację z listy, jeśli użytkownik się oddalił
                    if (distance > level && notifiedLocations.contains(locationId)) {
                        notifiedLocations.remove(locationId)
                    }
                }
            }
    }

    /**
     * Tworzy i wysyła powiadomienie, informując użytkownika o zbliżającym się źródle hałasu.
     *
     * @param level Poziom hałasu (dB), który określa zasięg.
     * @param distance Faktyczna odległość użytkownika do źródła (w metrach).
     */
    private fun sendNoiseAlert(level: Int, distance: Int) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Hałas w pobliżu!")
            .setContentText("Zbliżasz się do miejsca o hałasie $level dB. Odległość: ${distance}m")
            .setSmallIcon(R.drawable.ear)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    /**
     * Ta usługa nie umożliwia komunikacji z komponentami aplikacji – zwraca `null`.
     */
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Zatrzymuje nasłuchiwanie lokalizacji przy niszczeniu usługi.
     */
    override fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }
}