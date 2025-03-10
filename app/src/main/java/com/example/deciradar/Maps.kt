package com.example.deciradar
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Maps : AppCompatActivity() {
    private lateinit var returnFromMapsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.maps)

        returnFromMapsButton = findViewById(R.id.returnFromMapsButton)

        returnFromMapsButton.setOnClickListener {
            val intent = Intent(this, MainViewApp::class.java)
            startActivity(intent)
            finish()
        }
    }
}