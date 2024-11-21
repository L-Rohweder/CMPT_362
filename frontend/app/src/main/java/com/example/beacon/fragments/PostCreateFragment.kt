package com.example.beacon.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
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
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.beacon.R
import com.example.beacon.activities.LoginActivity
import com.example.beacon.databinding.FragmentPostCreateBinding
import com.example.beacon.models.BeaconPost
import com.example.beacon.utils.Constants.BACKEND_IP
import com.example.beacon.utils.ImageHandler
import com.example.beacon.view_models.UserViewModel
import kotlinx.serialization.json.Json
import org.json.JSONObject

class PostCreateFragment : Fragment() {

    private var _binding: FragmentPostCreateBinding? = null
    private lateinit var imageHandler: ImageHandler
    private lateinit var  image: ImageView
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPostCreateBinding.inflate(inflater, container, false)
        imageHandler = ImageHandler(requireContext())
        val root: View = binding.root
        val addImageButton = root.findViewById<Button>(R.id.postAddImage)
        val publishPostButton = root.findViewById<Button>(R.id.publishPostButton)
        image = root.findViewById<ImageView>(R.id.postImageView)

        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK){
                val bitmap: Bitmap = imageHandler.getBitmap(requireContext(), imageHandler.getTempImgUri())
                image.setImageBitmap(bitmap)
            }

        }


        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK && result.data != null){
                val imageUri: Uri? = result.data?.data
                imageUri?.let {
                    val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
                    image.setImageBitmap(bitmap)

                }
            }

        }


        publishPostButton.setOnClickListener {
            publishPost()
        }
        addImageButton.setOnClickListener(){
            imageHandler.checkCameraPerms(requireActivity())
            imageHandler.checkGalleryPerms(requireActivity())
            imageHandler.showDialog(requireActivity(), cameraResult, galleryResult)
        }

        return root
    }

    private fun publishPost() {
        val username = binding.usernameEditText.text.toString()
        val content = binding.contentEditText.text.toString()

        // Validate inputs
        if (username.isBlank() || content.isBlank()) {
            Toast.makeText(requireContext(), "Please enter both username and content.", Toast.LENGTH_SHORT).show()
            return
        }

        val userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        val prefs = requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
        val userId = prefs.getInt("USER_ID", -1)
        val storedUsername = prefs.getString("USERNAME", "")

        if (userId == -1) {
            Toast.makeText(requireContext(), "Please login again", Toast.LENGTH_SHORT).show()
            // Redirect to login
            startActivity(Intent(requireActivity(), LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            requireActivity().finish()
            return
        }

        userViewModel.requestedLocation.value = true

        userViewModel.location.observe(viewLifecycleOwner) { location ->
            if (userViewModel.requestedLocation.value == true) {
                val post = BeaconPost(
                    name = username,
                    content = content,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    imageLink = "",  // Add image handling if needed
                    userID = userId,
                    username = storedUsername ?: username
                )
                postToServer(post)
                userViewModel.requestedLocation.value = false
            }
        }
    }

    private fun postToServer(post: BeaconPost) {
        // Check if fragment is attached to context
        if (!isAdded) return

        val requestQueue = Volley.newRequestQueue(requireContext())
        val url = "$BACKEND_IP/post"
        val body = JSONObject().apply {
            put("name", post.name)
            put("content", post.content)
            put("latitude", post.latitude)
            put("longitude", post.longitude)
            put("imageLink", post.imageLink)
            put("userID", post.userID)
            put("username", post.username)
        }

        val request = JsonObjectRequest(
            Request.Method.POST, url, body,
            { response ->
                // Check if fragment is still attached
                if (isAdded) {
                    if (response.has("error")) {
                        Toast.makeText(requireContext(), response.getString("error"), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Post published successfully!", Toast.LENGTH_SHORT).show()
                        binding.usernameEditText.text.clear()
                        binding.contentEditText.text.clear()
                    }
                }
            },
            { error ->
                // Check if fragment is still attached before showing error
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
                        error is com.android.volley.NoConnectionError -> "No internet connection"
                        error is com.android.volley.TimeoutError -> "Connection timed out"
                        else -> "Failed to publish post: Please try again"
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    Log.e("PostError", "Error: ${error.message}", error)
                }
            })

        // Add tag to request for cleanup
        request.tag = this
        requestQueue.add(request)
    }

    override fun onDestroyView() {
        // Cancel any pending requests when fragment is destroyed
        Volley.newRequestQueue(requireContext()).cancelAll(this)
        super.onDestroyView()
        _binding = null
    }
}