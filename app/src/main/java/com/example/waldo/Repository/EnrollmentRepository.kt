package com.example.waldo.Repository

import android.content.Context
import android.util.Log
import com.example.waldo.Classes.DataCodes
import com.example.waldo.DTO.CreateEnrollmentDTO
import com.example.waldo.Interfaces.ApiService
import com.example.waldo.Models.Code
import com.example.waldo.Models.Enrollment
import com.example.waldo.Models.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EnrollmentRepository(private val apiService: ApiService, private val context: Context) {

    private fun getToken(): String? {
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", null)
    }

    fun createEnrollment(createEnrollmentDTO: CreateEnrollmentDTO, callback: (Boolean) -> Unit) {
        val token = getToken()
        if (token == null) {
            Log.e("AuthError", "No se encontró el token JWT en SharedPreferences")
            callback(false) // Llama al callback con `false` indicando error
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            apiService.createEnrollment(createEnrollmentDTO, "Bearer $token")
                .enqueue(object : Callback<Enrollment> {
                    override fun onResponse(
                        call: Call<Enrollment>,
                        response: Response<Enrollment>
                    ) {
                        if (response.isSuccessful) {
                            Log.d("Enrollment", "Enrollment created successfully")
                            callback(true) // Llama al callback con `true` indicando éxito
                        } else {
                            Log.e("Enrollment", "Failed to create enrollment on response")
                            callback(false) // Llama al callback con `false` indicando error
                        }
                    }

                    override fun onFailure(call: Call<Enrollment>, t: Throwable) {
                        Log.e("Enrollment", "Failed to create enrollment on request")
                        callback(false) // Llama al callback con `false` indicando error
                    }
                })
        }
    }

    // Obtener los niños vinculados
    fun getEnrolledKids(onResult: (List<User>?) -> Unit) {
        val token = getToken()
        if (token == null) {
            Log.e("EnrollmentRepo", "No se encontró el token")
            onResult(null)
            return
        }

        apiService.getOnceEnrolledKids("Bearer $token").enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    val kids = response.body()
                    Log.d("EnrollmentRepo", "Niños obtenidos del servidor: ${kids?.size}")
                    onResult(kids)
                } else {
                    Log.e("EnrollmentRepo", "Error al obtener niños vinculados: ${response.code()}")
                    onResult(null)
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e("EnrollmentRepo", "Error de conexión", t)
                onResult(null)
            }
        })
    }
}
