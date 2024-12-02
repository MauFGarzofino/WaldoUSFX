package com.example.waldo.Models

data class KidDisplayModel(
    val id_Enrollment: Int,
    val id_User: String,
    val name: String,
    var connectionStatus: String,
    val photo: String,
    var lastUpdated: Long
)
