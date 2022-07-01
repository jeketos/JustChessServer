package com.jeketos.data

import kotlinx.serialization.Serializable

@Serializable
data class Error(
    val message: String
) {
    companion object {
        val NoUid = Error("User not found.")
        val WrongUserUid = Error("Wrong user uid.")
        val WrongRoomUid = Error("Wrong room uid.")
        val WrongBoardData = Error("Wrong board data.")
        val UserNotFound = Error("User not found.")
        val RoomNotFound = Error("Room not found.")
        val NotYourTurn = Error("It`s not your turn or wrong user uid.")
        val NotEnoughPlayers = Error("Not enough players in the room.")
    }
}
