package com.example.waldo.Repository

import android.content.Context
import android.util.Log
import com.example.waldo.Interfaces.ApiService
import com.example.waldo.Models.LocationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LocationDataRepository(private val apiService: ApiService, private val context: Context) {

    // Recupera el token almacenado en SharedPreferences
    private fun getToken(): String? {
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", null)
    }

    // Obtener la última ubicación de un niño vinculado por ID
    fun getLocationById(id: String, callback: (LocationData?) -> Unit) {
        val token = getToken()
        if (token == null) {
            Log.e("LocationDataRepository", "No token found, unable to fetch location data.")
            callback(null)
            return
        }

        val authHeader = "Bearer $token"
        Log.d("LocationDataRepository", "Fetching location for user: $id with token: $authHeader")

        CoroutineScope(Dispatchers.IO).launch {
            apiService.getLocationById(id, authHeader).enqueue(object : Callback<LocationData> {
                override fun onResponse(call: Call<LocationData>, response: Response<LocationData>) {
                    if (response.isSuccessful) {
                        callback(response.body())
                        Log.d("LocationDataRepository", "Location received: ${response.body()}")
                    } else {
                        Log.e("LocationDataRepository", "Error fetching location: ${response.code()} - ${response.message()}")
                        callback(null)
                    }
                }

                override fun onFailure(call: Call<LocationData>, t: Throwable) {
                    Log.e("LocationDataRepository", "Failed to fetch location data", t)
                    callback(null)
                }
            })
        }
    }
}
