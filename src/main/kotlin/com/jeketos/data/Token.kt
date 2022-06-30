package com.jeketos.data

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val token: String
)

@Serializable
data class TokenWithUser(
    val token: String,
    val user: User
)
