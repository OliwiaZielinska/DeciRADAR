package com.example.deciradar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ChartActivity : AppCompatActivity() {
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chart)

        // Odebranie przekazanego uID
        userId = intent.getStringExtra("uID")

        // Obsługa przycisku powrotu
        findViewById<Button>(R.id.BackChartButton).setOnClickListener {
            val intent = Intent(this, Statistics::class.java)
            intent.putExtra("uID", userId) // Przekazanie uID z powrotem
            startActivity(intent)
            finish()
        }
    }
}
