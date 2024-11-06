package com.example.beacon.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.beacon.R
import com.example.beacon.databinding.FragmentMapBinding
import com.example.beacon.utils.Constants
import com.example.beacon.utils.Conversion
import com.example.beacon.view_models.UserViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.material.slider.Slider
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null

    private lateinit var _map: GoogleMap
    private lateinit var _location: LatLng
    private lateinit var _slider: Slider

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

        configureSlider(root)

        return root
    }

    fun configureSlider(root: View) {
        _slider = root.findViewById(R.id.rangeSlider)
        val rangeTextView = root.findViewById<TextView>(R.id.rangeTextView)
        _slider.addOnChangeListener { _, value, _ ->
            val range = Constants.RADIUS_MAX.pow(value.toDouble()/100)
            val unit = "km"
            val formatted = "${BigDecimal(range).setScale(3, RoundingMode.UP)} $unit"
            rangeTextView.text = formatted
        }

        _slider.addOnSliderTouchListener(object: Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // do nothing
            }

            override fun onStopTrackingTouch(slider: Slider) {
                _map.clear()
                val range = Constants.RADIUS_MAX.pow(slider.value.toDouble()/100)
                val circle = CircleOptions()
                    .fillColor(requireActivity().getColor(R.color.range_fill))
                    .strokeColor(requireActivity().getColor(R.color.range_stroke))
                    .center(_location)
                    .radius(range*1000) // in meters
                _map.addCircle(circle)

                val radiusLat = Conversion.kmToLat(range)
                val radiusLong = Conversion.kmToLong(_location.latitude, range)
                val neBound = LatLng(_location.latitude-radiusLat, _location.longitude-radiusLong)
                val swBound = LatLng(_location.latitude+radiusLat, _location.longitude+radiusLong)
                _map.moveCamera(CameraUpdateFactory.newLatLngBounds(
                    LatLngBounds(neBound, swBound), 10
                ))
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(map: GoogleMap) {
        val userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        userViewModel.requestedLocation.value = true
        _map = map

        userViewModel.location.observe(viewLifecycleOwner) { location ->
            if (userViewModel.requestedLocation.value == true) {
                _slider.isEnabled = true
                _location = location
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    location, 18f
                ))
                userViewModel.requestedLocation.value = false
            }
        }
    }
}