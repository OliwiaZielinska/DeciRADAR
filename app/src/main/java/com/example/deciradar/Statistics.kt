package com.example.deciradar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

/**
 * Klasa Statistics - odpowiedzialna za wyświetlanie ekranu statystyk aplikacji.
 * Umożliwia użytkownikowi powrót do głównej aktywności aplikacji.
 */
class Statistics : AppCompatActivity() {

    /**
     * Przycisk umożliwiający powrót do głównej aktywności.
     */
    private lateinit var backStatisticsButton: Button

    /**
     * Metoda onCreate - inicjalizuje komponenty widoku i ustawia funkcjonalność przycisku powrotu.
     * @param savedInstanceState Zapisany stan instancji aktywności, jeśli dostępny.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.statistics)

        // Inicjalizacja przycisku powrotu
        backStatisticsButton = findViewById(R.id.BackStatisticsButton)

        // Obsługa kliknięcia przycisku powrotu do ekranu głównego
        backStatisticsButton.setOnClickListener {
            val intent = Intent(this, MainViewApp::class.java)
            startActivity(intent)
            finish()
        }
    }
}