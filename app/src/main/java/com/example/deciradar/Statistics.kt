package com.example.deciradar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

/**
 * Aktywność Statistics odpowiada za prezentację statystyk pomiarów dźwięku.
 * Umożliwia użytkownikowi filtrowanie danych na podstawie okresu czasu (tydzień, miesiąc, rok),
 * a także przejście do szczegółów pomiaru lub wykresu.
 */
class Statistics : AppCompatActivity() {

    // RecyclerView do wyświetlania listy pomiarów
    private lateinit var recyclerView: RecyclerView

    // Adapter do obsługi danych pomiarów
    private lateinit var adapter: SoundDataAdapter

    // Lista przechowująca dane pomiarów dźwięku
    private val soundDataList = mutableListOf<SoundData>()

    // Spinner służący do wyboru filtra czasowego
    private lateinit var spinnerFilter: Spinner

    // Instancja Firebase Authentication
    private lateinit var auth: FirebaseAuth

    // Instancja bazy danych Firestore
    private val db = FirebaseFirestore.getInstance()

    // Identyfikator aktualnie zalogowanego użytkownika
    private var userId: String? = null

    /**
     * Metoda onCreate inicjalizuje widok aktywności oraz wszystkie komponenty UI.
     * @param savedInstanceState zapisany stan instancji (jeśli istnieje)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.statistics)

        auth = FirebaseAuth.getInstance()
        userId = intent.getStringExtra("uID") ?: auth.currentUser?.uid // Pobranie ID użytkownika

        // Inicjalizacja komponentów RecyclerView
        recyclerView = findViewById(R.id.statisticsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SoundDataAdapter(soundDataList, object : OnSoundDataInteractionListener {

            /**
             * Obsługa kliknięcia elementu listy – przejście do szczegółów pomiaru.
             * @param data dane pojedynczego pomiaru
             */
            override fun onItemClick(data: SoundData) {
                val intent = Intent(this@Statistics, DetailsActivity::class.java)
                intent.putExtra("date", data.date)
                intent.putExtra("hour", data.hour)
                intent.putExtra("intensity", data.soundIntensity)
                intent.putExtra("lat", data.lat ?: 0.0)
                intent.putExtra("lng", data.lng ?: 0.0)
                startActivity(intent)
            }

            /**
             * Obsługa usuwania elementu z listy i bazy Firestore.
             * @param data dane pomiaru do usunięcia
             * @param position pozycja elementu w liście
             */
            override fun onItemDelete(data: SoundData, position: Int) {
                soundDataList.removeAt(position)
                adapter.notifyItemRemoved(position)

                data.documentId?.let { docId ->
                    db.collection("measurements")
                        .document(docId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this@Statistics, "Pomiar usunięty", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@Statistics, "Błąd usuwania pomiaru", Toast.LENGTH_SHORT).show()
                            Log.e("Firestore", "Błąd usuwania dokumentu", e)
                        }
                } ?: run {
                    Toast.makeText(this@Statistics, "Brak ID dokumentu, nie można usunąć", Toast.LENGTH_SHORT).show()
                }
            }
        })
        recyclerView.adapter = adapter

        // Inicjalizacja spinnera z filtrami czasowymi
        spinnerFilter = findViewById(R.id.spinnerFilter)
        val filterOptions = arrayOf("Wszystko", "Tydzień", "Miesiąc", "Rok")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = spinnerAdapter

        // Obsługa wyboru filtra w spinnerze
        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedFilter = filterOptions[position]
                fetchFirestoreData(selectedFilter) // Pobierz dane z Firestore na podstawie wybranego filtra
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Obsługa przycisku powrotu do głównej aktywności aplikacji
        findViewById<Button>(R.id.BackStatisticsButton).setOnClickListener {
            val intent = Intent(this, MainViewApp::class.java)
            intent.putExtra("uID", userId) // Przekazanie identyfikatora użytkownika
            startActivity(intent)
            finish()
        }

        // Obsługa przycisku przejścia do aktywności z wykresem
        findViewById<Button>(R.id.ChartStatisticsButton).setOnClickListener {
            val intent = Intent(this, ChartActivity::class.java)
            intent.putExtra("uID", userId) // Przekazanie identyfikatora użytkownika
            startActivity(intent)
        }

        // Domyślne pobranie wszystkich danych
        fetchFirestoreData("Wszystko")
    }

    /**
     * Pobiera dane pomiarów użytkownika z Firestore i filtruje je według wybranego przedziału czasowego.
     * @param filter Filtr czasowy – "Tydzień", "Miesiąc", "Rok" lub "Wszystko".
     */
    private fun fetchFirestoreData(filter: String) {
        val userId = this.userId ?: return
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val now = Calendar.getInstance()

        // Obliczenie daty granicznej w zależności od wybranego filtra
        val cutoffDate: Date? = when (filter) {
            "Tydzień" -> Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.time
            "Miesiąc" -> Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }.time
            "Rok" -> Calendar.getInstance().apply { add(Calendar.YEAR, -1) }.time
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
                    val lat = document.getDouble("lat")
                    val lng = document.getDouble("lng")
                    val documentId = document.id

                    try {
                        val intensityValue = soundIntensity.replace(",", ".").toDoubleOrNull()
                        if (intensityValue == null || intensityValue.isInfinite() || intensityValue < 0) continue

                        val measurementDate = dateFormat.parse(date)
                        if (measurementDate != null && (cutoffDate == null || measurementDate.after(cutoffDate))) {
                            soundDataList.add(
                                SoundData(
                                    date,
                                    formatHour(hour),
                                    formatSoundIntensity(soundIntensity),
                                    userId,
                                    lat,
                                    lng,
                                    documentId
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("Firestore", "Błąd parsowania danych", e)
                    }
                }

                // Sortowanie listy według daty i godziny
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
     * Formatuje godzinę do uproszczonego formatu "HH:mm".
     * @param hour Godzina w formacie "HH:mm:ss".
     * @return Godzina w formacie "HH:mm".
     */
    private fun formatHour(hour: String): String {
        return try {
            val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputFormat.parse(hour)
            outputFormat.format(date ?: hour)
        } catch (e: Exception) {
            hour
        }
    }

    /**
     * Formatuje natężenie dźwięku jako liczbę zmiennoprzecinkową z dwoma miejscami po przecinku.
     * @param soundIntensity wartość natężenia dźwięku jako String.
     * @return Sformatowana wartość natężenia.
     */
    private fun formatSoundIntensity(soundIntensity: String): String {
        return try {
            String.format(Locale.US, "%.2f", soundIntensity.replace(",", ".").toDouble())
        } catch (e: Exception) {
            soundIntensity
        }
    }
}
