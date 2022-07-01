package com.jeketos.data

import io.ktor.server.auth.*

data class UserPrincipal(val user: User): Principal