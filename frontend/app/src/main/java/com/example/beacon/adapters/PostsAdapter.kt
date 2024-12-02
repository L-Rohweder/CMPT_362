package com.example.beacon.adapters

import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
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
import com.google.android.gms.maps.model.LatLng
import java.util.Date

class PostsAdapter(
    context: Context,
    private var posts: List<BeaconPost>,
    private var progressBar: ProgressBar,
    var userLocation: LatLng?
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
        Log.d("PostsAdapter", "Number of posts: ${posts.size}")
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
        val distance = userLocation?.let {
            calculateDistanceToPost(it.latitude, it.longitude, post.latitude, post.longitude)
        } ?: 0.0 // Default to 0.0 if location is null
        positionTextView.text = "${"%.2f".format(distance)} km away"


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

        val likeButton = listWidgetView.findViewById<ImageButton>(R.id.like)
        var likes = 0
        var isLiked = false
        val dislikeButton = listWidgetView.findViewById<ImageButton>(R.id.dislike)
        var isDisliked = false
        val likeText = listWidgetView.findViewById<TextView>(R.id.likeText)

        val prefs = context.getSharedPreferences("AUTH", MODE_PRIVATE)
        val userId = prefs.getInt("USER_ID", -1)
        getLikesFromServer(post.id) { userIds ->
            if (userIds != null) {
                if(userId in userIds) {
                    isLiked = true
                    likeButton.setImageResource(R.drawable.clickedthumbsup)
                }
                likes = userIds.size
            } else {
                Log.e("Likes", "Failed to retrieve likes")
            }
        }

        likeText.text = "Likes: $likes"

        likeButton.setOnClickListener {
            if(!isLiked) {
                if(isDisliked){
                    isDisliked=false
                    dislikeButton.setImageResource(R.drawable.thumbsdown)
                }
                likes += 1
                isLiked=true
                sendLikeToServer(post.id,userId)
                likeButton.setImageResource(R.drawable.clickedthumbsup)
            }
            else{
                likes -= 1
                isLiked=false
                likeButton.setImageResource(R.drawable.thumbsup)
            }
            likeText.text = "Likes: $likes"

        }

        dislikeButton.setOnClickListener {
            if(!isDisliked) {
                if(isLiked){
                    isLiked = false
                    likeButton.setImageResource(R.drawable.thumbsup)
                }
                isDisliked=true
                dislikeButton.setImageResource(R.drawable.clickedthumbsdown)
            }
            else{
                isDisliked=false
                dislikeButton.setImageResource(R.drawable.thumbsdown)
            }

        }

        listWidgetView.setOnClickListener {
            getRepliesFromServer(post)
        }

        return listWidgetView
    }

    fun getLikesFromServer(postId: Int,onResult: (List<Int>?) -> Unit){
        val url = "$BACKEND_IP/getLikes"
        val params = JSONObject().apply {
            put("postID", postId)
        }

        val request = object : StringRequest(
            Method.POST, url,
            {
                response->
                try{
                    Log.d("Response", "Server response: $response")
                    if(response ==null){
                        Log.d("Response", "Likes returned null")
                    }
                    else {
                        val jsonResponse = JSONObject(response)
                        val likesArray = jsonResponse.getJSONArray("likes")
                        val userIds = mutableListOf<Int>()
                        for (i in 0 until likesArray.length()) {
                            val userObject = likesArray.getJSONObject(i)
                            val userId = userObject.getInt("userID")
                            userIds.add(userId)
                        }
                        onResult(userIds)
                    }

                }catch (e: Exception) {
                    Log.e("Error", "Getting likes failed", e)
                    onResult(null)
                }
            },{
                error->Log.e("Error", error.toString())
                onResult(null)
            }
        ){
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
            override fun getBody(): ByteArray = params.toString().toByteArray(Charsets.UTF_8)
        }
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(request)
    }

    fun sendLikeToServer(postId:Int,userId:Int){
        val url = "$BACKEND_IP/like"
        val params = JSONObject().apply {
            put("userID", userId)
            put("postID", postId)
        }
        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    // Handle the server response
                    val jsonResponse = JSONObject(response)
                    val success = jsonResponse.getBoolean("success")
                    if (success) {
                        Log.d("Like", "Like sent successfully")
                    } else {
                        Log.e("Like", "Failed to like post")
                    }
                } catch (e: Exception) {
                    Log.e("Error", "Parsing response failed", e)
                }
            },
            { error ->
                // Handle error responses
                Log.e("Error", "Failed to send like to server: ${error.message}")
            }
        ){
            override fun getBody(): ByteArray = params.toString().toByteArray(Charsets.UTF_8)

            override fun getBodyContentType(): String = "application/json; charset=utf-8"
        }
        Volley.newRequestQueue(context).add(request)
    }

    fun updatePosts(newPosts: List<BeaconPost>) {
        Log.d("PostsAdapter", "Updating posts. New size: ${newPosts.size}")
        this.posts = newPosts
        notifyDataSetChanged()
    }

    private fun calculateDistanceToPost(lat1: Double,lon1: Double,lat2: Double,lon2: Double):Double{
        val location1 = Location("").apply{
            latitude = lat1
            longitude = lon1
        }
        val location2 = Location("").apply{
            latitude = lat2
            longitude = lon2
        }

        val distance = location1.distanceTo(location2)
        //convert distance to km
        return distance/1000.0

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