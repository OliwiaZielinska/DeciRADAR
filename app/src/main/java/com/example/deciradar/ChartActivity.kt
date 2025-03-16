package com.example.deciradar

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Aktywność wyświetlająca wykres słupkowy z danymi o poziomie hałasu z ostatnich 30 dni.
 */
class ChartActivity : AppCompatActivity() {
    private var userId: String? = null
    private lateinit var barChart: BarChart

    /**
     * Metoda wywoływana podczas tworzenia aktywności.
     * Inicjalizuje widok i ładuje dane wykresu.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chart)

        userId = intent.getStringExtra("uID")
        barChart = findViewById(R.id.barChart)

        loadChartData()

        findViewById<android.widget.Button>(R.id.BackChartButton).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.SaveChartButton).setOnClickListener {
            showSaveOptions()
        }
    }

    /**
     * Wyświetla okno dialogowe umożliwiające wybór formatu zapisu wykresu.
     */
    private fun showSaveOptions() {
        val options = arrayOf("PNG", "JPEG")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Wybierz format zapisu")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> saveChart("png")
                    1 -> saveChart("jpeg")
                }
            }
            .show()
    }
    /**
     * Zapisuje wykres w wybranym formacie na urządzeniu.
     * @param format Format pliku ("png" lub "jpeg").
     */
    private fun saveChart(format: String) {
        val bitmap = barChart.chartBitmap
        val fileName = "chart_${System.currentTimeMillis()}.$format"
        val file = File(getExternalFilesDir(null), fileName)

        try {
            val fos = FileOutputStream(file)
            when (format) {
                "png" -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                "jpeg" -> bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            }
            fos.flush()
            fos.close()
            saveToGallery(file, format)
            Log.d("ChartActivity", "Wykres zapisano w: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("ChartActivity", "Błąd zapisu wykresu", e)
        }
    }
    /**
     * Zapisuje plik wykresu do galerii urządzenia.
     * @param file Obiekt pliku wykresu.
     * @param format Format pliku (PNG lub JPEG).
     */
    private fun saveToGallery(file: File, format: String) {
        val values = android.content.ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/$format")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DeciRadar")
        }
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.let { uri ->
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                FileInputStream(file).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
    }


    /**
     * Pobiera dane z Firestore i generuje wykres na podstawie pomiarów dźwięku.
     */
    private fun loadChartData() {
        if (userId.isNullOrEmpty()) return

        val db = FirebaseFirestore.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance().time
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -30)
        val thirtyDaysAgo = cal.time

        db.collection("measurements")
            .whereEqualTo("userID", userId)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("ChartActivity", "Pobrano ${documents.size()} dokumentów dla userID: $userId")

                // Filtrowanie dokumentów według daty
                val filteredDocs = documents.documents.filter { doc ->
                    val dateStr = doc.getString("date")?.trim()
                    if (!dateStr.isNullOrEmpty()) {
                        try {
                            val docDate = sdf.parse(dateStr)
                            docDate != null && docDate >= thirtyDaysAgo && docDate <= today
                        } catch (e: Exception) {
                            Log.e("ChartActivity", "Błąd parsowania daty: $dateStr", e)
                            false
                        }
                    } else false
                }

                val dateToIntensities = mutableMapOf<String, MutableList<Float>>()

                // Grupowanie natężeń dźwięku według daty
                for (document in filteredDocs) {
                    val dateStr = document.getString("date")?.trim()
                    val intensityStr = document.getString("soundIntensity")?.trim()
                    if (!dateStr.isNullOrEmpty() && !intensityStr.isNullOrEmpty()) {
                        val intensityValue = intensityStr.replace(",", ".").toFloatOrNull()
                        if (intensityValue != null && intensityValue.isFinite()) {
                            dateToIntensities.getOrPut(dateStr) { mutableListOf() }.add(intensityValue)
                        }
                    }
                }

                // Tworzenie listy wartości dla wykresu
                val sortedDates = dateToIntensities.keys.sorted()
                val entries = mutableListOf<BarEntry>()
                for ((index, date) in sortedDates.withIndex()) {
                    val intensities = dateToIntensities[date]
                    if (!intensities.isNullOrEmpty()) {
                        val avgIntensity = intensities.average().toFloat()
                        entries.add(BarEntry(index.toFloat(), avgIntensity))
                    }
                }

                if (entries.isNotEmpty()) {
                    val dataSet = BarDataSet(entries, "Średnie natężenie dźwięku")
                    dataSet.color = Color.parseColor("#915DC0AC")

                    val barData = BarData(dataSet)
                    barData.barWidth = 0.7f
                    barData.setValueTypeface(Typeface.DEFAULT_BOLD)
                    barChart.data = barData

                    // Konfiguracja osi X
                    barChart.xAxis.apply {
                        val maxLabels = 7
                        val step = if (sortedDates.size > maxLabels) sortedDates.size / maxLabels else 1
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                val index = value.toInt()
                                return if (index in sortedDates.indices && index % step == 0) sortedDates[index] else ""
                            }
                        }
                        position = XAxis.XAxisPosition.BOTTOM
                        granularity = 1f
                        labelRotationAngle = 45f
                    }

                    barChart.setExtraBottomOffset(20f)
                    barChart.axisRight.isEnabled = false
                    barChart.description.isEnabled = false

                    // Konfiguracja legendy wykresu
                    barChart.legend.apply {
                        verticalAlignment = Legend.LegendVerticalAlignment.TOP
                        horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                        orientation = Legend.LegendOrientation.HORIZONTAL
                        setDrawInside(false)
                        yOffset = 10f
                        xOffset = 10f
                        typeface = Typeface.DEFAULT_BOLD
                        textColor = Color.parseColor("#3E4C57")
                    }

                    // Konfiguracja osi Y
                    barChart.axisLeft.apply {
                        axisMinimum = 0f
                        axisMaximum = 120f
                        textSize = 12f
                        setDrawGridLines(true)
                        setDrawLabels(true)
                        setDrawAxisLine(true)
                        granularity = 10f
                        labelCount = 6
                        textColor = Color.BLACK
                        typeface = Typeface.DEFAULT_BOLD
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return "$value dB"
                            }
                        }
                    }

                    // Dodanie czerwonej linii oznaczającej próg szkodliwości hałasu
                    val limitLine = LimitLine(85f, "Próg szkodliwości hałasu").apply {
                        lineColor = Color.RED
                        lineWidth = 2f
                        textSize = 12f
                        textColor = Color.RED
                        enableDashedLine(10f, 5f, 0f)
                        labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                    }

                    // Dodanie limitLine do osi Y
                    barChart.axisLeft.addLimitLine(limitLine)

                    barChart.animateY(1000)
                    barChart.invalidate()

                    barChart.post {
                        barChart.zoom(0.8f, 0.8f, (barChart.width / 2).toFloat(), (barChart.height / 2).toFloat())
                    }
                } else {
                    barChart.clear()
                    barChart.setNoDataText("Brak danych do wyświetlenia")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ChartActivity", "Błąd pobierania danych", exception)
            }
    }
}
