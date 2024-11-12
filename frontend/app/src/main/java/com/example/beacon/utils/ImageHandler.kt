package com.example.beacon.utils

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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.beacon.R
import com.example.beacon.models.ProfileImageModel
import java.io.File

class ImageHandler(private val context:Context) {


       private lateinit var tempImgFile: File
       private lateinit var tempImgUri: Uri


        init {
            tempImgFile = File(context.getExternalFilesDir(null), "PostImageFile.jpg")
            tempImgUri  = FileProvider.getUriForFile(context,"com.example.beacon.activities", tempImgFile)
        }

        fun showDialog(activity: Activity, cameraResult: ActivityResultLauncher<Intent>, galleryResult: ActivityResultLauncher<Intent>) {
            val alertDialog = AlertDialog.Builder(context)
            val options = arrayOf("Open Camera", "Select From Gallery")
            alertDialog.setTitle("Pick Profile Picture")
            alertDialog.setItems(options) {
                    dialog, pictureChoice ->
                if(pictureChoice == 0){
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri)
                    cameraResult.launch(intent)
                }
                else{
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryResult.launch(intent)
                }

            }
            alertDialog.show()
        }

        fun getTempImgUri(): Uri {
            return tempImgUri
        }


        fun checkCameraPerms(activity: Activity){
            if(ContextCompat.checkSelfPermission(context,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), 0)
            }
        }

    fun checkGalleryPerms(activity: Activity){
        if(ContextCompat.checkSelfPermission(context,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
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

    fun imageExists(): Boolean{
        return tempImgFile.exists()
    }


}