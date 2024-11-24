package com.example.waldo

import android.app.AlertDialog
import android.content.Context
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
import com.example.waldo.API.Observer
import com.example.waldo.permission.PermissionManager
import com.example.waldo.API.REST
import com.example.waldo.Classes.DataCodes
import com.example.waldo.Classes.IntegratorCamera
import com.example.waldo.DTO.CreateEnrollmentDTO
import com.example.waldo.Interfaces.ApiService
import com.example.waldo.Models.Code
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
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
    private lateinit var disposablesKids: CompositeDisposable
    private lateinit var observer: Observer
    private lateinit var navigationView: BottomNavigationView

    private val markersMap = mutableMapOf<String, Marker>() // Map para asociar IDs de niños con sus marcadores

    // Connection status
    private lateinit var connectionStatusRepository: ConnectionStatusRepository
    private val linkedKids = mutableListOf<User>() // Lista de niños vinculados

    //
    private var locationPollingDisposable: Disposable? = null // Nueva variable para manejar la suscripción
    private var selectedChildId: String? = null // Variable para almacenar el ID del niño seleccionado

    companion object {
        private const val TAG = "ParentMainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupRecyclerView()
        clearUserData()

        firebaseAuth = FirebaseAuth.getInstance()
        permissionManager = PermissionManager(this)
        locationDataRepository = LocationDataRepository(REST.getRestEngine().create(ApiService::class.java), this)
        codeRepository = CodeRepository(REST.getRestEngine().create(ApiService::class.java), this)
        enrollmentRepository = EnrollmentRepository(REST.getRestEngine().create(ApiService::class.java), this)
        userRepository = UserRepository(REST.getRestEngine().create(ApiService::class.java), this)
        connectionStatusRepository = ConnectionStatusRepository(REST.getRestEngine().create(ApiService::class.java), this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupButtons()
        requestNotificationPermissionIfNeeded()


        //Cargamos los kids vinculados a ese padre
        loadCodesParent()
        //Inicializa todos los componentes
        initViewComponents()

        navigationView.setOnNavigationItemSelectedListener  { menuItem ->
            when(menuItem.itemId){
                R.id.nav_historial -> {
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }
    }

    private fun initViewComponents() {
        disposablesKids = CompositeDisposable()
        observer = Observer()

        // Llama al observer con el adaptador existente
        observer.observeData(
            apiService = REST.getRestEngine().create(ApiService::class.java),
            disposables = disposablesKids,
            activity = this,
            kidsAdapter = kidsAdapter // Pasa el adaptador inicializado
        )
        navigationView = findViewById(R.id.bottomNavBar)
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.kidsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        kidsAdapter = KidsAdapter(EnrollmentRepository(REST.getRestEngine().create(ApiService::class.java), this),mutableListOf()) { selectedKid ->
            val selectedCode = DataCodes.instance.getCodes().find { it?.id_User == selectedKid.id_User }
            selectedChildId = selectedCode?.id_User

            if (selectedChildId != null) {
                Log.d(TAG, "Siguiendo al niño: ${selectedKid.name}")
                fetchSingleChildLocation()
            } else {
                Log.e(TAG, "No se encontró el código para el niño seleccionado.")
            }
        }

        recyclerView.adapter = kidsAdapter
    }

    private var singleChildLocationDisposable: Disposable? = null // Variable para manejar la suscripción

    private fun fetchSingleChildLocation() {
        // Detén cualquier flujo previo antes de iniciar uno nuevo
        singleChildLocationDisposable?.dispose()

        if (selectedChildId == null) {
            Log.e(TAG, "No hay niño seleccionado para seguir.")
            return
        }

        singleChildLocationDisposable = locationDataRepository.getLocationById(selectedChildId!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { locationData ->
                    updateMapWithChildLocation(locationData)
                    Log.d(TAG, "Actualizando ubicación para el niño con ID: $selectedChildId")
                },
                { error -> Log.e(TAG, "Error al obtener la ubicación del niño seleccionado", error) }
            )
    }

    private fun loadCodesParent(){
        linkedKids.clear()
        enrollmentRepository.getEnrolledKids { kids ->
            if (kids != null && kids.isNotEmpty()) {
                linkedKids.addAll(kids)
                val codes = kids.map { kid ->
                    Code(
                        id = 0, // Si no tienes un valor específico, usa un placeholder como `0`
                        id_User = kid.id,
                        code = "", // Placeholder si no tienes el código
                        isAvaible = true // O establece un valor booleano apropiado
                    )
                }
                DataCodes.instance.getCodes().clear()
                DataCodes.instance.getCodes().addAll(codes)
                fetchChildrenLocations()
            } else {
                Log.d(TAG, "No hay niños vinculados para este usuario.")
            }
        }
    }

    private fun clearUserData() {
        // Limpia los códigos en memoria
        DataCodes.instance.getCodes().clear()

        // Detiene el flujo de localización
        locationPollingDisposable?.dispose()
        locationPollingDisposable = null

        // Limpia los marcadores del mapa
        markersMap.values.forEach { it.remove() }
        markersMap.clear()

        linkedKids.clear()
        hasFocusedOnChildren = false

        kidsAdapter.updateKidsList(emptyList())
        Log.d("UserData", "Datos del usuario y mapa limpiados")
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
            clearUserData() // Limpia los datos antes de cerrar sesión

            val sharedPreferences = getSharedPreferences("auth", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply() // Limpia todos los datos guardados

            firebaseAuth.signOut()
            startActivity(Intent(this, SignInActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            })
            finish()
        }

        findViewById<Button>(R.id.btn_vincular).setOnClickListener {
            showOptionsDialog()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        //empieza el traqueo para el niño
        fetchChildrenLocations()
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
    }

    private fun fetchChildrenLocations() {
        Log.d(TAG, "Starting fetching to kids")
        locationPollingDisposable?.dispose()

        val pollingObservable = Observable.interval(0, 3, TimeUnit.SECONDS)
            .flatMap {
                Observable.fromIterable(DataCodes.instance.getCodes().filterNotNull())
                    .filter { code ->
                        Log.d(TAG, "fetchChildrenLocations: ${DataCodes.instance.getCodes().size}")
                        linkedKids.any { it.id == code.id_User }
                    }
                    .flatMapSingle { code ->
                        locationDataRepository.getLocationById(code.id_User.toString())
                            .doOnError { error -> Log.e(TAG, "Error fetching location for ID: ${code.id_User}", error) }
                    }
            }

        locationPollingDisposable = pollingObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { locationData -> updateMapWithChildLocation(locationData)},
                { error -> Log.e(TAG, "Error in location polling", error) }
            )
        disposables.add(locationPollingDisposable!!)
    }

    private fun updateMapWithChildLocation(locationData: LocationData) {

        val childLatLng = LatLng(locationData.latitude, locationData.longitude)
        Log.d(TAG, "Actualizando location Kid: ${locationData.id_User}")
        if (markersMap.containsKey(locationData.id_User)) {
            // Actualiza la posición del marcador existente
            markersMap[locationData.id_User]?.position = childLatLng
        } else {
            // Agrega un nuevo marcador si no existe
            val marker = map.addMarker(
                MarkerOptions()
                    .position(childLatLng)
                    .title("Batería: ${locationData.batteryLevel}%")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )
            if (marker != null) {
                markersMap[locationData.id_User] = marker
            }
        }

        // Centra la cámara en el niño seleccionado
        if (locationData.id_User == selectedChildId) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(childLatLng, 16f))
            Log.d(TAG, "Cámara centrada en el niño con ID: ${locationData.id_User}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
        singleChildLocationDisposable?.dispose()
    }

    private fun showOptionsDialog() {
        val input = EditText(this)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Ingresa el código de vinculación")
            .setMessage("Introduce el código del niño para vincular:")
            .setView(input)
            .setPositiveButton("Aceptar") { dialog, _ ->
                val enteredText = input.text.toString()
                fetchChildrenLocations()
                codeRepository.getLastCode(enteredText) { code ->
                    if (code != null) {
                        // Lógica para crear la vinculación con el callback
                        enrollmentRepository.createEnrollment(CreateEnrollmentDTO(firebaseAuth.currentUser?.uid.toString(), code.id_User)) { success ->
                            if (success) {
                                Log.d(TAG, "Vinculación creada con éxito para el niño ID: ${code.id_User}")
                                loadCodesParent()
                            } else {
                                Toast.makeText(this, "Error al crear la vinculación", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Log.e(TAG, "Código inválido")
                        Toast.makeText(this, "Código inválido", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNeutralButton("Escanear código QR") { dialog, _ ->
                integrator.initiateScan()
            }
            .create()

        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_LONG).show()
            } else {
                val qrContent = result.contents
                Toast.makeText(this, "Su código es: $qrContent", Toast.LENGTH_LONG).show()

                // Llama al método getLastCode con un callback
                codeRepository.getLastCode(qrContent) { code ->
                    if (code != null) {
                        // Si el código es válido, realiza la vinculación
                        enrollmentRepository.createEnrollment(CreateEnrollmentDTO(firebaseAuth.currentUser?.uid.toString(), code.id_User)) { success ->
                            if (success) {
                                Log.d(TAG, "Vinculación creada con éxito para el niño ID: ${code.id_User}")
                                loadCodesParent()
                            } else {
                                Toast.makeText(this, "Error al crear la vinculación", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Código QR inválido o expirado", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}