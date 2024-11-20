package com.example.waldo

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.waldo.permission.PermissionManager
import com.example.waldo.API.REST
import com.example.waldo.Classes.DataCodes
import com.example.waldo.Classes.IntegratorCamera
import com.example.waldo.DTO.CreateEnrollmentDTO
import com.example.waldo.Interfaces.ApiService
import com.example.waldo.Models.KidDisplayModel
import com.example.waldo.Models.LocationData
import com.example.waldo.Models.User
import com.example.waldo.Repository.CodeRepository
import com.example.waldo.Repository.ConnectionStatusRepository
import com.example.waldo.Repository.EnrollmentRepository
import com.example.waldo.Repository.LocationDataRepository
import com.example.waldo.Repository.UserRepository
import com.example.waldo.ui.KidsAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
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
    private var hasFocusedOnChildren = false // Para controlar el zoom automático
    private lateinit var userRepository: UserRepository
    private val integrator = IntegratorCamera.getIntegrator(this) // get integrator to camera
    private lateinit var kidsAdapter: KidsAdapter

    // Connection status
    private lateinit var connectionStatusRepository: ConnectionStatusRepository
    private val linkedKids = mutableListOf<User>() // Lista de niños vinculados

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
        userRepository = UserRepository(REST.getRestEngine().create(ApiService::class.java), this)

        // Connection Status
        connectionStatusRepository = ConnectionStatusRepository(REST.getRestEngine().create(ApiService::class.java), this)

        linkedKids.clear()

        // Configurar RecyclerView y su adaptador
        setupRecyclerView()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupButtons()
        requestNotificationPermissionIfNeeded()

        // Obtener niños vinculados al iniciar
        fetchLinkedKids()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.kidsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        kidsAdapter = KidsAdapter(mutableListOf()) // Lista inicial vacía
        recyclerView.adapter = kidsAdapter
    }

    private fun fetchLinkedKids() {
        enrollmentRepository.getEnrolledKids { kids ->
            if (kids != null) {
                linkedKids.clear() // Limpia la lista de niños vinculados
                linkedKids.addAll(kids) // Agrega los niños recién obtenidos

                val displayModels = kids.map { kid ->
                    KidDisplayModel(
                        name = "${kid.givenName} ${kid.familyName}",
                        connectionStatus = "Cargando estado..." // Placeholder inicial
                    )
                }

                kidsAdapter.updateKidsList(displayModels) // Actualiza el adaptador con los nuevos datos
                fetchConnectionStatuses() // Obtener los estados de conexión
            } else {
                Log.e("MainActivity", "Error al obtener los niños vinculados")
            }
        }
    }

    private fun fetchConnectionStatuses() {
        linkedKids.forEachIndexed { index, kid ->
            connectionStatusRepository.getLatestConnectionStatus(kid.id) { status ->
                if (status != null) {
                    val updatedKid = KidDisplayModel(
                        name = "${kid.givenName} ${kid.familyName}",
                        connectionStatus = status.connectionStatus
                    )
                    kidsAdapter.updateKid(index, updatedKid)
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (!permissionManager.hasNotificationPermission()) {
            permissionManager.requestNotificationPermission(this, 101)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permiso de notificación concedido.")
        } else {
            Log.e(TAG, "Permiso de notificación denegado.")
        }
    }

    private fun setupButtons() {
        val logoutButton = findViewById<Button>(R.id.btn_logout)
        logoutButton.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, SignInActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            })
            finish()
        }
        findViewById<Button>(R.id.btn_view_history).setOnClickListener { showCodes() }
        findViewById<Button>(R.id.btn_vincular).setOnClickListener {
            showOptionsDialog()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        fetchChildrenLocations() // Inicia la actualización de ubicaciones
    }

    private fun fetchChildrenLocations() {
        // Realiza la actualización periódica de todas las ubicaciones vinculadas
        val pollingObservable = Observable.interval(0, 10, TimeUnit.SECONDS)
            .flatMap {
                Observable.fromIterable(DataCodes.instance.getCodes().filterNotNull())
                    .flatMapSingle { code ->
                        enrollmentRepository.createEnrollment(CreateEnrollmentDTO(firebaseAuth.currentUser?.uid.toString(), code.id_User))
                        locationDataRepository.getLocationById(code.id_User.toString())
                            .doOnError { error -> Log.e(TAG, "Error fetching location for ID: ${code.id_User}", error) }
                    }
            }

        disposables.add(
            pollingObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { locationData -> updateMapWithChildLocation(locationData) },
                    { error -> Log.e(TAG, "Error in location polling", error) }
                )
        )
    }

    private fun updateMapWithChildLocation(locationData: LocationData) {
        val childLatLng = LatLng(locationData.latitude, locationData.longitude)

        userRepository.getUserById(locationData.id_User) { user ->

            val childName = "${user?.givenName ?: "Niño"} ${user?.familyName ?: ""}"
            val markerTitle = "$childName - Batería: ${locationData.batteryLevel}%"

            // Agregar el marcador
            map.addMarker(
                MarkerOptions()
                    .position(childLatLng)
                    .title(markerTitle)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )

            // Realizar zoom solo la primera vez
            if (!hasFocusedOnChildren) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(childLatLng, 16f))
                hasFocusedOnChildren = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear() // Limpia las suscripciones
    }

    private fun showOptionsDialog() {
        val input = EditText(this)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Ingresa el código de vinculación")
            .setMessage("Introduce el código del niño para vincular:")
            .setView(input)
            .setPositiveButton("Aceptar") { dialog, _ ->
                val enteredText = input.text.toString()
                codeRepository.getLastCode(enteredText)
                fetchLinkedKids()
                Log.e("Text Entered", "Código ingresado: $enteredText")
            }
            .setNeutralButton("Escanear codígo QR") { dialog, _ ->
                integrator.initiateScan()
            }
            .create()

        dialog.show()
    }
    private fun showCodes() {
        DataCodes.instance.getCodes().forEach { code ->
            Log.d("Code of array codes", "Code: ${code?.code} Kid: ${code?.id_User}")
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_LONG).show()
            } else {
                val qrContent = result.contents
                Toast.makeText(this, "Su codígo es: $qrContent", Toast.LENGTH_LONG).show()
                codeRepository.getLastCode(qrContent)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}