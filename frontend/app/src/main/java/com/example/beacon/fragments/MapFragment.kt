package com.example.beacon.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.beacon.R
import com.example.beacon.databinding.FragmentMapBinding
import com.example.beacon.models.BeaconPost
import com.example.beacon.view_models.UserViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(map: GoogleMap) {
        val userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        userViewModel.requestedLocation.value = true

        userViewModel.location.observe(viewLifecycleOwner) { location ->
            if (userViewModel.requestedLocation.value == true) {
                val marker = MarkerOptions().position(location)
                map.addMarker(marker)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    location, 18f
                ))
                Toast.makeText(activity, location.toString(), Toast.LENGTH_SHORT).show()
                userViewModel.requestedLocation.value = false
            }
        }
    }
}