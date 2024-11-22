package com.example.waldo.Repository

import android.content.Context
import android.util.Log
import com.example.waldo.DTO.ConnectionStatusDto
import com.example.waldo.Interfaces.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConnectionStatusRepository(
    private val apiService: ApiService,
    context: Context
) : BaseRepository(context) {

    fun getLatestConnectionStatus(userId: String, onResult: (ConnectionStatusDto?) -> Unit) {
        val token = getToken()
        if (token == null) {
            Log.e("ConnectionStatusRepo", "No se encontró el token")
            onResult(null)
            return
        }

        apiService.getLatestConnectionStatus(userId, "Bearer $token").enqueue(object : Callback<ConnectionStatusDto> {
            override fun onResponse(call: Call<ConnectionStatusDto>, response: Response<ConnectionStatusDto>) {
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    onResult(null)
                    Log.e("ConnectionStatusRepo", "Error al obtener el último estado: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ConnectionStatusDto>, t: Throwable) {
                onResult(null)
                Log.e("ConnectionStatusRepo", "Error de conexión", t)
            }
        })
    }

    fun getConnectionStatusHistory(userId: String, onResult: (List<ConnectionStatusDto>?) -> Unit) {
        val token = getToken()
        if (token == null) {
            Log.e("ConnectionStatusRepo", "No se encontró el token")
            onResult(null)
            return
        }

        apiService.getConnectionStatusHistory(userId, "Bearer $token").enqueue(object : Callback<List<ConnectionStatusDto>> {
            override fun onResponse(call: Call<List<ConnectionStatusDto>>, response: Response<List<ConnectionStatusDto>>) {
                if (response.isSuccessful) {
                    onResult(response.body())
                } else {
                    onResult(null)
                    Log.e("ConnectionStatusRepo", "Error al obtener el historial de conexión: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<ConnectionStatusDto>>, t: Throwable) {
                onResult(null)
                Log.e("ConnectionStatusRepo", "Error de conexión", t)
            }
        })
    }
}
