package com.example.beacon.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import com.google.android.material.slider.Slider
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.ln
import kotlin.math.pow

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null

    private var _map: GoogleMap? = null
    private lateinit var _location: LatLng
    private lateinit var _slider: Slider
    private lateinit var _rangeTextView: TextView

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

        _slider = root.findViewById(R.id.rangeSlider)
        _rangeTextView = root.findViewById(R.id.rangeTextView)
        configureSlider()

        return root
    }

    private fun configureSlider() {
        val sharedPrefs = requireActivity().getSharedPreferences(
            Constants.SP_KEY, Context.MODE_PRIVATE)
        val initialRange = sharedPrefs.getFloat(Constants.SP_RANGE_KM, 5.0f)
        _slider.value = rangeToSliderValue(initialRange)
        updateUIRange(_slider.value)

        _slider.addOnChangeListener { _, value, _ ->
            updateUIRange(value)
        }

        _slider.addOnSliderTouchListener(object: Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                val range = sliderValueToRange(slider.value).toFloat()
                with (sharedPrefs.edit()) {
                    putFloat(Constants.SP_RANGE_KM, range)
                    apply()
                }

                val userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
                userViewModel.range.value = range
            }
        })
    }

    private fun rangeToSliderValue(range: Float): Float {
        return (100* ln(range) / ln(Constants.RADIUS_MAX).toFloat())
    }

    private fun sliderValueToRange(sliderValue: Float): Double {
        return Constants.RADIUS_MAX.pow(sliderValue.toDouble()/100)
    }

    private fun updateUIRange(sliderValue: Float) {
        val range = sliderValueToRange(sliderValue)
        val unit = "km"
        val formatted = "${BigDecimal(range).setScale(3, RoundingMode.UP)} $unit"
        _rangeTextView.text = formatted
        drawRangeCircle(range)
    }

    private fun drawRangeCircle(range: Double) {
        if (_map != null) {
            val map = _map!!
            map.clear()
            val circle = CircleOptions()
                .fillColor(requireActivity().getColor(R.color.range_fill))
                .strokeColor(requireActivity().getColor(R.color.range_stroke))
                .center(_location)
                .radius(range * 1000) // in meters
            map.addCircle(circle)

            val radiusLat = Conversion.kmToLat(range)
            val radiusLong = Conversion.kmToLong(_location.latitude, range)
            val neBound =
                LatLng(_location.latitude - radiusLat, _location.longitude - radiusLong)
            val swBound =
                LatLng(_location.latitude + radiusLat, _location.longitude + radiusLong)
            map.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                    LatLngBounds(neBound, swBound), 10
                )
            )
        }
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
                updateUIRange(_slider.value)
                userViewModel.requestedLocation.value = false
            }
        }
    }
}