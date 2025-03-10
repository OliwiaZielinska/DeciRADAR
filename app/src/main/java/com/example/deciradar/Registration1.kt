package com.example.deciradar
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Registration1 : AppCompatActivity() {
    private lateinit var nameInput: TextView
    private lateinit var surnameInput: TextView
    private lateinit var mailInput: TextView
    private lateinit var passwordInput: TextView
    private lateinit var confirmPasswordInput: TextView
    private lateinit var saveButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_1)

        nameInput = findViewById<EditText>(R.id.RegistrationNameInput)
        surnameInput = findViewById<EditText>(R.id.RegistrationSurnameInput)
        mailInput = findViewById<EditText>(R.id.RegistrationMailInput)
        passwordInput = findViewById<EditText>(R.id.RegistrationPasswordInput)
        confirmPasswordInput = findViewById<EditText>(R.id.RegistrationDuplicatePasswordInput)

        saveButton = findViewById(R.id.SaveRegistration1Button)
        saveButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val surname = surnameInput.text.toString().trim()
            val mail = mailInput.text.toString().trim()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            if (name.isEmpty() || surname.isEmpty() || mail.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Wszystkie pola są wymagane!", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Hasła nie są identyczne!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Rejestracja zakończona!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Registration2::class.java)
                startActivity(intent)
            }
        }

        backButton = findViewById(R.id.BackRegistration1Button)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}