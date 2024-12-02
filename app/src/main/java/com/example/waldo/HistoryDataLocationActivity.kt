package com.example.waldo

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.waldo.API.REST
import com.example.waldo.Interfaces.ApiService
import com.example.waldo.Models.LocationData
import com.example.waldo.Repository.LocationDataRepository
import com.example.waldo.ui.DataLocationAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.bottomnavigation.BottomNavigationView

class HistoryDataLocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var locationDataRepository : LocationDataRepository
    private lateinit var navigationView : BottomNavigationView
    private lateinit var map: GoogleMap
    private lateinit var dataLocationAdapter: DataLocationAdapter
    private lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history_data_location)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val intent = intent
        val idKid = intent.getStringExtra("id_Kid")
        navigationView = findViewById(R.id.bottomNavBarReport)
        locationDataRepository = LocationDataRepository(REST.getRestEngine().create(ApiService::class.java), this)

        locationDataRepository.getHistoryForKid(idKid.toString()){ datalocations ->
            recyclerView = findViewById(R.id.locationsRecyclerView)
            recyclerView.layoutManager = LinearLayoutManager(this)
            dataLocationAdapter = DataLocationAdapter(datalocations!!)
            recyclerView.adapter = dataLocationAdapter
            showLocationOnMap(datalocations!!)
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_routes) as SupportMapFragment
        mapFragment.getMapAsync(this)

        navigationView.setOnNavigationItemSelectedListener  { menuItem ->
            when(menuItem.itemId){
                R.id.nav_report_battery -> {
                    val intent = Intent(this, BatteryLevelReportActivity::class.java)
                        .putExtra("id_Kid", idKid)
                    startActivity(intent)
                }
            }
            true
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        // Habilitar controles de UI
        map.uiSettings.isZoomControlsEnabled = true  // Habilitar botones de zoom
        map.uiSettings.isCompassEnabled = true      // Mostrar la brújula
        map.uiSettings.isMyLocationButtonEnabled = true // Habilitar botón para centrar en la ubicación del usuario
        map.uiSettings.isScrollGesturesEnabled = true   // Permitir desplazarse
        map.uiSettings.isZoomGesturesEnabled = true     // Permitir zoom
        map.uiSettings.isRotateGesturesEnabled = true   // Permitir rotación
        map.uiSettings.isTiltGesturesEnabled = true     // Permitir inclinar el mapa
    }
    fun showLocationOnMap(locations : List<LocationData>){
        if (locations.isNotEmpty()){
            val polylineOptions = PolylineOptions().color(android.graphics.Color.parseColor("#1b6ff2")).width(10f)

            locations.forEach{location->
                val latIng = LatLng(location.latitude, location.longitude)
                map.addMarker(MarkerOptions()
                    .position(latIng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    .title("Bateria: ${location.batteryLevel} %"))

                polylineOptions.add(latIng)
            }
            map.addPolyline(polylineOptions)
            val firstLocation = locations.first()
            val firstLatLng = LatLng(firstLocation.latitude, firstLocation.longitude)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 18f))
        }
    }

}