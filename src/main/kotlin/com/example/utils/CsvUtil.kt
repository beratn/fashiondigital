package com.example.utils

import com.example.models.SpeechModel
import java.io.InputStream
import java.time.LocalDate

class CsvUtil {
    fun readCsv(inputStream: InputStream): List<SpeechModel> {
        val reader = inputStream.bufferedReader()
        reader.readLine()
        return reader.lineSequence()
            .filter { it.isNotBlank() }
            .map {
                val (speaker, topic, date, words) = it.split(';', limit = 4)
                SpeechModel(
                    speaker.trim(),
                    topic.trim(),
                    LocalDate.parse(date.trim()),
                    words.toString().trim().removeSurrounding("\"").toInt()
                )
            }.toList()
    }
}