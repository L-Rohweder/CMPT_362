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

    private fun validateInputs(): Boolean {
        val email = emailInput.text.toString()
        val username = usernameInput.text.toString()
        val password = passwordInput.text.toString()
        val firstname = firstnameInput.text.toString()
        val lastname = lastnameInput.text.toString()

        when {
            email.isBlank() -> {
                emailInput.error = "Email cannot be empty"
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailInput.error = "Invalid email format"
                return false
            }
            username.isBlank() -> {
                usernameInput.error = "Username cannot be empty"
                return false
            }
            username.length < 3 -> {
                usernameInput.error = "Username must be at least 3 characters"
                return false
            }
            password.isBlank() -> {
                passwordInput.error = "Password cannot be empty"
                return false
            }
            password.length < 6 -> {
                passwordInput.error = "Password must be at least 6 characters"
                return false
            }
            !password.matches(".*[A-Z].*".toRegex()) -> {
                passwordInput.error = "Password must contain at least one uppercase letter"
                return false
            }
            !password.matches(".*[0-9].*".toRegex()) -> {
                passwordInput.error = "Password must contain at least one number"
                return false
            }
            firstname.isBlank() -> {
                firstnameInput.error = "First name cannot be empty"
                return false
            }
            lastname.isBlank() -> {
                lastnameInput.error = "Last name cannot be empty"
                return false
            }
        }
        return true
    }

    private fun performSignup(email: String, username: String, password: String, 
                            firstname: String, lastname: String) {
        if (!validateInputs()) return

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
            { response ->
                if (response.has("error")) {
                    Toast.makeText(this, response.getString("error"), Toast.LENGTH_LONG).show()
                    return@JsonObjectRequest
                }
                Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_LONG).show()
                finish()
            },
            { error ->
                val errorMessage = when (error) {
                    is com.android.volley.NoConnectionError -> "No internet connection"
                    is com.android.volley.TimeoutError -> "Connection timed out"
                    is com.android.volley.ServerError -> {
                        if (error.networkResponse?.statusCode == 409) {
                            "Username already exists"
                        } else {
                            "Server error occurred"
                        }
                    }
                    else -> "Registration failed: ${error.message ?: "Unknown error"}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            })

        requestQueue.add(request)
    }
} 