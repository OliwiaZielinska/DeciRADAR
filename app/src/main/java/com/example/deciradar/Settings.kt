package com.example.deciradar

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.deciradar.CloudFirestore.FirestoreDatabaseOperations
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.MessageDigest

class Settings : BaseActivity() {
    private lateinit var settings1Text: TextView
    private lateinit var startNightModeSettingsText: TextView
    private lateinit var endNightModeSettingsText: TextView
    private lateinit var settings2Text: TextView
    private lateinit var settingsPasswordText: TextView
    private lateinit var settingsDuplicatePasswordText: TextView
    private lateinit var startHourSettingsInput: TextView
    private lateinit var endHourSettingsInput: TextView
    private lateinit var settingsPasswordInput: EditText
    private lateinit var settingsDuplicatePasswordInput: EditText
    private lateinit var backSettingsButton: Button
    private lateinit var saveSettingsButton: Button
    private lateinit var deleteAccountSettingsButton: Button

    val db = FirebaseFirestore.getInstance()
    private val dbOperations = FirestoreDatabaseOperations(db)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)
        val uID = intent.getStringExtra("userID").toString()
        settings1Text = findViewById(R.id.Settings1Text)
        startNightModeSettingsText = findViewById(R.id.StartNightModeSettingsText)
        endNightModeSettingsText = findViewById(R.id.EndNightModeSettingsText)
        settings2Text = findViewById(R.id.Settings2Text)
        settingsPasswordText = findViewById(R.id.SettingsPasswordText)
        settingsDuplicatePasswordText = findViewById(R.id.SetingsDuplicatePasswordText)
        startHourSettingsInput = findViewById(R.id.EndHourSettingsInput)
        endHourSettingsInput = findViewById(R.id.StartHourSettingsInput)
        settingsPasswordInput = findViewById(R.id.SettingsPasswordInput)
        settingsDuplicatePasswordInput = findViewById(R.id.SettingsDuplicatePasswordInput)
        backSettingsButton = findViewById(R.id.BackSettingsButton)
        saveSettingsButton = findViewById(R.id.SaveSettingsButton)
        deleteAccountSettingsButton = findViewById(R.id.DeleteAccountSettingsButton)

        setData()
        startHourSettingsInput.setOnClickListener { showTimePickerDialog(startHourSettingsInput) }
        endHourSettingsInput.setOnClickListener { showTimePickerDialog(endHourSettingsInput) }

        saveSettingsButton.setOnClickListener { saveSettings() }
        deleteAccountSettingsButton.setOnClickListener { deleteAccount() }

        backSettingsButton.setOnClickListener {
            val intent = Intent(this, MainViewApp::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showTimePickerDialog(textView: TextView) {
        val currentTime = textView.text.toString()
        val hour: Int
        val minute: Int

        if (currentTime.isNotEmpty()) {
            val parts = currentTime.split(":")
            hour = parts[0].toInt()
            minute = parts[1].toInt()
        } else {
            hour = 0
            minute = 0
        }

        val timePickerDialog = TimePickerDialog(this,
            { _, selectedHour, selectedMinute ->
                textView.text = String.format("%02d:%02d", selectedHour, selectedMinute)
            }, hour, minute, true
        )
        timePickerDialog.show()
    }

    private fun setData() {
        val userId = FirebaseAuth.getInstance().currentUser!!.email
        val ref = db.collection("users").document(userId.toString())
        ref.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val startNightMode = document.getString("startNightMode")
                val endNightMode = document.getString("endNightMode")

                startHourSettingsInput.text = startNightMode
                endHourSettingsInput.text = endNightMode
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Wystąpił błąd!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveSettings() {
        val startHourSettingsInputValue = startHourSettingsInput.text.toString()
        val endHourSettingsInputValue = endHourSettingsInput.text.toString()
        val settingsPasswordInputValue = settingsPasswordInput.text.toString()
        val settingsDuplicatePasswordInputValue = settingsDuplicatePasswordInput.text.toString()

        if (settingsPasswordInputValue != settingsDuplicatePasswordInputValue) {
            Toast.makeText(this, "Hasła nie są identyczne!", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser!!.email
        val ref = db.collection("users").document(userId.toString())

        ref.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val storedHashedPassword = document.getString("password")
                val newHashedPassword = hashPassword(settingsPasswordInputValue)

                if (storedHashedPassword == newHashedPassword) {
                    Toast.makeText(this, "Nowe hasło nie może być takie samo jak stare!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Uaktualnienie tylko wybranych pól, zamiast nadpisywania całego dokumentu
                val updates = mutableMapOf<String, Any>(
                    "startNightMode" to startHourSettingsInputValue,
                    "endNightMode" to endHourSettingsInputValue
                )

                if (settingsPasswordInputValue.isNotEmpty()) {
                    updates["password"] = newHashedPassword
                }

                ref.update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Zmiana zakończona sukcesem", Toast.LENGTH_SHORT).show()
                        openActivity(
                            startHourSettingsInputValue,
                            endHourSettingsInputValue,
                            settingsPasswordInputValue,
                            userId.toString()
                        )
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Błąd podczas zapisu: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Błąd pobierania danych użytkownika!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteAccount() {
        val userId = FirebaseAuth.getInstance().currentUser!!.email
        GlobalScope.launch(Dispatchers.Main) {
            dbOperations.deleteUser(userId.toString())
        }
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun openActivity(
        startHourSettings: String,
        endHourSettings: String,
        settingsPassword: String,
        userId: String
    ) {
        val intent = Intent(this, MainViewApp::class.java)
        intent.putExtra("uID", userId)
        intent.putExtra("password", settingsPassword)
        intent.putExtra("endNightMode", endHourSettings)
        intent.putExtra("startNightMode", startHourSettings)
        startActivity(intent)
    }
    private fun hashPassword(password: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = messageDigest.digest(password.toByteArray())
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
}
