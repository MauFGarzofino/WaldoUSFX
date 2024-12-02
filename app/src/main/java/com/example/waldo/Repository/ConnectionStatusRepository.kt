package com.example.waldo.Repository

import android.content.Context
import com.example.waldo.Interfaces.ApiService

class ConnectionStatusRepository(
    private val apiService: ApiService,
    context: Context
) : BaseRepository(context) {

}
