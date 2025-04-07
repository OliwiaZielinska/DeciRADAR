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

class Maps : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var returnFromMapsButton: Button
    private lateinit var reportNoiseButton: Button
    private lateinit var firestore: FirebaseFirestore

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

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
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLocation = LatLng(it.latitude, it.longitude)
                    showNoiseReportDialog(userLocation)
                }
            }
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        setUpMap()
        loadNoiseReports()
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            return
        }
        map.isMyLocationEnabled = true
    }

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

    private fun saveNoiseReport(latLng: LatLng, noiseLevel: String) {
        val report = hashMapOf(
            "lat" to latLng.latitude,
            "lng" to latLng.longitude,
            "level" to noiseLevel,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("noise_reports")
            .add(report)
            .addOnSuccessListener {
                Toast.makeText(this, "Zapisano zgłoszenie w Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Błąd zapisu: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadNoiseReports() {
        firestore.collection("noise_reports")
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

    private fun addNoiseMarker(latLng: LatLng, noiseLevel: String) {
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title("Poziom hałasu: $noiseLevel")
        map.addMarker(markerOptions)
    }
}
