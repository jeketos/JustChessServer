package com.jeketos.db.model

import com.jeketos.data.AuthUser
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

object AuthUsers : Table() {
    val uid = varchar("uid", 128)
    val email = text("email")
    val password = text("password")

    override val primaryKey = PrimaryKey(uid)
}

fun AuthUsers.insert(authUser: AuthUser) {
    AuthUsers.insert {
        it[uid] = authUser.uid
        it[email] = authUser.email
        it[password] = authUser.password
    }
}

fun AuthUsers.select(email: String) =
    AuthUsers.select { AuthUsers.email eq email }
        .map { row ->
            AuthUser(
                uid = row[AuthUsers.uid],
                email = row[AuthUsers.email],
                password = row[AuthUsers.password]
            )
        }.singleOrNull()