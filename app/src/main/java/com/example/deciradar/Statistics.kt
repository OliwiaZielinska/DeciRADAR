package com.example.deciradar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

/**
 * Klasa Statistics odpowiada za wyświetlanie statystyk pomiarów dźwięku.
 * Pozwala użytkownikowi filtrować dane według określonych zakresów czasowych
 * i pobiera dane z Firestore.
 */
class Statistics : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SoundDataAdapter
    private val soundDataList = mutableListOf<SoundData>()
    private lateinit var spinnerFilter: Spinner
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private var userId: String? = null  // Identyfikator użytkownika

    /**
     * Metoda onCreate inicjalizuje widok statystyk oraz komponenty UI.
     * @param savedInstanceState zapisany stan instancji
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.statistics)

        auth = FirebaseAuth.getInstance()
        userId = intent.getStringExtra("uID") ?: auth.currentUser?.uid // Pobranie przekazanego identyfikatora użytkownika

        // Inicjalizacja RecyclerView
        recyclerView = findViewById(R.id.statisticsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SoundDataAdapter(soundDataList)
        recyclerView.adapter = adapter

        // Spinner do filtrowania danych
        spinnerFilter = findViewById(R.id.spinnerFilter)
        val filterOptions = arrayOf("Wszystko", "Tydzień", "Miesiąc", "Rok")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = spinnerAdapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedFilter = filterOptions[position]
                fetchFirestoreData(selectedFilter) // Pobieranie danych z Firestore z wybranym filtrem
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Przycisk powrotu do głównego widoku aplikacji
        findViewById<Button>(R.id.BackStatisticsButton).setOnClickListener {
            val intent = Intent(this, MainViewApp::class.java)
            intent.putExtra("uID", userId) // Przekazanie uID do głównej aktywności
            startActivity(intent)
            finish()
        }

        // Przycisk do przejścia do ChartActivity
        findViewById<Button>(R.id.ChartStatisticsButton).setOnClickListener {
            val intent = Intent(this, ChartActivity::class.java)
            intent.putExtra("uID", userId) // Przekazanie uID do ChartActivity
            startActivity(intent)
        }

        fetchFirestoreData("Wszystko") // Domyślne pobranie wszystkich danych
    }

    /**
     * Pobiera dane pomiarów dźwięku z Firestore i filtruje je według wybranego zakresu czasowego.
     * @param filter Wybrany filtr czasowy (np. "Tydzień", "Miesiąc", "Rok" lub "Wszystko").
     */
    private fun fetchFirestoreData(filter: String) {
        val userId = this.userId ?: return
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val now = Calendar.getInstance()
        val cutoffDate: Date? = when (filter) {
            "Tydzień" -> {
                val c = now.clone() as Calendar
                c.add(Calendar.DAY_OF_YEAR, -7)
                c.time
            }
            "Miesiąc" -> {
                val c = now.clone() as Calendar
                c.add(Calendar.DAY_OF_YEAR, -30)
                c.time
            }
            "Rok" -> {
                val c = now.clone() as Calendar
                c.add(Calendar.YEAR, -1)
                c.time
            }
            else -> null
        }

        db.collection("measurements")
            .whereEqualTo("userID", userId)
            .get()
            .addOnSuccessListener { documents ->
                soundDataList.clear()
                for (document in documents) {
                    val date = document.getString("date") ?: continue
                    val hour = document.getString("hour") ?: continue
                    val soundIntensity = document.getString("soundIntensity") ?: continue

                    try {
                        val intensityValue = soundIntensity.replace(",", ".").toDoubleOrNull()
                        if (intensityValue == null || intensityValue.isInfinite() || intensityValue < 0) {
                            continue // Pomiń nieprawidłowe lub ujemne wartości
                        }

                        val measurementDate = dateFormat.parse(date)
                        if (measurementDate != null && (cutoffDate == null || measurementDate.after(cutoffDate))) {
                            soundDataList.add(
                                SoundData(
                                    date,
                                    formatHour(hour),
                                    formatSoundIntensity(soundIntensity),
                                    userId
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("Firestore", "Błąd parsowania danych", e)
                    if (date.isNotEmpty() && hour.isNotEmpty() && soundIntensity.isNotEmpty()) {
                        try {
                            val intensityValue = soundIntensity.replace(",", ".").toDoubleOrNull()
                            if (intensityValue == null || intensityValue.isInfinite()) {
                                continue // pominięcie wartości infinite
                            }
                            val measurementDate = dateFormat.parse(date)
                            if (measurementDate != null) {
                                val shouldAdd = when (filter) {
                                    "Tydzień" -> {
                                        val tempCalendar = Calendar.getInstance()
                                        tempCalendar.add(Calendar.WEEK_OF_YEAR, -1)
                                        measurementDate.after(tempCalendar.time)
                                    }
                                    "Miesiąc" -> {
                                        val tempCalendar = Calendar.getInstance()
                                        tempCalendar.add(Calendar.MONTH, -1)
                                        measurementDate.after(tempCalendar.time)
                                    }
                                    "Rok" -> {
                                        val tempCalendar = Calendar.getInstance()
                                        tempCalendar.add(Calendar.YEAR, -1)
                                        measurementDate.after(tempCalendar.time)
                                    }
                                    else -> true
                                }

                                if (shouldAdd) {
                                    soundDataList.add(
                                        SoundData(
                                            date,
                                            formatHour(hour),
                                            formatSoundIntensity(soundIntensity),
                                            userId
                                        )
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Firestore", "Błąd parsowania daty", e)
                        }
                    }
                }

                // Sortowanie listy
                val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                soundDataList.sortWith(compareBy {
                    try {
                        dateTimeFormat.parse("${it.date} ${it.hour}")
                    } catch (e: Exception) {
                        Log.e("Sorting", "Błąd parsowania daty i godziny: ${it.date} ${it.hour}", e)
                        null
                    }
                })
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Błąd pobierania danych", e)
            }
    }


    /**
     * Formatuje godzinę do formatu "HH:mm", usuwając sekundy.
     * @param hour Godzina w formacie "HH:mm:ss".
     * @return Sformatowana godzina w formacie "HH:mm".
     */
    private fun formatHour(hour: String): String {
        return try {
            val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputFormat.parse(hour)
            outputFormat.format(date ?: hour)
        } catch (e: Exception) {
            hour // Jeśli wystąpi błąd, zwróć oryginalny string
        }
    }

    /**
     * Formatuje wartość natężenia dźwięku do dwóch miejsc po przecinku.
     * @param soundIntensity Wartość natężenia dźwięku jako string.
     * @return Sformatowana wartość z dwoma miejscami po przecinku.
     */
    private fun formatSoundIntensity(soundIntensity: String): String {
        return try {
            String.format(Locale.US, "%.2f", soundIntensity.replace(",", ".").toDouble())
        } catch (e: Exception) {
            soundIntensity // Jeśli wystąpi błąd, zwróć oryginalny string
        }
    }
}
