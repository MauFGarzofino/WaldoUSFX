package com.example.waldo.Models

data class LocationData(
    val id_User: String,
    val latitude: Double,
    val longitude: Double,
    val batteryLevel: Int
) {
    override fun toString(): String {
        return "LocationData(id_User='$id_User', latitude=$latitude, longitude=$longitude, batteryLevel=$batteryLevel)"
    }
}
