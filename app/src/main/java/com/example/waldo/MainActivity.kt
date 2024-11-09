package com.example.waldo

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.waldo.permission.PermissionManager
import com.example.waldo.API.REST
import com.example.waldo.Interfaces.ApiService
import com.example.waldo.Models.LocationData
import com.example.waldo.Repository.LocationDataRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var locationDataRepository: LocationDataRepository
    private lateinit var permissionManager: PermissionManager

    companion object {
        private const val TAG = "ParentMainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()
        permissionManager = PermissionManager(this)
        locationDataRepository = LocationDataRepository(REST.getRestEngine().create(ApiService::class.java), this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupLogoutButton()

        // Verificar y solicitar el permiso de notificación
        requestNotificationPermissionIfNeeded()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (!permissionManager.hasNotificationPermission()) {
            permissionManager.requestNotificationPermission(this, 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permiso de notificación concedido.")
            } else {
                Log.e(TAG, "Permiso de notificación denegado.")
            }
        }
    }

    private fun setupLogoutButton() {
        val logoutButton = findViewById<Button>(R.id.btn_logout)
        logoutButton.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, SignInActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            })
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        // Llama a la función para obtener la ubicación del niño
        fetchChildrenLocations()
    }

    private fun fetchChildrenLocations() {
        val childId = "Ci7ddBueLRMkcFFdyowqWYmjamB2"  // ID del niño
        locationDataRepository.getLocationById(childId) { locationData ->
            locationData?.let {
                Log.d("MainActivity", "Child location: $it")
                updateMapWithChildLocation(it)
            } ?: Log.e("MainActivity", "Failed to fetch child location.")
        }
    }

    private fun updateMapWithChildLocation(locationData: LocationData) {
        val childLatLng = LatLng(locationData.latitude, locationData.longitude)
        map.clear()

        // Añadir un marcador en la ubicación del niño
        map.addMarker(
            MarkerOptions()
                .position(childLatLng)
                .title("Nivel de batería: ${locationData.batteryLevel}%")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
        )

        // Centrar el mapa en la ubicación del niño
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(childLatLng, 16f))
    }
}
