package com.jeketos.utils

import com.jeketos.data.User
import com.jeketos.data.UserPrincipal
import io.ktor.server.application.*
import io.ktor.server.auth.*

fun ApplicationCall.user(): User? = this.principal<UserPrincipal>()?.user