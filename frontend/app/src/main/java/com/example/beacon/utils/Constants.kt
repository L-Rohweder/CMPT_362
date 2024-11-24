package com.example.beacon.utils

object Constants {
    // For emulator using localhost: "http://10.0.2.2:3333"
    // For physical device on same network: "http://YOUR_COMPUTER_IP:3333"
    const val BACKEND_IP = "http://10.0.0.2:3333"
    const val RADIUS_MAX = 2000.0

    const val SP_KEY = "SHARED_PREFS"
    const val SP_RANGE_KM = "SP_RANGE_KM"
    const val SP_UNITS = "SP_UNITS"

    const val EXTRA_POST = "POST"
    const val EXTRA_REPLY_LIST = "REPLY_LIST"
    const val EXTRA_LOCATION = "LOCATION"
}