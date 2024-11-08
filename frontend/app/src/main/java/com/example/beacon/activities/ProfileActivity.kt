package com.example.beacon.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.beacon.R
import com.example.beacon.models.ProfileImageModel
import com.example.beacon.utils.ImageHandler
import com.example.beacon.utils.ProfileInformation
import java.io.File

class ProfileActivity : AppCompatActivity(){
    private val profileInformation = ProfileInformation
    private lateinit var photoButton: Button
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var email: TextView
    private lateinit var name: TextView
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var profileImageViewModel: ProfileImageModel
    private lateinit var pfpImageView: ImageView
    private lateinit var imageHandler: ImageHandler



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileImageViewModel = ViewModelProvider(this).get(ProfileImageModel::class.java)
        setContentView(R.layout.activity_profile)
        photoButton = findViewById(R.id.changePFPButton)
        saveButton = findViewById(R.id.profileSaveButton)
        cancelButton = findViewById(R.id.profileCancelButton)
        email = findViewById(R.id.profileEmail)
        name = findViewById(R.id.profileName)
        pfpImageView = findViewById(R.id.profileImageView)
        pfpImageView.setImageResource(R.drawable.pfp)
        imageHandler = ImageHandler(this)




        photoButton.setOnClickListener(){
            imageHandler.checkCameraPerms(this)
            imageHandler.showDialog(this,cameraResult)
        }
        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK){
                val bitmap: Bitmap = imageHandler.getBitmap(this, imageHandler.getTempImgUri())
                profileImageViewModel.profileImage.value = bitmap
            }

        }


        if(imageHandler.imageExists()){
            val bitmap: Bitmap = imageHandler.getBitmap(this,imageHandler.getTempImgUri())
            pfpImageView.setImageBitmap(bitmap)
        }
        saveButton.setOnClickListener(){
            finish()
        }
        cancelButton.setOnClickListener(){
            finish()
        }
    }



}