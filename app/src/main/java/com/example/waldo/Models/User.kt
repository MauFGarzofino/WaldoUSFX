package com.example.waldo.Models

data class User(
    val id: String,
    val familyName: String,
    val givenName: String,
    val photo: String,
    val phone: String,
    val email: String,
    val role: String,
    val token: String? = null
)
