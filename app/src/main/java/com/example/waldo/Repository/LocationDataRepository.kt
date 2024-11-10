package com.example.waldo.Repository

import android.content.Context
import android.util.Log
import com.example.waldo.Interfaces.ApiService
import com.example.waldo.Models.LocationData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class LocationDataRepository(private val apiService: ApiService, private val context: Context) {

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
}
