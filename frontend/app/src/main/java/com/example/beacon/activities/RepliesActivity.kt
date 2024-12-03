package com.example.beacon.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.beacon.R
import com.example.beacon.adapters.PostsAdapter
import com.example.beacon.adapters.RepliesAdapter
import com.example.beacon.models.BeaconPost
import com.example.beacon.models.BeaconReply
import com.example.beacon.utils.Constants
import com.example.beacon.utils.Constants.BACKEND_IP
import com.example.beacon.utils.Constants.EXTRA_LOCATION
import com.example.beacon.utils.Constants.EXTRA_POST
import com.example.beacon.utils.Constants.EXTRA_REPLY_LIST
import com.example.beacon.view_models.UserViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.json.JSONObject

class RepliesActivity : AppCompatActivity() {
    private lateinit var post: BeaconPost
    private lateinit var contentEditText: EditText
    private lateinit var anonSwitch: SwitchCompat
    private lateinit var repliesAdapter: RepliesAdapter
    private lateinit var repliesLinearLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_replies)

        // Get the post JSON string using the correct key
        val postJson = intent.getStringExtra(EXTRA_POST)
        if (postJson == null) {
            Toast.makeText(this, "Failed to open post for replies.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        try {
            post = Json.decodeFromString(BeaconPost.serializer(), postJson)
            
            val replyListData = intent.getStringExtra(EXTRA_REPLY_LIST)
            val replies = if (replyListData != null) {
                Json.decodeFromString(
                    ListSerializer(BeaconReply.serializer()),
                    replyListData
                )
            } else {
                arrayListOf()
            }

            contentEditText = findViewById(R.id.contentEditText)
            val postButton = findViewById<Button>(R.id.postButton)
            postButton.setOnClickListener {
                postReplyToServer()
            }

            val postsLinearLayout = findViewById<LinearLayout>(R.id.postLinearLayout)
            val userViewModel = ViewModelProvider(this)[UserViewModel::class.java]

            // Get location from separate coordinates
            val latitude = intent.getDoubleExtra("latitude", 0.0)
            val longitude = intent.getDoubleExtra("longitude", 0.0)
            val location = if (latitude != 0.0 && longitude != 0.0) {
                LatLng(latitude, longitude)
            } else {
                null
            }

            val postsAdapter = PostsAdapter(this, listOf(post), null, location)
            postsLinearLayout.addView(postsAdapter.getView(0, null, postsLinearLayout))

            repliesLinearLayout = findViewById(R.id.repliesLinearLayout)
            repliesAdapter = RepliesAdapter(this, replies)
            for (i in 0..<repliesAdapter.count) {
                repliesLinearLayout.addView(repliesAdapter.getView(i, null, repliesLinearLayout))
            }

            val prefs = getSharedPreferences(Constants.SP_KEY, Context.MODE_PRIVATE)
            anonSwitch = findViewById(R.id.anonSwitch)
            anonSwitch.isChecked = prefs.getBoolean(Constants.SP_IS_ANON, false)
        } catch (e: Exception) {
            Log.e("Error", "Failed to parse post data", e)
            Toast.makeText(this, "Failed to open post for replies.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun postReplyToServer() {
        val requestQueue = Volley.newRequestQueue(this)
        val url = "$BACKEND_IP/reply"

        // Get user info from SharedPreferences
        val prefs = getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val userId = prefs.getInt("USER_ID", -1)
        val username = prefs.getString("USERNAME", null)
        val isAnon = anonSwitch.isChecked

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
                val pos = repliesAdapter.count
                repliesAdapter.add(reply)
                repliesLinearLayout.addView(repliesAdapter.getView(pos, null, repliesLinearLayout))
            },
            { error ->
                // Failure
                Toast.makeText(this, "Failed to publish reply.", Toast.LENGTH_SHORT).show()
                Log.e("Error", error.toString())
            })
        requestQueue.add(request)
    }
}