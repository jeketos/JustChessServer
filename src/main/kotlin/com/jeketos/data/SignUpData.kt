package com.jeketos.data

import kotlinx.serialization.Serializable

@Serializable
data class SignUpData(
    val email: String,
    val password: String,
    val name: String
)
