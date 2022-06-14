package com.jeketos.socket

import io.ktor.server.websocket.*

data class SocketRoom(
    val roomUid: String,
    val tailConnection: SocketConnection?,
    val eagleConnection: SocketConnection?
) {
    val connections: List<SocketConnection> = listOfNotNull(tailConnection, eagleConnection)

    fun thisConnection(userUid: String) = connections.firstOrNull { it.userUid == userUid }

    fun otherConnection(userUid: String) = connections.firstOrNull { it.userUid != userUid }
}

data class SocketConnection(
    val userUid: String,
    val session: DefaultWebSocketServerSession
)
