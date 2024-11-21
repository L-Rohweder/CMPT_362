package com.example.beacon.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import com.example.beacon.R
import com.example.beacon.activities.RepliesActivity
import com.example.beacon.models.BeaconPost
import com.example.beacon.utils.Constants.EXTRA_POST
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PostsAdapter(context: Context, private var posts: List<BeaconPost>):
    ArrayAdapter<BeaconPost>(context, R.layout.posts_adapter_view, posts) {
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
        usernameTextView.text = post.name

        val contentTextView = listWidgetView.findViewById<TextView>(R.id.content)
        contentTextView.text = post.content

        val positionTextView = listWidgetView.findViewById<TextView>(R.id.position)
        positionTextView.text = post.getFormattedPosition()

        // Display formatted datetime
        val datetimeTextView = listWidgetView.findViewById<TextView>(R.id.datetime)
        datetimeTextView.text = formatDateTime(post.datetime)

        val layout = listWidgetView.findViewById<LinearLayout>(R.id.layout)
        layout.setOnClickListener {
            val intent = Intent(context, RepliesActivity::class.java)
            intent.putExtra(EXTRA_POST, Json.encodeToString(BeaconPost.serializer(), post))
            context.startActivity(intent)
        }

        return listWidgetView
    }

    fun updatePosts(newPosts: List<BeaconPost>) {
        this.posts = newPosts
        notifyDataSetChanged()
    }

    private fun formatDateTime(datetime: String): String {
        return try {
            // Assume the datetime from the server is in UTC
            val originalFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            originalFormat.timeZone = TimeZone.getTimeZone("UTC")

            val targetFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            targetFormat.timeZone = TimeZone.getDefault() // Device's local timezone

            val date = originalFormat.parse(datetime)
            targetFormat.format(date)
        } catch (e: Exception) {
            Log.e("PostsAdapter", "Error parsing datetime: $datetime", e)
            datetime // Return the original string if parsing fails
        }
    }
}