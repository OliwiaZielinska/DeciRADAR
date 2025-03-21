package com.example.deciradar

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

/**
 * Klasa MainActivity obsługuje ekran logowania użytkownika.
 * Umożliwia logowanie oraz przejście do rejestracji nowego konta.
 */
class MainActivity : BaseActivity() {
    private var loginButton: Button? = null // Przycisk logowania
    private var signUpButton: Button? = null // Przycisk rejestracji
    private var emailInput: EditText? = null // Pole wejściowe dla e-maila
    private var passwordInput: EditText? = null // Pole wejściowe dla hasła

    /**
     * Metoda onCreate inicjalizuje komponenty UI oraz obsługuje kliknięcia przycisków.
     *
     * @param savedInstanceState zapisany stan instancji aktywności (jeśli istnieje)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicjalizacja elementów interfejsu
        loginButton = findViewById(R.id.logInButtonEnd)
        signUpButton = findViewById(R.id.signUpButton1)
        emailInput = findViewById(R.id.LoginEmailInput)
        passwordInput = findViewById(R.id.LoginPasswordInput)

        // Obsługa kliknięcia przycisku logowania
        loginButton?.setOnClickListener {
            loginRegisteredUser()
        }

        // Obsługa kliknięcia przycisku rejestracji
        signUpButton?.setOnClickListener {
            val intent = Intent(this, Registration1::class.java)
            startActivity(intent)
        }
    }

    /**
     * Metoda sprawdzająca, czy użytkownik poprawnie wypełnił pola e-maila i hasła.
     *
     * @return true, jeśli dane są poprawne, false w przeciwnym razie.
     */
    private fun validateLoginDetails(): Boolean {
        val email = emailInput?.text.toString().trim()
        val password = passwordInput?.text.toString().trim()

        return when {
            // Sprawdzenie, czy pole e-mail nie jest puste
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            // Sprawdzenie, czy e-mail zawiera znak '@'
            !email.contains("@") -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_invalid_email), true)
                false
            }
            // Sprawdzenie, czy pole hasła nie jest puste
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> {
                showErrorSnackBar("Wprowadzone dane logowania są poprawne", false)
                true
            }
        }
    }

    /**
     * Metoda obsługująca logowanie użytkownika za pomocą Firebase Authentication.
     * Jeśli dane logowania są poprawne, użytkownik zostaje przekierowany do ekranu głównego aplikacji.
     */
    private fun loginRegisteredUser() {
        if (validateLoginDetails()) {
            val email = emailInput?.text.toString().trim()
            val password = passwordInput?.text.toString().trim()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showErrorSnackBar(resources.getString(R.string.login_successfull), false)
                        goToMainActivity(email)
                        finish()
                    } else {
                        // Jeśli wystąpił błąd uwierzytelniania, przechwytujemy wyjątek i wyświetlamy komunikat.
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            showErrorSnackBar("Podany login lub hasło są nieprawidłowe.", true)
                        } else {
                            showErrorSnackBar("Wystąpił błąd: ${task.exception?.message}", true)
                        }
                    }
                }
        }
    }

    /**
     * Metoda przekierowująca zalogowanego użytkownika do głównego ekranu aplikacji.
     *
     * @param email adres e-mail zalogowanego użytkownika
     */
    open fun goToMainActivity(email: String) {
        val intent = Intent(this, MainViewApp::class.java)
        intent.putExtra("uID", email)
        startActivity(intent)
    }
}
