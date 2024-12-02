package com.example.waldo.Repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.waldo.Interfaces.ApiService
import com.example.waldo.Models.HistoryKid
import com.example.waldo.Models.User
import com.example.waldo.R
import com.example.waldo.ui.HistoryAdapter
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryRepository(private val apiService: ApiService, private val context: Context) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter : HistoryAdapter

    private fun getToken(): String? {
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", null)
    }
    fun getHistoryParent(activity: Activity) {
        val token = getToken()

        if (token == null) {
            Log.e("History Repository", "No se encontró el token")
            return
        }

        apiService.getHistoryKids(FirebaseAuth.getInstance().currentUser?.uid.toString(),"Bearer $token").enqueue(object : Callback<List<HistoryKid>> {
            override fun onResponse(call: Call<List<HistoryKid>>, response: Response<List<HistoryKid>>) {
                if (response.isSuccessful) {
                    val kids = response.body()
                    Log.d("History Repository", "Niños obtenidos del servidor: ${kids?.size}")
                    historyAdapter = HistoryAdapter(kids!!)
                    recyclerView = activity.findViewById(R.id.historyRecyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(activity)
                    recyclerView.adapter = historyAdapter
                } else {
                    Log.e("History Repository", "Error al obtener niños vinculados: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<HistoryKid>>, t: Throwable) {
                Log.e("History Repository", "Error de conexión", t)
            }
        })
    }
}