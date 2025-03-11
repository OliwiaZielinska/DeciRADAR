package com.example.deciradar
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.deciradar.CloudFirestore.FirestoreDatabaseOperations
import com.example.deciradar.CloudFirestore.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.Calendar

class Registration2 : BaseActivity() {

    private lateinit var startHourTextView: TextView
    private lateinit var endHourTextView: TextView
    private lateinit var startNightModeTextView: TextView
    private lateinit var endNightModeTextView: TextView
    private lateinit var saveButton: Button
    private lateinit var backButton: Button
    private lateinit var dbOperations: FirestoreDatabaseOperations
    private val database = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration_2)
        val name = intent.getStringExtra("name")
        val surname = intent.getStringExtra("surname")
        val mail = intent.getStringExtra("mail")
        val password = intent.getStringExtra("password")

        startHourTextView = findViewById(R.id.EndHourRegistration2Input)
        endHourTextView = findViewById(R.id.StartHourRegistration2Input)
        startNightModeTextView = findViewById(R.id.StartNightModeRegistration2Text)
        endNightModeTextView = findViewById(R.id.EndNightModeRegistration2Text)
        saveButton = findViewById(R.id.SaveRegistration2Button)
        backButton = findViewById(R.id.BackRegistration2Button)
        dbOperations = FirestoreDatabaseOperations(database)

        startHourTextView.setOnClickListener { showTimePickerDialog(startHourTextView) }
        endHourTextView.setOnClickListener { showTimePickerDialog(endHourTextView) }

        saveButton.setOnClickListener {
            registerUser(
                name.toString(),
                surname.toString(),
                mail.toString(),
                password.toString(),
                startHourTextView.text.toString(),
                endHourTextView.text.toString()
            )
        }

        backButton.setOnClickListener {
            val intent = Intent(this, Registration1::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showTimePickerDialog(textView: TextView) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            textView.text = formattedTime
        }, hour, minute, true)

        timePickerDialog.show()
    }

    private fun registerUser(
        name: String,
        surname: String,
        mail: String,
        password: String,
        startHourTextView: String,
        endHourTextView: String
    ) {
        val hashedPassword = hashPassword(password)
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(mail, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    showErrorSnackBar(
                        "Zarejestrowano pomyślnie. Twoje id to ${firebaseUser.uid}",
                        false
                    )

                    FirebaseAuth.getInstance().signOut()
                    finish()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    showErrorSnackBar(task.exception!!.message.toString(), true)
                }
            }
        val user = User(
            name,
            surname,
            mail,
            hashedPassword,
            startHourTextView,
            endHourTextView
        )
        GlobalScope.launch(Dispatchers.Main) {
            dbOperations.addUser(mail, user) // Użycie dbOperations
        }
    }

    private fun hashPassword(password: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = messageDigest.digest(password.toByteArray())
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
}