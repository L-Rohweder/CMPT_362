package com.example.beacon.activities

import android.Manifest
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
import com.example.beacon.utils.Constants.EXTRA_LOCATION
import com.example.beacon.view_models.UserViewModel
import com.google.android.gms.maps.model.LatLng

class TabbedActivity : AppCompatActivity(), LocationListener {
    private lateinit var binding: ActivityTabbedBinding
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val location = intent.getParcelableExtra(EXTRA_LOCATION, LatLng::class.java)
        if (location != null) {
            val userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
            userViewModel.location.value = location
        }

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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 100f, this)

        }
        catch (_: SecurityException) {
        }
    }

    override fun onLocationChanged(location: Location) {
        val userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        userViewModel.location.value = LatLng(location.latitude, location.longitude)
    }
}