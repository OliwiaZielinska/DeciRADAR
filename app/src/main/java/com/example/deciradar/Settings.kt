package com.example.deciradar
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Settings : AppCompatActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings)

        settings1Text = findViewById(R.id.Settings1Text)
        startNightModeSettingsText = findViewById(R.id.StartNightModeSettingsText)
        endNightModeSettingsText = findViewById(R.id.EndNightModeSettingsText)
        settings2Text = findViewById(R.id.Settings2Text)
        settingsPasswordText = findViewById(R.id.SettingsPasswordText)
        settingsDuplicatePasswordText = findViewById(R.id.SetingsDuplicatePasswordText)
        startHourSettingsInput = findViewById(R.id.StartHourSettingsInput)
        endHourSettingsInput = findViewById(R.id.EndHourSettingsInput)
        settingsPasswordInput = findViewById(R.id.SettingsPasswordInput)
        settingsDuplicatePasswordInput = findViewById(R.id.SettingsDuplicatePasswordInput)
        backSettingsButton = findViewById(R.id.BackSettingsButton)
        saveSettingsButton = findViewById(R.id.SaveSettingsButton)
        deleteAccountSettingsButton = findViewById(R.id.DeleteAccountSettingsButton)

        backSettingsButton.setOnClickListener {
            val intent = Intent(this, MainViewApp::class.java)
            startActivity(intent)
            finish()
        }
    }
}