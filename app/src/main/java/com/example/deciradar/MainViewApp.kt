package com.example.deciradar
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainViewApp : AppCompatActivity() {
    private lateinit var soundMeter: SoundMeter
    private lateinit var decibelTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_view_app)

        val statisticsButton: Button = findViewById(R.id.StatisticsButton)
        val mapsButton: Button = findViewById(R.id.MapsButton)
        val guideButton: Button = findViewById(R.id.GuideButton)
        val settingsButton: Button = findViewById(R.id.SettingsButton)
        val logOutButton: Button = findViewById(R.id.LogOutButton)
        decibelTextView = findViewById(R.id.valueOfDecibelsText)
        soundMeter = SoundMeter()

        if (checkMicrophonePermission()) {
            soundMeter.start()
            val handler = Handler(Looper.getMainLooper())
            handler.post(object : Runnable {
                override fun run() {
                    val dbLevel = soundMeter.getAmplitude()
                    val roundedDbLevel = String.format("%.3f", dbLevel)
                    decibelTextView.text = "$roundedDbLevel"
                    handler.postDelayed(this, 500)
                }
            })
        }

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
    private fun checkMicrophonePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO), 100
                )
                false
            } else {
                true
            }
        } else {
            true
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        soundMeter.stop()
    }
}