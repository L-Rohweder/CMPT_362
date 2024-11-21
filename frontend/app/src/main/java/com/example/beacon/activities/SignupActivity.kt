package com.example.beacon.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.beacon.R
import com.example.beacon.utils.Constants.BACKEND_IP
import org.json.JSONObject

class SignupActivity : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var firstnameInput: EditText
    private lateinit var lastnameInput: EditText
    private lateinit var signupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        emailInput = findViewById(R.id.emailInput)
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        firstnameInput = findViewById(R.id.firstnameInput)
        lastnameInput = findViewById(R.id.lastnameInput)
        signupButton = findViewById(R.id.signupButton)

        signupButton.setOnClickListener {
            val email = emailInput.text.toString()
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()
            val firstname = firstnameInput.text.toString()
            val lastname = lastnameInput.text.toString()

            if (email.isBlank() || username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performSignup(email, username, password, firstname, lastname)
        }
    }

    private fun performSignup(email: String, username: String, password: String, 
                            firstname: String, lastname: String) {
        val requestQueue = Volley.newRequestQueue(this)
        val url = "$BACKEND_IP/postUser"

        val body = JSONObject().apply {
            put("email", email)
            put("username", username)
            put("password", password)
            put("firstname", firstname)
            put("lastname", lastname)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, body,
            { _ ->
                Toast.makeText(
                    this,
                    "Registration successful! Please login.",
                    Toast.LENGTH_LONG
                ).show()
                finish() // Return to login screen
            },
            { error ->
                Toast.makeText(
                    this,
                    "Registration failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            })

        requestQueue.add(request)
    }
} 