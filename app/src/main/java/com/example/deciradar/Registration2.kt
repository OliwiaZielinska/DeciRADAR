package com.example.deciradar
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Registration2 : AppCompatActivity() {

    private lateinit var startHourTextView: TextView
    private lateinit var endHourTextView: TextView
    private lateinit var startNightModeTextView: TextView
    private lateinit var endNightModeTextView: TextView
    private lateinit var saveButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_2)

        startHourTextView = findViewById(R.id.StartHourRegistration2Input)
        endHourTextView = findViewById(R.id.EndHourRegistration2Input)
        startNightModeTextView = findViewById(R.id.StartNightModeRegistration2Text)
        endNightModeTextView = findViewById(R.id.EndNightModeRegistration2Text)
        saveButton = findViewById(R.id.SaveRegistration2Button)
        backButton = findViewById(R.id.BackRegistration2Button)

        saveButton.setOnClickListener {
            Toast.makeText(this, "Rejestracja zakończona!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        backButton.setOnClickListener {
            val intent = Intent(this, Registration1::class.java)
            startActivity(intent)
            finish()
        }
    }
}