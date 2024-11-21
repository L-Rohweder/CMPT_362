package com.example.beacon.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.beacon.R
import com.example.beacon.models.ProfileImageModel
import com.example.beacon.utils.ImageHandler
import com.example.beacon.utils.ProfileInformation

class ProfileActivity : AppCompatActivity() {
    private val profileInformation = ProfileInformation
    private lateinit var photoButton: Button
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var emailInput: EditText
    private lateinit var nameInput: EditText
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>
    private lateinit var profileImageViewModel: ProfileImageModel
    private lateinit var pfpImageView: ImageView
    private lateinit var imageHandler: ImageHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileImageViewModel = ViewModelProvider(this)[ProfileImageModel::class.java]
        setContentView(R.layout.activity_profile)

        // Initialize views
        initializeViews()
        // Load user data
        loadUserData()
        // Setup image handlers
        setupImageHandlers()
        // Setup button listeners
        setupButtonListeners()
    }

    private fun initializeViews() {
        photoButton = findViewById(R.id.changePFPButton)
        saveButton = findViewById(R.id.profileSaveButton)
        cancelButton = findViewById(R.id.profileCancelButton)
        emailInput = findViewById(R.id.profileEmail)
        nameInput = findViewById(R.id.profileName)
        pfpImageView = findViewById(R.id.profileImageView)
        pfpImageView.setImageResource(R.drawable.pfp)
        imageHandler = ImageHandler(this)
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("AUTH", MODE_PRIVATE)
        emailInput.setText(prefs.getString("EMAIL", ""))
        nameInput.setText("${prefs.getString("FIRSTNAME", "")} ${prefs.getString("LASTNAME", "")}")
    }

    private fun setupImageHandlers() {
        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap: Bitmap = imageHandler.getBitmap(this, imageHandler.getTempImgUri())
                profileImageViewModel.profileImage.value = bitmap
                pfpImageView.setImageBitmap(bitmap)
            }
        }

        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageUri: Uri? = result.data?.data
                imageUri?.let {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                    profileImageViewModel.profileImage.value = bitmap
                    pfpImageView.setImageBitmap(bitmap)
                }
            }
        }

        if (imageHandler.imageExists()) {
            val bitmap: Bitmap = imageHandler.getBitmap(this, imageHandler.getTempImgUri())
            pfpImageView.setImageBitmap(bitmap)
        }
    }

    private fun setupButtonListeners() {
        photoButton.setOnClickListener {
            imageHandler.apply {
                checkCameraPerms(this@ProfileActivity)
                checkGalleryPerms(this@ProfileActivity)
                showDialog(this@ProfileActivity, cameraResult, galleryResult)
            }
        }

        saveButton.setOnClickListener {
            // TODO: Implement save functionality
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
            finish()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }
}