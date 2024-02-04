package com.example.plugins

import com.example.services.SpeechProcessor
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    routing {
        get("/") {
            val urls = call.request.queryParameters.entries()
                .filter { it.key.startsWith("url") }
                .flatMap { it.value }
                .toList()

            val uniqueUrls = urls.distinct()
            if (uniqueUrls.size != urls.size) {
                //throw IllegalArgumentException("The same URL cannot be used more than once.")
            }
            val speechProcessor = SpeechProcessor()
            val result = speechProcessor.process(urls)
            call.respond(HttpStatusCode.OK, result)
        }
    }
}
