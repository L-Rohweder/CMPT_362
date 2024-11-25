package com.example.beacon.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.beacon.R
import com.example.beacon.databinding.FragmentPostCreateBinding
import com.example.beacon.models.BeaconPost
import com.example.beacon.utils.Constants.BACKEND_IP
import com.example.beacon.utils.ImageHandler
import com.example.beacon.view_models.UserViewModel
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PostCreateFragment : Fragment() {

    private var _binding: FragmentPostCreateBinding? = null
    private val binding get() = _binding!!
    private lateinit var imageHandler: ImageHandler
    private lateinit var image: ImageView
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>
    private var requestQueue: RequestQueue? = null
    private lateinit var firebaseStorage: FirebaseStorage
    private var containsImage = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        firebaseStorage = Firebase.storage
        _binding = FragmentPostCreateBinding.inflate(inflater, container, false)
        val context = requireContext()
        imageHandler = ImageHandler(context)
        requestQueue = Volley.newRequestQueue(context)

        val root: View = binding.root
        setupViews(root)
        setupImageHandlers()
        return root
    }

    private fun setupViews(root: View) {
        val addImageButton = root.findViewById<Button>(R.id.postAddImage)
        val publishPostButton = root.findViewById<Button>(R.id.publishPostButton)
        image = root.findViewById(R.id.postImageView)

        publishPostButton.setOnClickListener {
            publishPost()
        }

        addImageButton.setOnClickListener {
            activity?.let { activity ->
                imageHandler.apply {
                    checkCameraPerms(activity)
                    checkGalleryPerms(activity)
                    showDialog(activity, cameraResult, galleryResult)
                }
            }
        }
    }

    private fun setupImageHandlers() {
        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                context?.let { ctx ->
                    val bitmap: Bitmap = imageHandler.getBitmap(ctx, imageHandler.getTempImgUri())
                    image.setImageBitmap(bitmap)
                    containsImage = true
                    image.visibility = View.VISIBLE
                }
            }
        }

        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageUri: Uri? = result.data?.data
                imageUri?.let {
                    context?.let { ctx ->
                        val bitmap = MediaStore.Images.Media.getBitmap(ctx.contentResolver, it)
                        image.setImageBitmap(bitmap)
                        containsImage = true
                        image.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun publishPost() {
        val context = context ?: return
        val content = binding.contentEditText.text.toString()

        // Get user info from SharedPreferences
        val prefs = context.getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val userId = prefs.getInt("USER_ID", -1)
        val username = prefs.getString("USERNAME", null)

        // Validate inputs and user auth
        if (content.isBlank()) {
            Toast.makeText(context, "Please enter content for your post.", Toast.LENGTH_SHORT).show()
            return
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        if (userId == -1 || username == null) {
            Toast.makeText(context, "Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        val userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        userViewModel.requestedLocation.value = true

        userViewModel.location.observe(viewLifecycleOwner) { location ->
            if (userViewModel.requestedLocation.value == true) {
                if(containsImage){
                    createFirebaseImage { imageLink->
                        if(imageLink != null){
                            val post = BeaconPost(
                                name = username,
                                content = content,
                                latitude = location.latitude,
                                longitude = location.longitude,
                                imageLink = imageLink,
                                userID = userId,
                                id = -1,
                                username = username,
                                datetime = dateFormat.format(Date())
                            )
                            postToServer(post)
                            userViewModel.requestedLocation.value = false
                        }
                        else{Toast.makeText(requireContext(), "Image upload failed. Try again.", Toast.LENGTH_SHORT).show()}

                    }
                }
                else{
                    val post = BeaconPost(
                        name = username,
                        content = content,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        imageLink = "",
                        userID = userId,
                        id = -1,
                        username = username,
                        datetime = dateFormat.format(Date())
                    )
                    postToServer(post)
                    userViewModel.requestedLocation.value = false
                }

            }
        }
    }

    private fun createFirebaseImage(onComplete: (String?) -> Unit){
        //might need to change this later. If two users create a post in the same milisecond we will run into issues
        val storageReference = firebaseStorage.reference.child("images/${System.currentTimeMillis()}.jpg")

        val drawable = image.drawable
        if(drawable is BitmapDrawable){
            val bitmap = drawable.bitmap
            val byteArrOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrOutputStream)
            val data = byteArrOutputStream.toByteArray()

            val uploadTask = storageReference.putBytes(data)
            uploadTask.addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    onComplete(uri.toString())
                    Log.d("PostCreateFragment", "Image uploaded successfully: ${uri.toString()}")
                }.addOnFailureListener { onComplete(null)
                    Log.e("PostCreateFragment", "Failed to get download URL")}
            }.addOnFailureListener { onComplete(null)
                Log.e("PostCreateFragment", "Failed to get download URL")}
        }
        else{onComplete(null)}
    }

    private fun postToServer(post: BeaconPost) {
        val context = context ?: return
        if (!isAdded) return

        val url = "$BACKEND_IP/post"
        val body = JSONObject().apply {
            put("name", post.name)
            put("content", post.content)
            put("latitude", post.latitude)
            put("longitude", post.longitude)
            put("imageLink", post.imageLink)
            put("userID", post.userID)
            put("id", post.id)
            put("username", post.username)
            put("datetime", post.datetime)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, body,
            { response ->
                if (isAdded) {
                    if (response.has("error")) {
                        Toast.makeText(context, response.getString("error"), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Post published successfully!", Toast.LENGTH_SHORT).show()
                        binding.contentEditText.text.clear()
                    }
                }
            },
            { error ->
                if (isAdded) {
                    val errorMessage = when {
                        error.networkResponse?.data != null -> {
                            try {
                                val errorJson = JSONObject(String(error.networkResponse.data))
                                errorJson.getString("error")
                            } catch (e: Exception) {
                                "Failed to publish post: Network error"
                            }
                        }
                        error is NoConnectionError -> "No internet connection"
                        error is TimeoutError -> "Connection timed out"
                        else -> "Failed to publish post: Please try again"
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    Log.e("PostError", "Error: ${error.message}", error)
                }
            })

        request.tag = this
        requestQueue?.add(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requestQueue?.cancelAll(this)
        _binding = null
        requestQueue = null
    }
}