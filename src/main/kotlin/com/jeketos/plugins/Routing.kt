package com.jeketos.plugins

import com.jeketos.Constants
import com.jeketos.data.*
import com.jeketos.db.dao.DAOFacade
import com.jeketos.socket.SocketController
import com.jeketos.socket.SocketEvents
import com.jeketos.storage.RoomStorage
import com.jeketos.utils.user
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.websocket.*
import java.util.*

fun Application.configureRouting(dao: DAOFacade) {
    routing {
        authenticate("auth-jwt") {
            get("api/v1/user") {
                val user = call.user() ?: return@get call.respond(
                    message = Error.UserNotFound,
                    status = HttpStatusCode.NotFound
                )

                call.respond(user)
            }

            route("api/v1/rooms") {
                get {
                    call.respond(RoomStorage.getAllRooms())
                }
                get("{uid?}") {
                    val uid: String = call.parameters["uid"] ?: return@get call.respond(
                        message = Error.NoUid,
                        status = HttpStatusCode.BadRequest
                    )
                    val room = RoomStorage.getRoomByRoomUid(uid) ?: return@get call.respond(
                        message = Error.RoomNotFound,
                        status = HttpStatusCode.NotFound
                    )

                    if (room.gameState.winner != null) {
                        RoomStorage.achieve(room)
                        SocketController.sendEvent(roomUid = room.uid, event = SocketEvents.Close)
                        SocketController.closeRoom(room.uid)
                    }
                    call.respond(room)
                }
                get("achieved/{uid}") {
                    val uid: String = call.parameters["uid"] ?: return@get call.respond(
                        message = Error.NoUid,
                        status = HttpStatusCode.BadRequest
                    )
                    val room = RoomStorage.getAchievedRoom(uid) ?: return@get call.respond(
                        message = Error.RoomNotFound,
                        status = HttpStatusCode.NotFound
                    )
                    call.respond(room)
                }
            }

            get("api/v1/findGame") {
                val user = call.user() ?: return@get call.respond(
                    message = Error.UserNotFound,
                    status = HttpStatusCode.NotFound
                )
                val room = RoomStorage.getRoomByUserUid(user.uid) ?: Room(
                    uid = UUID.randomUUID().toString(),
                    players = Players(eagle = user),
                    gameState = GameState(
                        turn = user.uid,
                        movesCount = 0,
                        board = Constants.emptyBoard,
                        state = State.Idle
                    )
                ).also {
                    RoomStorage.addRoom(it)
                }
                if (room.players.tail == null && room.players.eagle?.uid != user.uid) {
                    room.players.tail = user
                }
                call.respond(room)

            }

            post("api/v1/makeMove") {
                val user = call.user() ?: return@post call.respond(
                    message = Error.UserNotFound,
                    status = HttpStatusCode.NotFound
                )
                val move = call.receive<Move>()
                when {
                    move.roomUid.isEmpty() -> return@post call.respond(
                        message = Error.WrongRoomUid,
                        status = HttpStatusCode.PreconditionFailed
                    )
                    move.board.isEmpty() -> return@post call.respond(
                        message = Error.WrongBoardData,
                        status = HttpStatusCode.PreconditionFailed
                    )
                }

                val room = RoomStorage.getRoomByRoomUid(move.roomUid) ?: return@post call.respond(
                    message = Error.RoomNotFound,
                    status = HttpStatusCode.NotFound
                )
                val roomIndex = RoomStorage.getAllRooms().indexOf(room)

                if (room.players.list().size < 2) {
                    return@post call.respond(
                        message = Error.NotEnoughPlayers,
                        status = HttpStatusCode.PreconditionFailed
                    )
                }
                if (room.players.list().none { it.uid == user.uid } || room.gameState.turn != user.uid) {
                    return@post call.respond(
                        message = Error.NotYourTurn,
                        status = HttpStatusCode.PreconditionFailed
                    )
                }

                val modifiedRoom = room.copy(
                    gameState = room.gameState.copy(
                        turn = room.players.other(user.uid).uid,
                        movesCount = room.gameState.movesCount + 1,
                        board = move.board,
                        state = State.Idle
                    )
                )

                RoomStorage.replace(roomIndex, modifiedRoom)
                call.respond(modifiedRoom)
                SocketController.sendEvent(roomUid = modifiedRoom.uid, event = SocketEvents.Update)
            }

            get("api/v1/giveUp") {
                val user = call.user() ?: return@get call.respond(
                    message = Error.UserNotFound,
                    status = HttpStatusCode.NotFound
                )
                RoomStorage.getRoomByUserUid(user.uid)?.let {
                    val other = it.players.other(user.uid).uid
                    val room = it.copy(
                        gameState = it.gameState.copy(turn = other, state = State.Mate, winner = other)
                    )
                    RoomStorage.update(room)
                    call.respond(room)
                    SocketController.sendEvent(roomUid = room.uid, event = SocketEvents.Update)
                } ?: call.respond(
                    message = Error.RoomNotFound,
                    status = HttpStatusCode.BadRequest
                )
            }
        }
    }
}
