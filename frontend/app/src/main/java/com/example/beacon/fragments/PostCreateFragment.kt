package com.example.beacon.fragments

import android.app.Activity
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
import com.android.volley.Request
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

class PostCreateFragment : Fragment() {

    private var _binding: FragmentPostCreateBinding? = null
    private lateinit var imageHandler: ImageHandler
    private lateinit var  image: ImageView
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>
    private lateinit var firebaseStorage: FirebaseStorage
    private var containsImage = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        firebaseStorage = Firebase.storage
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
                containsImage = true

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
        Log.d("publishPost", "Called")
        // Validate inputs
        if (username.isBlank() || content.isBlank()) {
                Toast.makeText(requireContext(), "Please enter both username and content.", Toast.LENGTH_SHORT).show()
                return
        }


        val userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]

        userViewModel.requestedLocation.value = true

        userViewModel.location.observe(viewLifecycleOwner) { location ->
            if (userViewModel.requestedLocation.value == true) {
                if(containsImage){
                    createFirebaseImage { imageLink->
                        if(imageLink != null){
                            Log.d("publishPost", "Failed to get download URL")
                            val post = BeaconPost(username, content, location.latitude, location.longitude, imageLink)
                            postToServer(post)
                        }
                        else{
                            Toast.makeText(requireContext(), "Image upload failed. Try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else{
                    Log.d("publishPost", "no image")
                    val post = BeaconPost(username, content, location.latitude, location.longitude, "")
                    postToServer(post)
                }
                userViewModel.requestedLocation.value = false
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
        val requestQueue = Volley.newRequestQueue(requireActivity())
        val url = "$BACKEND_IP/post"
        val body = JSONObject(Json.encodeToString(BeaconPost.serializer(), post))

        val request = JsonObjectRequest(
            Request.Method.POST, url, body,
            { _ ->
                // Success
                Toast.makeText(requireContext(), "Post published successfully!", Toast.LENGTH_SHORT).show()
                // Clear input fields
                binding.usernameEditText.text.clear()
                binding.contentEditText.text.clear()
            },
            { error ->
                // Failure
                Toast.makeText(requireContext(), "Failed to publish post.", Toast.LENGTH_SHORT).show()
                Log.e("Error", error.toString())
            })
        requestQueue.add(request)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}