package com.fashiondigital

import com.fashiondigital.configs.ConfigProperties
import com.fashiondigital.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = ConfigProperties.appPort, host = ConfigProperties.appHost, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureRouting()
}
