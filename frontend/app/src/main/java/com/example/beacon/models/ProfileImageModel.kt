package com.example.beacon.models

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileImageModel: ViewModel() {
    val profileImage = MutableLiveData<Bitmap>()

}