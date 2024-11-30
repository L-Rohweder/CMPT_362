package com.example.beacon.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.beacon.R
import com.example.beacon.activities.RepliesActivity
import com.example.beacon.models.BeaconPost
import com.example.beacon.models.BeaconReply
import com.example.beacon.utils.Constants.BACKEND_IP
import com.example.beacon.utils.Constants.EXTRA_POST
import com.example.beacon.utils.Constants.EXTRA_REPLY_LIST
import com.example.beacon.view_models.UserViewModel
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import com.bumptech.glide.Glide
import com.example.beacon.utils.Conversion
import java.util.Date

class PostsAdapter(
    context: Context,
    private var posts: List<BeaconPost>,
    private var progressBar: ProgressBar
): ArrayAdapter<BeaconPost>(context, R.layout.posts_adapter_view, posts) {

    override fun getItem(position: Int): BeaconPost {
        return posts[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return posts.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listWidgetView = convertView ?: LayoutInflater.from(context).inflate(R.layout.posts_adapter_view, parent, false)

        val post = posts[position]

        val usernameTextView = listWidgetView.findViewById<TextView>(R.id.username)
        if (!post.isAnon) {
            usernameTextView.text = post.name
        }
        else {
            usernameTextView.text = context.getString(R.string.anonymous)
        }

        val contentTextView = listWidgetView.findViewById<TextView>(R.id.content)
        contentTextView.text = post.content

        val positionTextView = listWidgetView.findViewById<TextView>(R.id.position)
        positionTextView.text = post.getFormattedPosition()

        // Display formatted datetime
        val datetimeTextView = listWidgetView.findViewById<TextView>(R.id.datetime)
        datetimeTextView.text = Conversion.formatDateTime(post.datetime)

        val postImageView = listWidgetView.findViewById<ImageView>(R.id.postImage)

        if(!post.imageLink.isNullOrEmpty()){
            postImageView.visibility = View.VISIBLE
            Glide.with(context).load(post.imageLink).into(postImageView)
        }
        else{
            postImageView.visibility = View.GONE
        }

        val likeButton = listWidgetView.findViewById<Button>(R.id.like)
        var likes = 0
        var isLiked = false
        val dislikeButton = listWidgetView.findViewById<Button>(R.id.dislike)
        var dislikes = 0
        var isDisliked = false

        likeButton.setOnClickListener {
            if(!isLiked) {
                if(isDisliked){
                    dislikes -= 1
                    isDisliked=false
                    dislikeButton.text = "Dislike(${dislikes})"
                }
                likes += 1
                isLiked=true
            }
            else{
                likes -= 1
                isLiked=false
            }
            likeButton.text = "Like(${likes})"
        }



        dislikeButton.setOnClickListener {
            if(!isDisliked) {
                if(isLiked){
                    likes -= 1
                    isLiked=false
                    likeButton.text = "Like(${likes})"
                }
                dislikes += 1
                isDisliked=true
            }
            else{
                dislikes -= 1
                isDisliked=false
            }
            dislikeButton.text = "Dislike(${dislikes})"
        }

        listWidgetView.setOnClickListener {
            getRepliesFromServer(post)
        }

        return listWidgetView
    }

    fun updatePosts(newPosts: List<BeaconPost>) {
        this.posts = newPosts
        notifyDataSetChanged()
    }

    private fun getRepliesFromServer(post: BeaconPost) {
        if (progressBar.visibility == View.VISIBLE) {
            return
        }
        progressBar.visibility = View.VISIBLE

        val requestQueue = Volley.newRequestQueue(context)
        val url = "$BACKEND_IP/getReplies"

        // Creating the JSON body with out current latitude and longitude
        val params = JSONObject()
        params.put("postId", post.id)

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    progressBar.visibility = View.INVISIBLE

                    // Parse the response string into a list of BeaconPost objects
                    val replies = Json.decodeFromString(
                        ListSerializer(BeaconReply.serializer()),
                        response)

                    if (replies.isEmpty()) {
                        Toast.makeText(context, "No replies.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Replies retrieved successfully!", Toast.LENGTH_SHORT).show()
                    }

                    val intent = Intent(context, RepliesActivity::class.java)
                    intent.putExtra(EXTRA_POST, Json.encodeToString(BeaconPost.serializer(), post))
                    intent.putExtra(EXTRA_REPLY_LIST, response)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("Error", "Parsing replies failed", e)
                    Toast.makeText(context, "Error parsing replies.", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                progressBar.visibility = View.INVISIBLE
                Toast.makeText(context, "Failed to retrieve replies from server.", Toast.LENGTH_SHORT).show()
                Log.e("Error", error.toString())
            }
        ) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return params.toString().toByteArray(Charsets.UTF_8)
            }
        }

        requestQueue.add(request)
    }
}