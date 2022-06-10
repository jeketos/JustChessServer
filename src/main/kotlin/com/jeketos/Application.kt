package com.jeketos

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.jeketos.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureSockets()
        configureRouting()
        configureSerialization()
        configureMonitoring()
    }.start(wait = true)
}
