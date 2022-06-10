package com.jeketos.plugins

import com.jeketos.data.*
import com.jeketos.storage.RoomStorage
import com.jeketos.storage.UserStorage
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.util.UUID

fun Application.configureRouting() {

    routing {
        get("api/v1/join") {
            val uid = UUID.randomUUID().toString()
            val user = User(uid = uid)
            UserStorage.addUser(user)
            call.respond(user)
        }

        get("api/v1/user/{uid?}") {
            val uid: String = call.parameters["uid"] ?: return@get call.respond(
                message = Error.NoUid,
                status = HttpStatusCode.BadRequest
            )

            UserStorage.getUser(uid)?.let {
                call.respond(it)
            } ?: call.respond(message = Error.UserNotFound)
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
                call.respond(room)
            }
        }


        route("api/v1/findGame") {
            get("{uid?}") {
                val uid: String = call.parameters["uid"] ?: return@get call.respond(
                    message = Error.NoUid,
                    status = HttpStatusCode.BadRequest
                )
                val user = UserStorage.getUser(uid)
                if (user != null) {
                    val room = RoomStorage.getRoomByUserUid(uid) ?: Room(
                        uid = UUID.randomUUID().toString(),
                        players = Players(eagle = user),
                        gameState = GameState(
                            turn = user.uid,
                            movesCount = 0,
                            board = "Cg8BngkvAr4DTwleAe8KfgCMAJ0ArAC9AMwA3QDsAP0ACwAaACsAOgBLAFoAawB6AAgAGQAoADkASABZAGgAeQAHABYAJwA2AEcAVgBnAHYABAAVACQANQBEAFUAZAB1BIMEkgSjBLIEwwTSBOME8g4ABZENIAaxB0ANUQXgDnE=",
                            state = State.Idle
                        )
                    ).also {
                        RoomStorage.addRoom(it)
                    }

                    if (room.players.tail == null && room.players.eagle?.uid != user.uid) {
                        room.players.tail = user
                    }
                    call.respond(room)
                } else {
                    call.respond(
                        message = Error.UserNotFound,
                        status = HttpStatusCode.NotFound
                    )
                }
            }
        }

        post("api/v1/makeMove") {
            val move = call.receive<Move>()
            when {
                move.userUid.isEmpty() -> return@post call.respond(message = Error.WrongUserUid, status = HttpStatusCode.PreconditionFailed)
                move.roomUid.isEmpty() -> return@post call.respond(message = Error.WrongRoomUid, status = HttpStatusCode.PreconditionFailed)
                move.board.isEmpty() -> return@post call.respond(message = Error.WrongBoardData, status = HttpStatusCode.PreconditionFailed)
            }
            println("##### roomUid = ${move.roomUid}")
            println("##### room = ${RoomStorage.getRoomByRoomUid(move.roomUid)}")

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
            if (room.players.list().none { it.uid == move.userUid } || room.gameState.turn != move.userUid) {
                return@post call.respond(
                    message = Error.NotYourTurn,
                    status = HttpStatusCode.PreconditionFailed
                )
            }

            val modifiedRoom = room.copy(
                gameState = room.gameState.copy(
                    turn = room.players.other(move.userUid).uid,
                    movesCount = room.gameState.movesCount + 1,
                    board = move.board,
                    state = State.Idle
                )
            )

            RoomStorage.replace(roomIndex, modifiedRoom)
            call.respond(modifiedRoom)
        }

        get("api/v1/giveUp/{uid?}") {
            val uid: String = call.parameters["uid"] ?: return@get call.respond(
                message = Error.NoUid,
                status = HttpStatusCode.BadRequest
            )
            RoomStorage.getRoomByUserUid(uid)?.let {
                val other = it.players.other(uid).uid
                val room = it.copy(
                    gameState = it.gameState.copy(turn = other, state = State.Mate, winner = other)
                )
                RoomStorage.achieve(room)
                call.respond(room)
            } ?: call.respond(
                message = Error.RoomNotFound,
                status = HttpStatusCode.BadRequest
            )
        }
    }
}
