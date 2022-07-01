package com.jeketos.data

import kotlinx.serialization.Serializable

@Serializable
data class Move(
    val roomUid: String,
    val board: String
)
