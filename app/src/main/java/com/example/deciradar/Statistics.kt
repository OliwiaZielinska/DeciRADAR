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

    /**
     * Metoda onCreate inicjalizuje widok statystyk oraz komponenty UI.
     * @param savedInstanceState zapisany stan instancji
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.statistics)

        auth = FirebaseAuth.getInstance()

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
            startActivity(intent)
            finish()
        }

        fetchFirestoreData("Wszystko") // Domyślne pobranie wszystkich danych
    }

    /**
     * Pobiera dane pomiarów dźwięku z Firestore i filtruje je według wybranego zakresu czasowego.
     * @param filter Wybrany filtr czasowy (np. "Tydzień", "Miesiąc", "Rok" lub "Wszystko").
     */
    private fun fetchFirestoreData(filter: String) {
        val userId = auth.currentUser?.uid ?: return
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        db.collection("measurements")
            .whereEqualTo("userID", userId)
            .get()
            .addOnSuccessListener { documents ->
                soundDataList.clear()
                for (document in documents) {
                    val date = document.getString("date") ?: ""
                    val hour = document.getString("hour") ?: ""
                    val soundIntensity = document.getString("soundIntensity") ?: ""

                    if (date.isNotEmpty() && hour.isNotEmpty() && soundIntensity.isNotEmpty()) {
                        try {
                            val measurementDate = dateFormat.parse(date)
                            if (measurementDate != null) {
                                val shouldAdd = when (filter) {
                                    "Tydzień" -> {
                                        calendar.add(Calendar.WEEK_OF_YEAR, -1)
                                        measurementDate.after(calendar.time)
                                    }
                                    "Miesiąc" -> {
                                        calendar.add(Calendar.MONTH, -1)
                                        measurementDate.after(calendar.time)
                                    }
                                    "Rok" -> {
                                        calendar.add(Calendar.YEAR, -1)
                                        measurementDate.after(calendar.time)
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
                // Sortowanie listy według daty i godziny w kolejności rosnącej
                val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                soundDataList.sortWith(compareBy { soundData ->
                    try {
                        dateTimeFormat.parse("${soundData.date} ${soundData.hour}")
                    } catch (e: Exception) {
                        Log.e("Sorting", "Błąd parsowania daty i godziny: ${soundData.date} ${soundData.hour}", e)
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
     * Formatuje godzinę do formatu "gg:mm", usuwając sekundy.
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