package com.example.waldo.DTO

data class ConnectionStatusDto(
    val userId: String,
    val connectionStatus: String, // Ej.: "No network available", "No internet access", "Internet is accessible"
    val lastChecked: String // Fecha y hora del último estado
)
