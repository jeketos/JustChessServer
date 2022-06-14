package com.jeketos.plugins

import com.jeketos.socket.SocketController
import com.jeketos.storage.RoomStorage
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
//        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/room/{roomUid?}/user/{userUid?}") { // websocketSession
            val roomUid: String = call.parameters["roomUid"] ?: return@webSocket close(
                CloseReason(
                    CloseReason.Codes.NOT_CONSISTENT,
                    "No room uid provided"
                )
            )

            val userUid: String = call.parameters["userUid"] ?: return@webSocket close(
                CloseReason(
                    CloseReason.Codes.NOT_CONSISTENT,
                    "No user uid provided"
                )
            )

            val room = RoomStorage.getRoomByUserUid(userUid) ?: return@webSocket close(
                CloseReason(
                    CloseReason.Codes.NOT_CONSISTENT,
                    "No such room or user"
                )
            )

            val socketRoom = SocketController.getOrCreateRoom(
                roomUid = roomUid,
                userUid = userUid,
                webSocketSession = this
            )

            this.outgoing.send(Frame.Text("room created. Connections - ${socketRoom.connections.size}"))
            for (frame in this@webSocket.incoming) {
                frame as? Frame.Text ?: continue
            }
        }
    }
}