package com.example.beacon.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.beacon.R
import com.example.beacon.adapters.PostsAdapter
import com.example.beacon.adapters.RepliesAdapter
import com.example.beacon.models.BeaconPost
import com.example.beacon.models.BeaconReply
import com.example.beacon.utils.Constants.BACKEND_IP
import com.example.beacon.utils.Constants.EXTRA_POST
import com.example.beacon.utils.Constants.EXTRA_REPLY_LIST
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class RepliesActivity : AppCompatActivity() {
    private lateinit var post: BeaconPost
    private lateinit var contentEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_replies)

        val postData = intent.getStringExtra(EXTRA_POST)
        if (postData == null) {
            Toast.makeText(
                this, "Failed to open post for replies.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val replyListData = intent.getStringExtra(EXTRA_REPLY_LIST)
        if (replyListData == null) {
            Toast.makeText(
                this, "Failed to open replies", Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }

        val replies = Json.decodeFromString(
            ListSerializer(BeaconReply.serializer()),
            replyListData
        )
        val repliesAdapter = RepliesAdapter(this, replies)
        val repliesListView = findViewById<ListView>(R.id.repliesListView)
        repliesListView.adapter = repliesAdapter

        post = Json.decodeFromString(BeaconPost.serializer(), postData)
        contentEditText = findViewById(R.id.contentEditText)
        val postButton = findViewById<Button>(R.id.postButton)
        postButton.setOnClickListener {
            postReplyToServer()
        }

        val usernameTextView = findViewById<TextView>(R.id.username)
        if (!post.isAnon) {
            usernameTextView.text = post.name
        }
        val contentTextView = findViewById<TextView>(R.id.content)
        contentTextView.text = post.content
        val positionTextView = findViewById<TextView>(R.id.position)
        positionTextView.text = post.getFormattedPosition()
        val datetimeTextView = findViewById<TextView>(R.id.datetime)
        datetimeTextView.text = post.datetime
    }

    private fun postReplyToServer() {
        val requestQueue = Volley.newRequestQueue(this)
        val url = "$BACKEND_IP/reply"

        // Get user info from SharedPreferences
        val prefs = getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val userId = prefs.getInt("USER_ID", -1)
        val username = prefs.getString("USERNAME", null)
        val isAnon = findViewById<SwitchCompat>(R.id.anonSwitch).isChecked

        // Validate inputs and user auth
        if (contentEditText.text.isBlank()) {
            Toast.makeText(this, "Please enter content for your reply.", Toast.LENGTH_SHORT).show()
            return
        }

        if (userId == -1 || username == null) {
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        val reply = BeaconReply(
            name = username,
            content = contentEditText.text.toString(),
            postId = post.id,
            userId = userId,
            isAnon = isAnon
        )

        val json = Json { encodeDefaults = true}
        val body = JSONObject(json.encodeToString(BeaconReply.serializer(), reply))

        val request = JsonObjectRequest(
            Request.Method.POST, url, body,
            { _ ->
                // Success
                Toast.makeText(this, "Reply published successfully!", Toast.LENGTH_SHORT).show()
                finish()
            },
            { error ->
                // Failure
                Toast.makeText(this, "Failed to publish reply.", Toast.LENGTH_SHORT).show()
                Log.e("Error", error.toString())
            })
        requestQueue.add(request)
    }
}