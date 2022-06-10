package com.jeketos.storage

import com.jeketos.data.Room

object RoomStorage {
    private val rooms: MutableList<Room> = mutableListOf()
    private val achieved: MutableList<Room> = mutableListOf()

    fun getRoomByUserUid(userUid: String): Room? {
        val find = rooms.find { it.players.eagle?.uid == userUid || it.players.tail?.uid == userUid }
        return find ?: rooms.firstOrNull { it.players.tail == null }
    }

    fun getRoomByRoomUid(roomUid: String): Room? = rooms.find { it.uid == roomUid }

    fun addRoom(room: Room) {
        rooms.add(room)
    }

    fun getAllRooms(): List<Room> = rooms.toList()

    fun replace(roomIndex: Int, modifiedRoom: Room) {
        rooms[roomIndex] = modifiedRoom
    }

    fun achieve(room: Room) {
        rooms.find { it.uid == room.uid }?.let {
            rooms.remove(it)
            achieved.add(it)
        }
    }
}