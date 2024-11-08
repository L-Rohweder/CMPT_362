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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        photoButton = findViewById(R.id.changePFPButton)
        saveButton = findViewById(R.id.profileSaveButton)
        cancelButton = findViewById(R.id.profileCancelButton)
        email = findViewById(R.id.profileEmail)
        name = findViewById(R.id.profileName)
        pfpImageView = findViewById(R.id.profileImageView)



        val tempImgFile = File(getExternalFilesDir(null), "ImageFile.jpg")
        val tempImgUri  = FileProvider.getUriForFile(this,"com.example.beacon.activities", tempImgFile)

        photoButton.setOnClickListener(){
            checkCameraPerms()
            val alertDialog = AlertDialog.Builder(this)
            val options = arrayOf("Open Camera", "Select From Gallery")
            alertDialog.setTitle("Pick Profile Picture")
            alertDialog.setItems(options) {
                    dialog, pictureChoice ->
                if(pictureChoice == 0){
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri)
                    cameraResult.launch(intent)
                }

            }



            alertDialog.show()



        }
        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK){
                val bitmap = getBitmap(this, tempImgUri)
                profileImageViewModel.profileImage.value = bitmap
            }

        }
        profileImageViewModel = ViewModelProvider(this).get(ProfileImageModel::class.java)
        profileImageViewModel.profileImage.observe(this,{it->
            pfpImageView.setImageBitmap(it)
        })

        if(tempImgFile.exists()){
            val bitmap = getBitmap(this,tempImgUri)
            pfpImageView.setImageBitmap(bitmap)
        }
    }

    fun checkCameraPerms(){
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }

    }

    //Creates a bitmap for our image
    fun getBitmap(context: Context, imgUri: Uri): Bitmap {
        val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imgUri))
        val matrix = Matrix()
        //ensure the image is rotated properly
        //matrix.setRotate(90f)
        return Bitmap.createBitmap(bitmap,0,0, bitmap.width, bitmap.height, matrix, true)

    }

}