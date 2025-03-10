package com.example.deciradar
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainViewApp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_view_app)

        val statisticsButton: Button = findViewById(R.id.StatisticsButton)
        val mapsButton: Button = findViewById(R.id.MapsButton)
        val guideButton: Button = findViewById(R.id.GuideButton)
        val settingsButton: Button = findViewById(R.id.SettingsButton)
        val logOutButton: Button = findViewById(R.id.LogOutButton)

        statisticsButton.setOnClickListener {
            val intent = Intent(this, Statistics::class.java)
            startActivity(intent)
        }

        mapsButton.setOnClickListener {
            val intent = Intent(this, Maps::class.java)
            startActivity(intent)
        }

        guideButton.setOnClickListener {
            val intent = Intent(this, Advices::class.java)
            startActivity(intent)
        }

        settingsButton.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }

        logOutButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}