package com.example.deciradar
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
/**
 * Klasa Maps odpowiada za wyświetlanie ekranu mapy oraz umożliwia powrót do ekranu głównego aplikacji.
 */
class Maps : AppCompatActivity() {
    private lateinit var returnFromMapsButton: Button
    /**
     * Metoda wywoływana podczas tworzenia aktywności. Inicjalizuje widok oraz ustawia nasłuchiwacz na przycisk powrotu.
     *
     * @param savedInstanceState zapisany stan instancji aktywności (jeśli istnieje)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.maps)
        // Inicjalizacja przycisku powrotu
        returnFromMapsButton = findViewById(R.id.returnFromMapsButton)
        // Obsługa kliknięcia przycisku - powrót do ekranu głównego
        returnFromMapsButton.setOnClickListener {
            val intent = Intent(this, MainViewApp::class.java)
            startActivity(intent)
            finish()
        }
    }
}