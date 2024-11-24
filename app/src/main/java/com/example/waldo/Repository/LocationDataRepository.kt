package com.example.waldo.Repository

import android.app.Activity
import android.content.Context
import android.provider.ContactsContract.Data
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.waldo.Interfaces.ApiService
import com.example.waldo.Models.HistoryKid
import com.example.waldo.Models.LocationData
import com.example.waldo.R
import com.example.waldo.ui.DataLocationAdapter
import com.example.waldo.ui.HistoryAdapter
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LocationDataRepository(private val apiService: ApiService, private val context: Context) {

    private lateinit var dataLocationAdapter : DataLocationAdapter
    private lateinit var recyclerView: RecyclerView
    // Recupera el token almacenado en SharedPreferences
    private fun getToken(): String? {
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", null)
    }

    // Obtener la última ubicación de un niño vinculado por ID
    fun getLocationById(id: String): Single<LocationData> {
        val token = getToken() ?: return Single.error(Throwable("Token not found"))

        return Observable.fromCallable {
            apiService.getLocationById(id, "Bearer $token").blockingFirst()
        }.firstOrError()
    }

    fun getHistoryForKid(id_Kid : String){
        val token = getToken()
        if (token == null) {
            Log.e("History Repository", "No se encontró el token")
            return
        }
        apiService.getHistoryLocations(id_Kid,"Bearer $token").enqueue(object : Callback<List<LocationData>> {
            override fun onResponse(call: Call<List<LocationData>>, response: Response<List<LocationData>>) {
                if (response.isSuccessful) {
                    val activity = context as Activity
                    val dataLocations = response.body()
                    Log.d("History Repository", "Niños obtenidos del servidor: ${dataLocations?.size}")
                    dataLocationAdapter = DataLocationAdapter(dataLocations!!)
                    recyclerView = activity.findViewById(R.id.locationsRecyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(activity)
                    recyclerView.adapter = dataLocationAdapter
                } else {
                    Log.e("History Repository", "Error al obtener niños vinculados: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<LocationData>>, t: Throwable) {
                Log.e("History Repository", "Error de conexión", t)
            }
        })
    }
}
