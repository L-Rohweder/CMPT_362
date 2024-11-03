package com.example.beacon.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.beacon.R
import com.example.beacon.models.BeaconPost

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
        val listWidgetView = convertView ?: View.inflate(context, R.layout.posts_adapter_view, null)

        val post = posts[position]

        val usernameTextView = listWidgetView.findViewById<TextView>(R.id.username)
        usernameTextView.text = post.name

        val contentTextView = listWidgetView.findViewById<TextView>(R.id.content)
        contentTextView.text = post.content

        val positionTextView = listWidgetView.findViewById<TextView>(R.id.position)
        positionTextView.text = post.getFormattedPosition()

        return listWidgetView
    }
}