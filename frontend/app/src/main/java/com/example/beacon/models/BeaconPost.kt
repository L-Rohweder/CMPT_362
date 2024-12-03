package com.example.beacon.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class BeaconPost(
    val name: String,
    val content: String,
    val latitude: Double,
    val longitude: Double,
    val imageLink: String = "",
    val userID: Int,
    val id: Int = -1,
    val username: String,
    val datetime: String = "",
    val isAnon: Boolean = false,
    var likedUserIds: String = "[]",
    var dislikedUserIds: String = "[]"
) {
    fun getFormattedPosition(): String {
        return "$latitude $longitude"
    }

    fun getDislikedUserIds(): IntArray {
        return Json.decodeFromString<IntArray>(dislikedUserIds)
    }

    fun getLikedUserIds(): IntArray {
        return Json.decodeFromString<IntArray>(likedUserIds)
    }
}
