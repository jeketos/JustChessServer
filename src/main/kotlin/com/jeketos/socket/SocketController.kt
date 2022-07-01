package com.jeketos.socket

import io.ktor.server.websocket.*
import io.ktor.websocket.*

object SocketController {

    val  rooms: MutableList<SocketRoom> = mutableListOf()

    fun getOrCreateRoom(
        roomUid: String,
        userUid: String,
        webSocketSession: DefaultWebSocketServerSession
    ): SocketRoom {
        return rooms.find { it.roomUid == roomUid }?.let {
            if (it.eagleConnection == null) {
                val socketRoom = it.copy(
                    eagleConnection = SocketConnection(
                        userUid = userUid,
                        session = webSocketSession
                    )
                )
                rooms[rooms.indexOf(it)] = socketRoom
                socketRoom
            } else it
        } ?: run {
            val socketRoom = SocketRoom(
                roomUid = roomUid,
                tailConnection = SocketConnection(
                    userUid = userUid,
                    session = webSocketSession
                ),
                eagleConnection = null
            )
            rooms.add(socketRoom)
            socketRoom
        }
    }

    suspend fun sendEvent(
        roomUid: String,
        event: SocketEvents
    ) {
        rooms.find { it.roomUid == roomUid }?.apply {
            connections.forEach {
                runCatching {
                    it.session.outgoing.send(Frame.Text(event.id))
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

    suspend fun closeRoom(roomUid: String) {
        val room = rooms.firstOrNull { it.roomUid == roomUid }
        room?.connections?.forEach {
            it.session.close()
        }
        rooms.remove(room)
    }
}

enum class SocketEvents(val id: String) {
    Start("Start"),
    Update("Update"),
    Close("Close")
}