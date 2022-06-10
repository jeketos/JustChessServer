package com.jeketos.data

import kotlinx.serialization.Serializable

@Serializable
data class Move(
    val userUid: String,
    val roomUid: String,
    val board: String
)
