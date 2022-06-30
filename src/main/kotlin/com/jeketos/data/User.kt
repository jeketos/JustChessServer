package com.jeketos.data

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class User(
    val uid: String = UUID.randomUUID().toString(),
    val name: String = uid,
    val games: Int = 0,
    val wins: Int = 0,
    val loses: Int = 0,
    val draws: Int = 0,
    val rating: Int = 1000
)

@Serializable
data class AuthUser(
    val uid: String,
    val email: String,
    val password: String
)

@Serializable
data class Credentials(
    val email: String,
    val password: String
)
