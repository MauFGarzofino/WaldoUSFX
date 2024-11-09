package com.example.waldo.Repository

import android.content.Context
import android.util.Log
import com.example.waldo.Interfaces.ApiService
import com.example.waldo.Models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserRepository(private val apiService: ApiService, private val context: Context) {

    // Devuelve la llamada para crear el usuario en el backend
    fun createUser(user: User): Call<User> {
        return apiService.createUser(user)
    }

    // Recupera el token almacenado en SharedPreferences
    private fun getToken(): String? {
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", null)
    }

    fun saveToken(token: String) {
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("jwt_token", token)
            apply()
        }
    }

    // Método adicional para verificar la conexión o permisos específicos del usuario
    fun verifyUserConnection() {
        // Implementación opcional
    }
}
