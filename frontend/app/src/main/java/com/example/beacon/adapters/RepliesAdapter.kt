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
import android.widget.Toast
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.beacon.R
import com.example.beacon.activities.RepliesActivity
import com.example.beacon.models.BeaconReply
import com.example.beacon.utils.Constants.BACKEND_IP
import com.example.beacon.utils.Constants.EXTRA_POST
import com.example.beacon.utils.Constants.EXTRA_REPLY_LIST
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class RepliesAdapter(context: Context, private var replies: List<BeaconReply>):
    ArrayAdapter<BeaconReply>(context, R.layout.posts_adapter_view, replies) {
    override fun getItem(position: Int): BeaconReply {
        return replies[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return replies.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listWidgetView = convertView ?: LayoutInflater.from(context).inflate(R.layout.posts_adapter_view, parent, false)

        val reply = replies[position]

        val usernameTextView = listWidgetView.findViewById<TextView>(R.id.username)
        if (!reply.isAnon) {
            usernameTextView.text = reply.name
        }
        else {
            usernameTextView.text = context.getString(R.string.anonymous)
        }

        val contentTextView = listWidgetView.findViewById<TextView>(R.id.content)
        contentTextView.text = reply.content

        // Display formatted datetime
        val datetimeTextView = listWidgetView.findViewById<TextView>(R.id.datetime)
        datetimeTextView.text = reply.datetime

        return listWidgetView
    }

    fun updateReplies(newPosts: List<BeaconReply>) {
        this.replies = newPosts
        notifyDataSetChanged()
    }
}