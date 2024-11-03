package com.example.beacon.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.beacon.R
import com.example.beacon.databinding.FragmentPostCreateBinding
import com.example.beacon.models.BeaconPost
import com.example.beacon.view_models.UserViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONObject

class PostCreateFragment : Fragment() {

    private var _binding: FragmentPostCreateBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostCreateBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val publishPostButton = root.findViewById<Button>(R.id.publishPostButton)
        publishPostButton.setOnClickListener {
            publishPost()
        }

        return root
    }

    private fun publishPost() {
        val username = _binding?.root?.findViewById<EditText>(R.id.usernameEditText)?.text
        val content = _binding?.root?.findViewById<EditText>(R.id.contentEditText)?.text
        val userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        userViewModel.requestedLocation.value = true
        userViewModel.location.observe(requireActivity()) {
            if (userViewModel.requestedLocation.value == true) {
                val post = BeaconPost(username.toString(), content.toString(), it.latitude, it.longitude)
                postToServer(post)
                userViewModel.requestedLocation.value = false
            }
        }
    }

    private fun postToServer(post: BeaconPost) {
        val requestQueue = Volley.newRequestQueue(requireActivity())
        val url = "http://10.0.0.193:3333/post"
        val body = JSONObject(Json.encodeToString(BeaconPost.serializer(), post))
        val request = JsonObjectRequest(
            Request.Method.POST, url, body,
            { response ->
//                binding.root.findViewById<TextView>(R.id.jsonTextView).text = response
            },
            { error ->
                Toast.makeText(requireActivity(), "Failed to post to server", Toast.LENGTH_SHORT).show()
                Log.d("Error", error.toString())
            })
        requestQueue.add(request)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}