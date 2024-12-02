package com.example.waldo.Models

import java.util.Date

data class HistoryKid(
    val id: Int,
    val id_Parent: String,
    val id_Kid: String,
    val isActive: String,
    val created_at: Date,
    val familyName: String,
    val givenName: String,
    val photo: String,
)
