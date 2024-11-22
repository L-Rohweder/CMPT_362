package com.example.beacon.view_models

import android.content.ComponentName
import android.content.ServiceConnection
import android.location.Location
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class UserViewModel: ViewModel() {
    val location = MutableLiveData<LatLng>()
    val requestedLocation = MutableLiveData(false)
    val range = MutableLiveData(5.0f)
}