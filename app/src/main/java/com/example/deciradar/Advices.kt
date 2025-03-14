package com.example.deciradar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

/**
 * Klasa odpowiedzialna za wyświetlanie porad dotyczących zdrowia słuchu.
 * Pobiera porady z bazy danych Firestore i pozwala użytkownikowi na losowe wybranie jednej z nich.
 */
class Advices : AppCompatActivity() {
    private lateinit var adviceTextView: TextView
    private lateinit var drawAdviceButton: Button
    private lateinit var backGuideButton: Button
    private lateinit var firestore: FirebaseFirestore
    private var hearingTips = mutableListOf<String>()

    /**
     * Metoda wywoływana podczas tworzenia aktywności.
     * Inicjalizuje widoki oraz pobiera porady z bazy Firestore.
     * @param savedInstanceState Zapisany stan instancji, jeśli istnieje.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.advices)

        // Inicjalizacja widoków
        adviceTextView = findViewById(R.id.AdviceTextView)
        drawAdviceButton = findViewById(R.id.drawAdviceButton)
        backGuideButton = findViewById(R.id.BackGuideButton)
        firestore = FirebaseFirestore.getInstance()

        // Pobranie porad z Firestore
        loadHearingTips()

        // Obsługa kliknięcia przycisku losowania porady
        drawAdviceButton.setOnClickListener {
            displayRandomAdvice()
        }

        // Obsługa kliknięcia przycisku powrotu do głównej aktywności
        backGuideButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.email ?: "Nieznany użytkownik"
            openMainActivity(userId)
        }
    }

    /**
     * Pobiera porady dotyczące zdrowia słuchu z kolekcji Firestore i zapisuje je w liście.
     * Jeśli pobranie nie powiedzie się, wyświetlany jest komunikat błędu.
     */
    private fun loadHearingTips() {
        firestore.collection("HearingTips")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val advice = document.getString("text")
                    if (advice != null) {
                        hearingTips.add(advice)
                    }
                }
                displayRandomAdvice()
            }
            .addOnFailureListener { exception ->
                Log.e("Advices", "Nie udało się pobrać porad o zdrowym słuchu", exception)
                Toast.makeText(this, "Nie udało się pobrać porad o zdrowym słuchu.", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Wyświetla losową poradę z listy pobranych porad.
     * Jeśli lista jest pusta, wyświetla komunikat informujący o braku porad.
     */
    private fun displayRandomAdvice() {
        if (hearingTips.isNotEmpty()) {
            val randomAdvice = hearingTips[Random.nextInt(hearingTips.size)]
            adviceTextView.text = randomAdvice
        } else {
            adviceTextView.text = "Brak porad dotyczących zdrowego słuchu."
        }
    }

    /**
     * Otwiera główną aktywność aplikacji `MainViewApp`, przekazując identyfikator użytkownika.
     * @param userID Adres e-mail aktualnie zalogowanego użytkownika.
     */
    private fun openMainActivity(userID: String) {
        val intent = Intent(this, MainViewApp::class.java)
        intent.putExtra("uID", userID)
        startActivity(intent)
        finish()
    }
}