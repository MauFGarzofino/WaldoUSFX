package com.example.waldo

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.waldo.permission.PermissionManager
import com.example.waldo.API.REST
import com.example.waldo.Classes.DataCodes
import com.example.waldo.DTO.CreateEnrollmentDTO
import com.example.waldo.Interfaces.ApiService
import com.example.waldo.Models.LocationData
import com.example.waldo.Repository.CodeRepository
import com.example.waldo.Repository.EnrollmentRepository
import com.example.waldo.Repository.LocationDataRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var locationDataRepository: LocationDataRepository
    private lateinit var permissionManager: PermissionManager
    private lateinit var codeRepository: CodeRepository
    private lateinit var enrollmentRepository: EnrollmentRepository

    private val disposables = CompositeDisposable() // Para gestionar las suscripciones

    companion object {
        private const val TAG = "ParentMainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseAuth = FirebaseAuth.getInstance()
        permissionManager = PermissionManager(this)
        locationDataRepository = LocationDataRepository(REST.getRestEngine().create(ApiService::class.java), this)
        codeRepository = CodeRepository(REST.getRestEngine().create(ApiService::class.java), this)
        enrollmentRepository = EnrollmentRepository(REST.getRestEngine().create(ApiService::class.java), this)

        val btn_vincular = findViewById<Button>(R.id.btn_vincular)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupLogoutButton()
        requestNotificationPermissionIfNeeded()

        btn_vincular.setOnClickListener {
            showInputDialog()
        }
        findViewById<Button>(R.id.btn_view_history).setOnClickListener {
            showCodes()
        }
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

        // Observar la ubicación

    }

    private fun fetchChildrenLocations(code : String) {
        val pollingObservable = Observable.interval(0, 10, TimeUnit.SECONDS)
            .flatMap {
                Observable.fromIterable(DataCodes.instance.getCodes().filterNotNull())
                    .flatMapSingle { code ->
                        enrollmentRepository.createEnrollment(CreateEnrollmentDTO(firebaseAuth.currentUser?.uid.toString(), code.id_User))
                        locationDataRepository.getLocationById(code?.id_User.toString())
                            .doOnError { error ->
                                Log.e(TAG, "Error fetching location for ID: ${code?.id_User}", error)
                            }
                    }
            }
        disposables.add(
            pollingObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { locationData ->
                        updateMapWithChildLocation(locationData)
                    },
                    { error ->
                        Log.e(TAG, "Error in location polling", error)
                    }
                )
        )
    }

    private fun updateMapWithChildLocation(locationData: LocationData) {

        Log.e("Location", "${locationData.longitude} ${locationData.longitude}")
        val childLatLng = LatLng(locationData.latitude, locationData.longitude)
        map.clear()
        map.addMarker(
            MarkerOptions()
                .position(childLatLng)
                .title("Nivel de batería: ${locationData.batteryLevel}%")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
        )
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(childLatLng, 16f))
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear() // Limpia las suscripciones
    }
    fun showInputDialog(){

        val input = EditText(this)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Ingresa un texto")
            .setMessage("Escribe algo:")
            .setView(input)
            .setPositiveButton("OK") { dialog, which ->
                val enteredText = input.text.toString()
                codeRepository.getLastCode(enteredText)
                Log.e("Text Entered", "Latitud: $enteredText")
                fetchChildrenLocations(enteredText)
            }
            .setNegativeButton("Cancelar") { dialog, which ->
                dialog.cancel()
            }
            .create()

        dialog.show()
    }
    fun showCodes(){
        DataCodes.instance.getCodes().forEach{ code ->
            Log.d("Code of array codes", "Code: ${code?.code} Kid: ${code?.id_User}")
        }
    }
}