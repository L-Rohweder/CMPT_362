package com.example.beacon.models

import kotlinx.serialization.Serializable

@Serializable
class BeaconReply(
    val name: String,
    val content: String,
    val postId: Int,
    val id: Int = -1,
    val likes: Int = 0,
    val dislikes: Int = 0,
    val userId: Int = -1,
    val datetime: String = "",
    val isAnon: Boolean = false
) {
}