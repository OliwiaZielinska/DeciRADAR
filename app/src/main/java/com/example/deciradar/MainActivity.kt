package com.example.deciradar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

/**
 * Klasa `MainActivity` odpowiada za ekran logowania użytkownika.
 *
 * Obsługuje logowanie przy pomocy Firebase Authentication, przejście do rejestracji,
 * sprawdzenie uprawnień (w tym powiadomień dla Androida 13+), oraz podstawową walidację pól wejściowych.
 */
class MainActivity : BaseActivity() {
    private var loginButton: Button? = null // Przycisk logowania
    private var signUpButton: Button? = null // Przycisk rejestracji
    private var emailInput: EditText? = null // Pole wejściowe dla e-maila
    private var passwordInput: EditText? = null // Pole wejściowe dla hasła

    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 100

    /**
     * Metoda onCreate inicjalizuje komponenty UI oraz obsługuje kliknięcia przycisków.
     *
     * @param savedInstanceState zapisany stan instancji aktywności (jeśli istnieje)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Sprawdzenie i żądanie uprawnienia POST_NOTIFICATIONS dla Androida 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }

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
     * Obsługuje odpowiedź użytkownika na prośbę o przyznanie uprawnień.
     *
     * @param requestCode Kod żądania uprawnień.
     * @param permissions Lista żądanych uprawnień.
     * @param grantResults Wyniki (przyznane lub nie) dla każdego uprawnienia.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Uprawnienie przyznane
            } else {
                // Uprawnienie nie zostało przyznane
            }
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
