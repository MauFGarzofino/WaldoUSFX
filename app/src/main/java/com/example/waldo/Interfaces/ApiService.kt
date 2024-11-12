package com.example.waldo.Interfaces

import com.example.waldo.DTO.CreateEnrollmentDTO
import com.example.waldo.Models.Code
import com.example.waldo.Models.Enrollment
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

    @POST("enrollments")
    fun createEnrollment(
        @Body createEnrollmentDTO: CreateEnrollmentDTO,
        @Header("Authorization") authHeader: String
    ): Call<Enrollment>

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

    @GET("codes/code/{id}")
    fun getLastCode(
        @Path("id") id: String?,
        @Header("Authorization") authHeader: String
    ): Call<Code>

}
