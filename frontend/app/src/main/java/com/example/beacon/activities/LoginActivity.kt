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

    private fun performLogin(username: String, password: String) {
        val requestQueue = Volley.newRequestQueue(this)
        val url = "$BACKEND_IP/getUser"

        val body = JSONObject().apply {
            put("username", username)
            put("password", password)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, body,
            { response ->
                // Save auth details
                getSharedPreferences("AUTH", MODE_PRIVATE).edit().apply {
                    putInt("USER_ID", response.getInt("id"))
                    putString("USERNAME", response.getString("username"))
                    putString("EMAIL", response.getString("email"))
                    putString("FIRSTNAME", response.getString("firstname"))
                    putString("LASTNAME", response.getString("lastname"))
                    apply()
                }
                
                startMainActivity()
                finish()
            },
            { error ->
                Toast.makeText(
                    this,
                    "Login failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            })

        requestQueue.add(request)
    }

    private fun startMainActivity() {
        startActivity(Intent(this, TabbedActivity::class.java))
    }
} 