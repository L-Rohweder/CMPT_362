package com.example.beacon.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.beacon.R
import com.example.beacon.databinding.ActivityTabbedBinding
import com.example.beacon.utils.Constants
import com.example.beacon.utils.Constants.EXTRA_LOCATION
import com.example.beacon.view_models.UserViewModel
import com.google.android.gms.maps.model.LatLng

class TabbedActivity : AppCompatActivity(), LocationListener {
    private lateinit var binding: ActivityTabbedBinding
    private lateinit var locationManager: LocationManager
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        
        // Get location from intent using type-safe getParcelable with null check
        val location = try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(EXTRA_LOCATION, LatLng::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<LatLng>(EXTRA_LOCATION)
            }
        } catch (e: Exception) {
            null
        }

        if (location != null) {
            userViewModel.location.value = location
        } else {
            Toast.makeText(this, "Waiting for location...", Toast.LENGTH_SHORT).show()
        }

        val sharedPrefs = getSharedPreferences(Constants.SP_KEY, Context.MODE_PRIVATE)
        userViewModel.range.value = sharedPrefs.getFloat(Constants.SP_RANGE_KM, 5.0f)

        binding = ActivityTabbedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_tabbed)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_create,
                R.id.navigation_map,
                R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        initLocationManager()
    }

    private fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000, // Update every 5 seconds
                    10f,  // Or when moved 10 meters
                    this
                )
                
                // Get last known location as backup
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null && userViewModel.location.value == null) {
                    userViewModel.location.value = LatLng(lastLocation.latitude, lastLocation.longitude)
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onLocationChanged(location: Location) {
        userViewModel.location.value = LatLng(location.latitude, location.longitude)
    }
}