package com.example.beacon.utils

import kotlin.math.cos

object Conversion {
    fun kmToLat(km: Double): Double {
        return km/110.574
    }

    fun kmToLong(latitude: Double, km: Double): Double {
        return km/(111.320* cos(latitude))
    }
}