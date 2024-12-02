package com.example.beacon.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.beacon.R
import com.example.beacon.adapters.PostsAdapter
import com.example.beacon.databinding.FragmentHomeBinding
import com.example.beacon.models.BeaconPost
import com.example.beacon.utils.Constants.BACKEND_IP
import com.example.beacon.view_models.UserViewModel
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.json.JSONObject

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var requestQueue: RequestQueue
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 60000L // 1 minute in milliseconds
    
    private val updateRunnable = object : Runnable {
        override fun run() {
            getPostsFromServer()
            handler.postDelayed(this, updateInterval)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        progressBar = root.findViewById(R.id.progressBar)
        requestQueue = Volley.newRequestQueue(requireActivity())
        val postListView = root.findViewById<ListView>(R.id.postsListView)
        val userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        postsAdapter = PostsAdapter(requireActivity(), mutableListOf(), progressBar, null)
        userViewModel.location.observe(viewLifecycleOwner) { currentLocation->
            if( currentLocation!=null){
                postsAdapter = PostsAdapter(requireActivity(), mutableListOf(), progressBar, currentLocation)
                postListView.adapter = postsAdapter
                Log.d("HomeFragment", "Location received: $currentLocation")
            }
            else{
                Toast.makeText(requireContext(), "Location unavailable. Cannot get posts.", Toast.LENGTH_SHORT).show()
                Log.d("HomeFragment", "Location is null")
            }
        }



        // Start periodic updates
        startPeriodicUpdates()

        return root
    }

    private fun startPeriodicUpdates() {
        handler.post(updateRunnable)
    }

    private fun stopPeriodicUpdates() {
        handler.removeCallbacks(updateRunnable)
    }

    private fun getPostsFromServer() {
        if (progressBar.visibility == View.VISIBLE) {
            return
        }

        val url = "$BACKEND_IP/get"

        // Get user's current location
        val userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        val currentLocation = userViewModel.location.value
        val range = userViewModel.range.value


        if (currentLocation == null) {
            Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        // Creating the JSON body with current latitude and longitude
        val params = JSONObject()
        params.put("latitude", currentLocation.latitude)
        params.put("longitude", currentLocation.longitude)
        params.put("range", range)

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    progressBar.visibility = View.INVISIBLE
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
                progressBar.visibility = View.INVISIBLE
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
        stopPeriodicUpdates()
        requestQueue.cancelAll(this)
        _binding = null
    }
}