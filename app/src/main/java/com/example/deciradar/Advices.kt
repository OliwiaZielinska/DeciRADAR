package com.example.deciradar
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Advices : AppCompatActivity() {
    private lateinit var adviceTextView: TextView
    private lateinit var drawAdviceButton: Button
    private lateinit var backGuideButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.advices)

        adviceTextView = findViewById(R.id.AdviceTextView)
        drawAdviceButton = findViewById(R.id.drawAdviceButton)
        backGuideButton = findViewById(R.id.BackGuideButton)

        backGuideButton.setOnClickListener {
            val intent = Intent(this, MainViewApp::class.java)
            startActivity(intent)
            finish()
        }
    }
}