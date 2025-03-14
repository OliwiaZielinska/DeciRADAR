package com.example.deciradar

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.deciradar.R
import com.google.android.material.snackbar.Snackbar
/**
 * Klasa bazowa dla wszystkich aktywności w aplikacji.
 * Zawiera wspólne metody, które mogą być używane przez inne klasy.
 */
open class BaseActivity : AppCompatActivity() {
    /**
     * Wyświetla pasek informacyjny (Snackbar) z komunikatem o błędzie lub sukcesie.
     *
     * @param message Treść komunikatu do wyświetlenia.
     * @param errorMessage Określa, czy jest to komunikat o błędzie (true) czy o sukcesie (false).
     */
    fun showErrorSnackBar(message: String, errorMessage: Boolean) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view
        // Ustawienie koloru tła w zależności od typu komunikatu
        if (errorMessage) {
            snackbarView.setBackgroundColor(
                ContextCompat.getColor(
                    this@BaseActivity,
                    R.color.colorSnackBarError // Kolor dla błędu
                )
            )
        } else {
            snackbarView.setBackgroundColor(
                ContextCompat.getColor(
                    this@BaseActivity,
                    R.color.colorSnackBarSuccess // Kolor dla sukcesu
                )
            )
        }
        snackbar.show()
    }
}