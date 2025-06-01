package com.example.deciradar

import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import java.io.IOException
import java.util.*

/**
 * Aktywność odpowiedzialna za wyświetlanie szczegółów pomiaru dźwięku.
 * Pobiera dane przekazane z poprzedniej aktywności (data, godzina, natężenie, lokalizacja)
 * i wyświetla je użytkownikowi. Umożliwia również powrót do poprzedniego ekranu.
 */
class DetailsActivity : AppCompatActivity() {

    /**
     * Metoda wywoływana przy tworzeniu aktywności.
     * Inicjalizuje widoki, przypisuje dane i ustawia nasłuchiwacz kliknięcia na przycisk powrotu.
     *
     * @param savedInstanceState Stan zapisany podczas poprzedniego cyklu życia aktywności (jeśli istniał).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        // Pobranie danych z Intentu przekazanych z poprzedniej aktywności
        val date = intent.getStringExtra("date")
        val hour = intent.getStringExtra("hour")
        val intensity = intent.getStringExtra("intensity")
        val lat = intent.getDoubleExtra("lat", 0.0)
        val lng = intent.getDoubleExtra("lng", 0.0)

        // Inicjalizacja elementów interfejsu
        val textDate: TextView = findViewById(R.id.textDate)
        val textHour: TextView = findViewById(R.id.textHour)
        val textIntensity: TextView = findViewById(R.id.textIntensity)
        val textLocation: TextView = findViewById(R.id.textLocation)

        // Ustawienie wartości w widokach tekstowych
        textDate.text = "Data: $date"
        textHour.text = "Godzina: $hour"
        textIntensity.text = "Natężenie: $intensity dB"

        // Geokodowanie współrzędnych na adres tekstowy
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            val address = if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0)
            } else {
                "Nieznany adres"
            }
            textLocation.text = "Adres: $address"
        } catch (e: IOException) {
            textLocation.text = "Błąd geokodowania"
        }

        // Obsługa przycisku powrotu – zamyka bieżącą aktywność
        val buttonBack: ImageButton = findViewById(R.id.buttonBack)
        buttonBack.setOnClickListener {
            finish()
        }
    }
}
