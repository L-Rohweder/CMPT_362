package com.example.beacon.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class UserViewModel: ViewModel() {
    val location = MutableLiveData<LatLng>()
    val requestedLocation = MutableLiveData(false)
    val range = MutableLiveData(5.0f)
}