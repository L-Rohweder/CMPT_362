package com.example.beacon.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.beacon.R
import com.example.beacon.models.ProfileImageModel
import com.example.beacon.utils.ImageHandler
import com.example.beacon.utils.ProfileInformation

class ProfileActivity : AppCompatActivity(){
    private val profileInformation = ProfileInformation
    private lateinit var photoButton: Button
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var email: TextView
    private lateinit var name: TextView
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>
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
            imageHandler.checkGalleryPerms(this)
            imageHandler.showDialog(this,cameraResult, galleryResult)
        }
        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK){
                val bitmap: Bitmap = imageHandler.getBitmap(this, imageHandler.getTempImgUri())
                profileImageViewModel.profileImage.value = bitmap
            }

        }

        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK && result.data != null){
                val imageUri: Uri? = result.data?.data
                imageUri?.let {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                    profileImageViewModel.profileImage.value = bitmap
                    pfpImageView.setImageBitmap(bitmap)

                }
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