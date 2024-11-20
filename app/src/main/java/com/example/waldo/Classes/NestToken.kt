package com.example.waldo.Classes

import android.content.Context

class NestToken private constructor(){
    companion object{
        val instance: NestToken by lazy { NestToken() }
    }
    fun getToken(context: Context): String?{
        val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwt_token", null)
    }
}