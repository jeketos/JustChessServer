package com.jeketos.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uid: String,
    val name: String = uid,
    val games: Int = 0,
    val wins: Int = 0,
    val loses: Int = 0,
    val draws: Int = 0,
    val rating: Int = 1000
)
