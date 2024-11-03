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
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.beacon.R
import com.example.beacon.adapters.PostsAdapter
import com.example.beacon.databinding.FragmentHomeBinding
import com.example.beacon.models.BeaconPost
import com.example.beacon.utils.Constants.BACKEND_IP
import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.json.Json

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val dummyPosts = listOf(
            BeaconPost("Anonymous", "Some text here", 49.279613292501836, -122.92005152774361),
            BeaconPost("Anonymous", "Some text here", 49.279613292501836, -122.92005152774361)
        )

        val postsAdapter = PostsAdapter(requireActivity(), dummyPosts)
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
        val request = StringRequest(Request.Method.GET, url,
        { response ->
            val posts = Json.decodeFromString(BeaconPost.serializer(), response)
            Log.d("debug", posts.toString())
        },
        { error ->
            Toast.makeText(context, "Failed to get from server", Toast.LENGTH_SHORT).show()
            Log.d("Error", error.toString())
        })
        requestQueue.add(request)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}