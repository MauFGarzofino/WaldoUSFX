package com.example.waldo.Interfaces

import com.example.waldo.Models.Code
import com.example.waldo.Models.LocationData
import com.example.waldo.Models.User
import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("users")
    fun createUser(@Body user: User): Call<User>

    @GET("users/code/{id}")
    fun getCodeById(
        @Path("id") id: String?,
        @Header("Authorization") authHeader: String
    ): Call<Code>

    @GET("data-locations/{id}")
    fun getLocationById(
        @Path("id") id: String,
        @Header("Authorization") authHeader: String
    ): Observable<LocationData>
}
