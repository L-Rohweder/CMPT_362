package com.example.beacon.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.volley.toolbox.StringRequest
import com.example.beacon.adapters.PostsAdapter
import com.android.volley.toolbox.Volley
import com.example.beacon.R
import com.example.beacon.databinding.FragmentHomeBinding
import com.example.beacon.models.BeaconPost
import com.example.beacon.utils.Constants.BACKEND_IP
import com.example.beacon.view_models.UserViewModel
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.json.JSONObject

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var postsAdapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        postsAdapter = PostsAdapter(requireActivity(), mutableListOf())
        val postListView = root.findViewById<ListView>(R.id.postsListView)
        postListView.adapter = postsAdapter

        val getPostsButton = root.findViewById<Button>(R.id.getPostsButton)
        getPostsButton.setOnClickListener {
            getPostsFromServer()
        }

        return root
    }

    private fun getPostsFromServer() {
        val requestQueue = Volley.newRequestQueue(requireActivity())
        val url = "$BACKEND_IP/get"

        // Get user's current location
        val userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        val currentLocation = userViewModel.location.value
        val range = userViewModel.range.value

        if (currentLocation == null) {
            Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
            return
        }

        // Creating the JSON body with out current latitude and longitude
        val params = JSONObject()
        params.put("latitude", currentLocation.latitude)
        params.put("longitude", currentLocation.longitude)
        params.put("range", range)

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    // Parse the response string into a list of BeaconPost objects
                    val posts = Json.decodeFromString(
                        ListSerializer(BeaconPost.serializer()),
                        response
                    )

                    if (posts.isEmpty()) {
                        Toast.makeText(requireContext(), "No posts found nearby.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Posts retrieved successfully!", Toast.LENGTH_SHORT).show()
                    }

                    postsAdapter.updatePosts(posts)
                } catch (e: Exception) {
                    Log.e("Error", "Parsing posts failed", e)
                    Toast.makeText(requireContext(), "Error parsing posts.", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(context, "Failed to retrieve posts from server.", Toast.LENGTH_SHORT).show()
                Log.e("Error", error.toString())
            }
        ) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return params.toString().toByteArray(Charsets.UTF_8)
            }
        }

        requestQueue.add(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}