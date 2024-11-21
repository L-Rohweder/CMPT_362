package com.example.beacon.activities

import android.content.Intent
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

class LoginActivity : AppCompatActivity() {
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Check if already logged in
        val prefs = getSharedPreferences("AUTH", MODE_PRIVATE)
        if (prefs.contains("USER_ID")) {
            startMainActivity()
            finish()
            return
        }

        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        loginButton = findViewById(R.id.loginButton)
        signupButton = findViewById(R.id.signupButton)

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(username, password)
        }

        signupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun validateInputs(): Boolean {
        val username = usernameInput.text.toString()
        val password = passwordInput.text.toString()

        when {
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
        }
        return true
    }

    private fun performLogin(username: String, password: String) {
        if (!validateInputs()) return

        val requestQueue = Volley.newRequestQueue(this)
        val url = "$BACKEND_IP/getUser"

        val body = JSONObject().apply {
            put("username", username)
            put("password", password)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, body,
            { response ->
                if (response.has("error")) {
                    Toast.makeText(this, response.getString("error"), Toast.LENGTH_LONG).show()
                    return@JsonObjectRequest
                }
                
                // Save auth details
                getSharedPreferences("AUTH", MODE_PRIVATE).edit().apply {
                    putInt("USER_ID", response.getInt("id"))
                    putString("USERNAME", response.getString("username"))
                    putString("EMAIL", response.getString("email"))
                    putString("FIRSTNAME", response.getString("firstname"))
                    putString("LASTNAME", response.getString("lastname"))
                    apply()
                }
                
                startActivity(Intent(this, TabbedActivity::class.java))
                finish()
            },
            { error ->
                val errorMessage = when {
                    error.networkResponse?.data != null -> {
                        try {
                            val errorJson = JSONObject(String(error.networkResponse.data))
                            errorJson.getString("error")
                        } catch (e: Exception) {
                            "Invalid username or password"
                        }
                    }
                    error is com.android.volley.NoConnectionError -> "No internet connection"
                    error is com.android.volley.TimeoutError -> "Connection timed out"
                    else -> "Invalid username or password"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            })

        requestQueue.add(request)
    }

    private fun startMainActivity() {
        startActivity(Intent(this, TabbedActivity::class.java))
    }
} 