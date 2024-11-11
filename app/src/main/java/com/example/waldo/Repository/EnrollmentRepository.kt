package com.example.waldo.Repository

import android.content.Context
import android.util.Log
import com.example.waldo.Classes.DataCodes
import com.example.waldo.DTO.CreateEnrollmentDTO
import com.example.waldo.Interfaces.ApiService
import com.example.waldo.Models.Code
import com.example.waldo.Models.Enrollment
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
    fun createEnrollment(createEnrollmentDTO: CreateEnrollmentDTO) {
        val token = getToken()
        if (token == null) {
            Log.e("AuthError", "No se encontró el token JWT en SharedPreferences")
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            apiService.createEnrollment(createEnrollmentDTO,"Bearer $token").enqueue(object : Callback<Enrollment> {
                override fun onResponse(call: Call<Enrollment>, response: Response<Enrollment>) {
                    if (response.isSuccessful) {
                        Log.d("Enrollment", "Enrollment created successfully")
                    } else {
                        Log.e("Enrollment", "Failed to create enrollment on response")
                    }
                }
                override fun onFailure(call: Call<Enrollment>, t: Throwable) {
                    Log.e("Enrollment", "Failed to create enrollment on request")
                }
            })
        }
    }
}