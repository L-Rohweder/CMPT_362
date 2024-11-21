package com.example.beacon.models

import kotlinx.serialization.Serializable

@Serializable
class BeaconReply(
    val name: String,
    val content: String,
    val likes: Int = 0,
    val dislikes: Int = 0,
    val postId: Long = -1,
    val userId: Long = -1,
    val datetime: String = ""
) {
}