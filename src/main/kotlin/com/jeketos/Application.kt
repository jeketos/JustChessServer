package com.jeketos

import com.jeketos.db.DatabaseFactory
import com.jeketos.db.dao.DAOFacadeImpl
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.jeketos.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        DatabaseFactory.init()
        val dao = DAOFacadeImpl()
        configureSockets()
        configureRouting(dao)
        configureSerialization()
        configureMonitoring()
        configureAuth(dao)
    }.start(wait = true)
}
