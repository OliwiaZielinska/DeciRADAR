package com.example.deciradar

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
/**
 * Main activity of the application, responsible for handling user interactions
 * on the login and sign-up buttons.
 */
class ActivityMain : AppCompatActivity() {
    private lateinit var logInButtonEnd: Button
    private lateinit var signUpButton1: Button
    /**
     * Called when the activity is first created. It initializes the UI components
     * and sets click listeners for the buttons.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down, this contains the most recent data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logInButtonEnd = findViewById(R.id.logInButtonEnd)
        signUpButton1 = findViewById(R.id.signUpButton1)

        signUpButton1.setOnClickListener {
            Toast.makeText(this, "Kliknięto ZAREJESTRUJ SIĘ", Toast.LENGTH_SHORT).show()
        }

        logInButtonEnd.setOnClickListener {
            Toast.makeText(this, "Kliknięto ZALOGUJ SIĘ", Toast.LENGTH_SHORT).show()
        }
    }
}