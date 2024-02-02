package com.example.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.InputStream
import java.net.URL
import java.time.LocalDate

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    routing {
        get("/") {
            data class Speech(
                val speaker: String,
                val topic: String,
                val date: LocalDate,
                val words: String
            )

            fun readCsv(inputStream: InputStream): List<Speech> {
                val reader = inputStream.bufferedReader()
                reader.readLine()
                return reader.lineSequence()
                    .filter { it.isNotBlank() }
                    .map {
                        val (speaker, topic, date, words) = it.split(';', limit = 4)
                        Speech(
                            speaker.trim(),
                            topic.trim(),
                            LocalDate.parse(date.trim()),
                            words.trim().removeSurrounding("\"")
                        )
                    }.toList()
            }

            val speeches = mutableListOf<Speech>()
            val urlParams = mutableListOf<String>()

            call.request.queryParameters.forEach { key, values ->
                if (key.startsWith("url")) {
                    urlParams.addAll(values)
                }
            }

            if (urlParams.isNotEmpty()) {
                urlParams.forEach { urlString ->
                    try {
                        URL(urlString).openStream().use { inputStream ->
                            speeches.addAll(readCsv(inputStream))
                        }
                    } catch (e: Exception) {
                        call.respond(HttpStatusCode.InternalServerError, "Error processing URL: $urlString")
                        return@forEach
                    }
                }
                call.respond(HttpStatusCode.OK, speeches)
            } else {
                call.respond(HttpStatusCode.OK, emptyList<Speech>())
            }
        }
    }
}
