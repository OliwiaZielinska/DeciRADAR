package com.example.deciradar

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Klasa służąca do wyświetlenia mapy i zgłaszania poziomu hałasu w poszczególnych lokalizacjach.
 * Aktywność pobiera lokalizację użytkownika i wyświetla mapę z markerami dla zgłoszonych poziomów hałasu.
 */
class Maps : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var returnFromMapsButton: Button
    private lateinit var reportNoiseButton: Button
    private lateinit var firestore: FirebaseFirestore

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

    /**
     * Metoda wywoływana podczas tworzenia aktywności.
     * Inicjalizuje aktywność, ustawia mapę, usługi lokalizacji oraz przyciski.
     * @param savedInstanceState Zapisany stan aktywności, jeśli istnieje.
     */
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.maps)

        // Inicjalizacja Firestore
        firestore = FirebaseFirestore.getInstance()

        // Przycisk powrotu
        returnFromMapsButton = findViewById(R.id.returnFromMapsButton)
        returnFromMapsButton.setOnClickListener { finish() }

        // Przycisk zgłoszenia hałasu
        reportNoiseButton = findViewById(R.id.reportNoiseButton)
        reportNoiseButton.setOnClickListener {
            // Pobranie lokalizacji użytkownika
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLocation = LatLng(it.latitude, it.longitude)
                    // Okno dialogowe do zgłoszenia hałasu
                    showNoiseReportDialog(userLocation)
                }
            }
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /**
     * Metoda ustawiająca mapę i ładująca zgłoszenia hałasu z pamięci.
     * @param googleMap Obiekt mapy, który został załadowany.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        setUpMap()
        loadNoiseReports()
        map.setOnMarkerClickListener { marker ->
            showConfirmationDialog(marker)
            true // zwracamy true, by nie pokazywać domyślnego info okna
        }
    }

    private fun showConfirmationDialog(marker: com.google.android.gms.maps.model.Marker) {
        val position = marker.position

        AlertDialog.Builder(this)
            .setTitle("Czy hałas jest nadal aktualny?")
            .setMessage(marker.title)
            .setPositiveButton("TAK") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this, "Dzięki za potwierdzenie!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("NIE") { dialog, _ ->
                deleteNoiseReport(position)
                marker.remove()
                Toast.makeText(this, "Pomiar usunięty z mapy", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun deleteNoiseReport(position: LatLng) {
        firestore.collection("noiseReports")
            .whereEqualTo("lat", position.latitude)
            .whereEqualTo("lng", position.longitude)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    firestore.collection("noiseReports").document(document.id).delete()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Błąd usuwania: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    /**
     * Metoda konfiguruje mapę, w tym umożliwia lokalizację użytkownika na mapie,
     * jeśli aplikacja ma odpowiednie uprawnienia.
     */
    @SuppressLint("MissingPermission")
    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            return
        }

        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)
                val zoomLevel = 15f
                val cameraUpdate = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(userLatLng, zoomLevel)
                map.animateCamera(cameraUpdate)
            }
        }
    }

    /**
     * Metoda wyświetlająca okno dialogowe, które umożliwia użytkownikowi zgłoszenie poziomu hałasu.
     * @param latLng Lokalizacja, w której hałas został zgłoszony.
     */
    private fun showNoiseReportDialog(latLng: LatLng) {
        val levels = arrayOf("50 dB", "60 dB", "70 dB", "80 dB", "90 dB", "100 dB")
        AlertDialog.Builder(this)
            .setTitle("Zgłoś poziom hałasu")
            .setItems(levels) { _, which ->
                val noiseLevel = levels[which]
                saveNoiseReport(latLng, noiseLevel)
                addNoiseMarker(latLng, noiseLevel)
                Toast.makeText(this, "Zgłoszono hałas: $noiseLevel", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    /**
     * Metoda służąca do zapisu zgłoszenia hałasu do bazy danych.
     * @param latLng Lokalizacja, w której hałas został zgłoszony.
     * @param noiseLevel Poziom hałasu zgłoszony przez użytkownika.
     */
    private fun saveNoiseReport(latLng: LatLng, noiseLevel: String) {
        val report = hashMapOf(
            "lat" to latLng.latitude,
            "lng" to latLng.longitude,
            "level" to noiseLevel,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("noiseReports")
            .add(report)
            .addOnSuccessListener {
                Toast.makeText(this, "Zapisano zgłoszenie w Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Błąd zapisu: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Metoda ładuje zgłoszenia hałasu z bazy danych i dodaje markery na mapie.
     */
    private fun loadNoiseReports() {
        firestore.collection("noiseReports")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val lat = document.getDouble("lat") ?: continue
                    val lng = document.getDouble("lng") ?: continue
                    val level = document.getString("level") ?: continue
                    val latLng = LatLng(lat, lng)
                    addNoiseMarker(latLng, level)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Błąd wczytywania: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Metoda dodająca marker na mapie dla zgłoszonego hałasu.
     * @param latLng Lokalizacja, w której hałas został zgłoszony.
     * @param noiseLevel Poziom hałasu zgłoszony przez użytkownika.
     */
    private fun addNoiseMarker(latLng: LatLng, noiseLevel: String) {
        val numericLevel = noiseLevel.filter { it.isDigit() }.toIntOrNull() ?: return
        val hue = getHueForNoiseLevel(numericLevel)

        val markerOptions = MarkerOptions()
            .position(latLng)
            .title("Poziom hałasu: $noiseLevel")
            .icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(hue))

        map.addMarker(markerOptions)
    }
    /**
     * Zwraca odpowiedni odcień koloru markera na podstawie poziomu hałasu.
     *
     * @param level Poziom hałasu jako liczba całkowita.
     * @return Float reprezentujący wartość hue (barwy) markera mapy Google.
     */
    private fun getHueForNoiseLevel(level: Int): Float {
        return when (level) {
            in 0..30 -> 120f            // ciemnozielony (zielony)
            in 31..40 -> 100f           // jaśniejszy zielony
            in 41..60 -> 80f            // jeszcze jaśniejszy zielony
            in 61..70 -> 60f            // zielono-żółty
            in 71..75 -> 50f            // żółty
            in 76..90 -> 45f            // ciemnożółty
            in 91..100 -> 30f           // jasnopomarańczowy
            in 101..110 -> 20f          // pomarańczowy
            in 111..120 -> 10f          // ciemnopomarańczowy
            else -> 0f                  // czerwony
        }
    }
}
