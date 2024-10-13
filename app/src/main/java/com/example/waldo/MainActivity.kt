package com.example.waldo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    // Inicializamos después
    private lateinit var map:GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createFragment()
    }

    private fun createFragment() {
        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        createMarker()
        drawCircle()
    }

    private fun createMarker() {
        val coordinates = LatLng(-19.039633557431262, -65.25686524810982)
        val marker = MarkerOptions().position(coordinates).title("Facultad de Tecnología")
        map.addMarker(marker)
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(coordinates, 18f),
            2000,
            null
        )
    }
    private fun drawCircle(){
        val circleOptions = CircleOptions()
            .center(LatLng(-19.039633557431262, -65.25686524810982))
            .radius(50.0)
            .strokeWidth(4f)
            .strokeColor(0xFF009bff.toInt())
            .fillColor(0x55bad9ee)

        map.addCircle(circleOptions)
    }
}