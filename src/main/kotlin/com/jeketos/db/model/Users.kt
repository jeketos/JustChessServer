package com.jeketos.db.model

import com.jeketos.data.User
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

object Users : Table() {
    val uid = varchar("uid", 128)
    val name = text("name")
    val games = integer("games")
    val wins = integer("wins")
    val loses = integer("loses")
    val draws = integer("draws")
    val rating = integer("rating")

    override val primaryKey = PrimaryKey(uid)
}

fun Users.insert(user: User) {
    Users.insert {
        it[uid] = user.uid
        it[name] = user.name
        it[games] = user.games
        it[draws] = user.draws
        it[wins] = user.wins
        it[loses] = user.loses
        it[rating] = user.rating
    }
}

fun Users.select(uid: String) =
    Users.select { Users.uid eq uid }
        .map {
            User(
                uid = it[Users.uid],
                name = it[name],
                games = it[games],
                draws = it[draws],
                wins = it[wins],
                loses = it[loses],
                rating = it[rating],
            )
        }
        .singleOrNull()