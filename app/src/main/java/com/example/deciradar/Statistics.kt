package com.example.deciradar
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Statistics : AppCompatActivity() {
    private lateinit var backStatisticsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.statistics)

        backStatisticsButton = findViewById(R.id.BackStatisticsButton)

        backStatisticsButton.setOnClickListener {
            val intent = Intent(this, MainViewApp::class.java)
            startActivity(intent)
            finish()
        }
    }
}