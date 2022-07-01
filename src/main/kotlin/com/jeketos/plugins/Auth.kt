package com.jeketos.plugins

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.jeketos.Constants
import com.jeketos.data.*
import com.jeketos.db.dao.DAOFacade
import com.jeketos.utils.isEmailAddress
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Application.configureAuth(dao: DAOFacade) {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT.require(Algorithm.HMAC256(Constants.secret))
                    .build()
            )
            validate { credential ->
                val user = dao.getUser(credential.payload.getClaim(Constants.uid).asString())
                if (user != null) {
                    UserPrincipal(user)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired.")
            }
        }
    }
    routing {
        post("api/v1/signUp") {
            val data = call.receive<SignUpData>()
            val error = when {
                data.email.isEmailAddress().not() -> "Wrong email address."
                dao.getAuthUserByEmail(data.email) != null -> "This email has already taken."
                data.password.isEmpty() || data.password.length < 6 -> "Password should contain at least 6 symbols."
                data.name.isEmpty() -> "Name should not be empty."
                else -> null
            }
            if (error != null) {
                call.respond(HttpStatusCode.PreconditionFailed, error)
                return@post
            }

            val user = User(name = data.name)
            dao.addUser(user)
            val password = BCrypt.withDefaults().hashToString(10, data.password.toCharArray())
            dao.addAuthUser(
                AuthUser(
                    uid = user.uid,
                    email = data.email,
                    password = password
                )
            )
            call.respond(
                TokenWithUser(
                    token = createToken(user.uid),
                    user = user
                )
            )
        }

        post("api/v1/login") {
            val credentials = call.receive<Credentials>()
            val authUser = dao.getAuthUserByEmail(credentials.email)
                ?: return@post call.respond(HttpStatusCode.NotFound, "User not found.")

            if (BCrypt.verifyer().verify(credentials.password.toCharArray(), authUser.password).verified) {
                val token = createToken(authUser.uid)
                call.respond(
                    TokenWithUser(
                        token = token,
                        user = dao.getUser(authUser.uid) ?: return@post call.respond(
                            HttpStatusCode.NotFound,
                            "User not found."
                        )
                    )
                )
            } else {
                call.respond(HttpStatusCode.Forbidden, "Wrong username or password.")
            }
        }
    }
}

private fun createToken(uid: String): String {
    return JWT.create()
        .withClaim(Constants.uid, uid)
        .withExpiresAt(Date(System.currentTimeMillis() + 86400000))
        .sign(Algorithm.HMAC256(Constants.secret))
}