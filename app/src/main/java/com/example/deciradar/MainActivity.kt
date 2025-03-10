package com.example.deciradar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginButton: Button = findViewById(R.id.logInButtonEnd)
        val signUpButton: Button = findViewById(R.id.signUpButton1)
        val emailInput: EditText = findViewById(R.id.LoginEmailInput)
        val passwordInput: EditText = findViewById(R.id.LoginPasswordInput)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Proszę wypełnić wszystkie pola", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, MainViewApp::class.java)
                startActivity(intent)
            }
        }

        signUpButton.setOnClickListener {
            val intent = Intent(this, Registration1::class.java)
            startActivity(intent)
        }
    }
}