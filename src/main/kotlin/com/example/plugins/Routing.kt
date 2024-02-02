package com.example.plugins

import com.example.models.SpeechModel
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.net.URL

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    routing {
        get("/") {

            val speechesByUrl = call.request.queryParameters.entries()
                .filter { it.key.startsWith("url") }
                .flatMap { it.value }
                .mapNotNull { urlString ->
                    try {
                        URL(urlString).openStream().use { inputStream ->
                            readCsv(inputStream)
                        }
                    } catch (e: Exception) {
                        null
                    }
                }.flatten()

            val mostSpeeches = speechesByUrl.groupingBy { it.speaker }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key

            val mostSecuritySpeeches = speechesByUrl.filter { it.topic.lowercase() == "homeland security" }
                .groupingBy { it.speaker }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key

            val leastWordy = speechesByUrl.groupBy { it.speaker }
                .mapValues { (_, speeches) -> speeches.sumOf { it.words } }
                .minByOrNull { it.value }
                ?.key

            call.respond(HttpStatusCode.OK, mapOf(
                "mostSpeeches" to mostSpeeches,
                "mostSecurity" to mostSecuritySpeeches,
                "leastWordy" to leastWordy
            ))
        }
    }
}
