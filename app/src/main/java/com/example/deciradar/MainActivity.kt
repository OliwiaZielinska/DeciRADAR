package com.example.deciradar

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity() {
    private var loginButton: Button? = null
    private var signUpButton: Button? = null
    private var emailInput: EditText? = null
    private var passwordInput: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loginButton = findViewById(R.id.logInButtonEnd)
        signUpButton = findViewById(R.id.signUpButton1)
        emailInput = findViewById(R.id.LoginEmailInput)
        passwordInput = findViewById(R.id.LoginPasswordInput)

        loginButton?.setOnClickListener {
            loginRegisteredUser()
        }

        signUpButton?.setOnClickListener {
            val intent = Intent(this, Registration1::class.java)
            startActivity(intent)
        }
    }
    private fun validateLoginDetails(): Boolean {
        return when {
            TextUtils.isEmpty(emailInput?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(passwordInput?.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> {
                showErrorSnackBar("Wprowadzone dane logowania są poprawne", false)
                true
            }
        }
    }
    private fun loginRegisteredUser() {
        if (validateLoginDetails()) {
            val email = emailInput?.text.toString().trim { it <= ' ' }
            val password = passwordInput?.text.toString().trim { it <= ' ' }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showErrorSnackBar(resources.getString(R.string.login_successfull), false)
                        goToMainActivity(email)
                        finish()
                    } else {
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }
    open fun goToMainActivity(email: String) {
        val intent = Intent(this, MainViewApp::class.java)
        intent.putExtra("uID", email)
        startActivity(intent)
    }
}
