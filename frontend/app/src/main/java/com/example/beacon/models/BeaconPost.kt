package com.example.beacon.models

import kotlinx.serialization.Serializable

@Serializable
data class BeaconPost(val name: String, val content: String, val latitude: Double, val longitude: Double, val datetime: String = "") {
    fun getFormattedPosition(): String {
        return "$latitude $latitude"
    }
}