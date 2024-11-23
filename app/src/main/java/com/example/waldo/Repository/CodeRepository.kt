package com.example.waldo.Repository

import android.content.Context
import android.provider.ContactsContract.Data
import android.util.Log
import android.widget.Toast
import com.example.waldo.Classes.DataCodes
import com.example.waldo.Interfaces.ApiService
import com.example.waldo.Models.Code
import com.example.waldo.Models.LocationData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CodeRepository(private val apiService: ApiService, private val context: Context) {
    private fun getToken(): String? {
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", null)
    }
    private fun showMessageDialog(message: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context)
        builder.setTitle("Informacion")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun getLastCode(id: String?, callback: (Code?) -> Unit) {
        val token = getToken()
        if (token == null) {
            Log.e("AuthError", "No se encontró el token JWT en SharedPreferences")
            callback(null)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            apiService.getLastCode(id, "Bearer $token").enqueue(object : Callback<Code> {
                override fun onResponse(call: Call<Code>, response: Response<Code>) {
                    if (response.isSuccessful) {
                        val code = response.body()
                        if (code != null) {
                            DataCodes.instance.addDataLocation(code)
                            showMessageDialog("Conectado exitosamente ${response.code()}")
                        }
                        callback(code) // Llama al callback con el código obtenido
                    } else {
                        showMessageDialog("Codigo expirado o no disponible ${response.code()}")
                        callback(null) // Llama al callback con null si hay error
                    }
                }

                override fun onFailure(call: Call<Code>, t: Throwable) {
                    Log.e("Error", "Error en la solicitud de código de vinculación", t)
                    callback(null) // Llama al callback con null si falla la solicitud
                }
            })
        }
    }
}