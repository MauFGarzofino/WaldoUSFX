package com.example.waldo.Repository

import android.content.Context

open class BaseRepository(private val context: Context) {

    protected fun getToken(): String? {
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", null)
    }
}
