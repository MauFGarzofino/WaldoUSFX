package com.example.waldo.Interfaces

import com.example.waldo.DTO.ConnectionStatusDto
import com.example.waldo.DTO.CreateEnrollmentDTO
import com.example.waldo.Models.Code
import com.example.waldo.Models.Enrollment
import com.example.waldo.Models.EnrollmentKid
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

    @GET("users/{id}")
    fun getUserById(
        @Path("id") id: String,
        @Header("Authorization") authHeader: String
    ): Call<User>

    // Obtener el estado de conexión más reciente de un niño por su userId
    @GET("connection-status/{userId}/latest")
    fun getLatestConnectionStatus(
        @Path("userId") userId: String,
        @Header("Authorization") authHeader: String
    ): Call<ConnectionStatusDto>

    // Obtener el historial completo de conexión de un niño por su userId
    @GET("connection-status/{userId}")
    fun getConnectionStatusHistory(
        @Path("userId") userId: String,
        @Header("Authorization") authHeader: String
    ): Call<List<ConnectionStatusDto>>

    // Método para obtener todos los niños vinculados al padre
    @GET("enrollments-Kids")
    fun getEnrollmentsKids(
        @Path("userId") userId: String,
        @Header("Authorization") authHeader: String
    ): Observable<List<EnrollmentKid>>

    @GET("enrollments/linked/kids")
    fun getOnceEnrolledKids(
        @Header("Authorization") authHeader: String
    ): Call<List<User>>
}
