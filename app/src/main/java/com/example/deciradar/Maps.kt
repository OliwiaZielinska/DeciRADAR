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
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

/**
 * Klasa służąca do wyświetlenia mapy i zgłaszania poziomu hałasu w poszczególnych lokalizacjach.
 * Aktywność pobiera lokalizację użytkownika i wyświetla mapę z markerami dla zgłoszonych poziomów hałasu.
 */
class Maps : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var returnFromMapsButton: Button
    private lateinit var reportNoiseButton: Button

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
        private const val PREFS_NAME = "NoiseReports"
        private const val KEY_REPORTS = "reports"
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

        // Przycisk powrotu do okna głównego
        returnFromMapsButton = findViewById(R.id.returnFromMapsButton)
        returnFromMapsButton.setOnClickListener { finish() }

        // Przycisk zgłaszania hałasu
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
    }

    /**
     * Metoda konfiguruje mapę, w tym umożliwia lokalizację użytkownika na mapie,
     * jeśli aplikacja ma odpowiednie uprawnienia.
     */
    private fun setUpMap() {
        // Sprawdzenie uprawnień do lokalizacji
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            return
        }
        map.isMyLocationEnabled = true
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
     * Metoda służąca do zapisu zgłoszenia hałasu do lokalnej pamięci urządzenia.
     * @param latLng Lokalizacja, w której hałas został zgłoszony.
     * @param noiseLevel Poziom hałasu zgłoszony przez użytkownika.
     */
    private fun saveNoiseReport(latLng: LatLng, noiseLevel: String) {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        sharedPreferences.edit() {
            val reports = JSONArray(sharedPreferences.getString(KEY_REPORTS, "[]"))

            val report = JSONObject()
            report.put("lat", latLng.latitude)
            report.put("lng", latLng.longitude)
            report.put("level", noiseLevel)

            reports.put(report)
            putString(KEY_REPORTS, reports.toString())
        }
    }

    /**
     * Metoda ładuje zgłoszenia hałasu z lokalnej pamięci urządzenia i dodaje markery na mapie.
     */
    private fun loadNoiseReports() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val reports = JSONArray(sharedPreferences.getString(KEY_REPORTS, "[]"))

        for (i in 0 until reports.length()) {
            val report = reports.getJSONObject(i)
            val latLng = LatLng(report.getDouble("lat"), report.getDouble("lng"))
            val noiseLevel = report.getString("level")
            addNoiseMarker(latLng, noiseLevel)
        }
    }

    /**
     * Metoda dodająca marker na mapie dla zgłoszonego hałasu.
     * @param latLng Lokalizacja, w której hałas został zgłoszony.
     * @param noiseLevel Poziom hałasu zgłoszony przez użytkownika.
     */
    private fun addNoiseMarker(latLng: LatLng, noiseLevel: String) {
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title("Poziom hałasu: $noiseLevel")
        map.addMarker(markerOptions)
    }
}
