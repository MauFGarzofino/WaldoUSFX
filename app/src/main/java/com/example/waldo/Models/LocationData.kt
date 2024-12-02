package com.example.waldo.Models

import java.util.Date

data class LocationData(
    val id_User: String,
    val latitude: Double,
    val longitude: Double,
    val batteryLevel: Int,
    val created_at : Date
) {
    override fun toString(): String {
        return "LocationData(id_User='$id_User', latitude=$latitude, longitude=$longitude, batteryLevel=$batteryLevel)"
    }
}
