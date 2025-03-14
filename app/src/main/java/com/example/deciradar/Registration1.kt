package com.example.deciradar
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
/**
 * Aktywność rejestracji użytkownika - pierwsza część.
 * Pozwala użytkownikowi na wprowadzenie podstawowych danych, takich jak imię, nazwisko, email i hasło.
 */
class Registration1 : BaseActivity() {
    private lateinit var nameInput: TextView
    private lateinit var surnameInput: TextView
    private lateinit var mailInput: TextView
    private lateinit var passwordInput: TextView
    private lateinit var confirmPasswordInput: TextView
    private lateinit var saveButton: Button
    private lateinit var backButton: Button
    /**
     * Inicjalizacja aktywności.
     * Ustawia widok i przypisuje elementy interfejsu użytkownika do zmiennych.
     */
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
            registerUser()
        }

        backButton = findViewById(R.id.BackRegistration1Button)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    /**
     * Walidacja danych wprowadzonych przez użytkownika.
     * Sprawdza, czy wszystkie pola są wypełnione oraz czy hasła są identyczne.
     *
     * @return true, jeśli dane są poprawne, false w przeciwnym razie.
     */
    private fun validateRegisterDetails(): Boolean {
        val name = nameInput.text.toString().trim()
        val surname = surnameInput.text.toString().trim()
        val mail = mailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()

        if (TextUtils.isEmpty(name)) {
            showErrorSnackBar(getString(R.string.err_msg_enter_name), true)
            return false
        }
        if (TextUtils.isEmpty(surname)) {
            showErrorSnackBar(getString(R.string.err_msg_enter_surname), true)
            return false
        }
        if (TextUtils.isEmpty(mail)) {
            showErrorSnackBar(getString(R.string.err_msg_enter_email), true)
            return false
        }
        if (TextUtils.isEmpty(password)) {
            showErrorSnackBar(getString(R.string.err_msg_enter_password), true)
            return false
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            showErrorSnackBar(getString(R.string.err_msg_enter_confirm_password), true)
            return false
        }
        if (password != confirmPassword) {
            showErrorSnackBar(getString(R.string.err_msg_passwords_do_not_match), true)
            return false
        }
        return true
    }
    /**
     * Rozpoczyna proces rejestracji użytkownika.
     * Jeśli dane są poprawne, przechodzi do drugiej części rejestracji.
     */
    private fun registerUser() {
        if (validateRegisterDetails()) {
            val name = nameInput.text.toString().trim()
            val surname = surnameInput.text.toString().trim()
            val mailInput = mailInput.text.toString().trim()
            val passwordInput = passwordInput.text.toString().trim()

            openActivity(
                name,
                surname,
                mailInput,
                passwordInput
            )
        }
    }
    /**
     * Przekazuje wprowadzone dane użytkownika do kolejnej aktywności rejestracji.
     *
     * @param name Imię użytkownika
     * @param surname Nazwisko użytkownika
     * @param mailInput Adres e-mail użytkownika
     * @param passwordInput Hasło użytkownika
     */
    private fun openActivity(
        name: String,
        surname: String,
        mailInput: String,
        passwordInput: String
    ) {
        val intent = Intent(this, Registration2::class.java)
        intent.putExtra("name", name)
        intent.putExtra("surname", surname)
        intent.putExtra("mail", mailInput)
        intent.putExtra("password", passwordInput)
        startActivity(intent)
    }
}