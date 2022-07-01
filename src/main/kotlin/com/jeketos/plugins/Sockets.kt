package com.jeketos.plugins

import com.jeketos.socket.SocketController
import com.jeketos.socket.SocketEvents
import com.jeketos.storage.RoomStorage
import com.jeketos.utils.user
import io.ktor.server.application.*
import io.ktor.server.auth.*
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
        authenticate("auth-jwt") {
            webSocket("/room/{roomUid?}") {
                val roomUid: String = call.parameters["roomUid"] ?: return@webSocket close(
                    CloseReason(
                        CloseReason.Codes.NOT_CONSISTENT,
                        "No room uid provided"
                    )
                )

                val user = call.user() ?: return@webSocket close(
                    CloseReason(
                        CloseReason.Codes.NOT_CONSISTENT,
                        "No authorized user"
                    )
                )

                val room = RoomStorage.getRoomByUserUid(user.uid) ?: return@webSocket close(
                    CloseReason(
                        CloseReason.Codes.NOT_CONSISTENT,
                        "No such room or user"
                    )
                )

                val socketRoom = SocketController.getOrCreateRoom(
                    roomUid = roomUid,
                    userUid = user.uid,
                    webSocketSession = this
                )

                println("#### start web socket")

                this.outgoing.send(Frame.Text("room created. Connections - ${socketRoom.connections.size}"))

                if (socketRoom.connections.size == 2) {
                    SocketController.sendEvent(roomUid = roomUid, event = SocketEvents.Start)
                }

                for (frame in this@webSocket.incoming) {
                    println("#### connections - ${socketRoom.connections.size}")
                    frame as? Frame.Text ?: continue
                }
            }
        }

    }
}