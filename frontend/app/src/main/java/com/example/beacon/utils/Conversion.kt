package com.example.beacon.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos

object Conversion {
    fun kmToLat(km: Double): Double {
        return km/110.574
    }

    fun kmToLong(latitude: Double, km: Double): Double {
        return km/(111.320* cos(latitude))
    }

    fun formatDateTime(datetime: String): String {
        return try {
            // Assume the datetime from the server is in UTC
            val originalFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            originalFormat.timeZone = TimeZone.getTimeZone("UTC")

            //this is for recent posts
            val postDate = originalFormat.parse(datetime)
            val currentTime = Date()

            val milisSincePost = currentTime.time-postDate.time
            val hoursSincePost = milisSincePost/(3600000)
            if(hoursSincePost <24){
                return "$hoursSincePost hours ago"
            }

            val targetFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            targetFormat.timeZone = TimeZone.getDefault() // Device's local timezone

            val date = originalFormat.parse(datetime)
            targetFormat.format(date)
        } catch (e: Exception) {
            Log.e("Conversion", "Error parsing datetime: $datetime", e)
            datetime // Return the original string if parsing fails
        }
    }
}