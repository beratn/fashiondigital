package com.fashiondigital.utils

import com.fashiondigital.models.SpeechModel
import junit.framework.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import java.time.LocalDate

class CsvUtilTest {
    private val csvUtil = CsvUtil()

    @Test
    fun `readCsv should correctly parse input stream into list of SpeechModel`() {
        val csvContent = """
            Speaker;Topic;Date;Words
            John Doe;Health;2022-01-01;100
            Jane Doe;Technology;2022-02-01;200
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csvContent.toByteArray())
        val result = csvUtil.readCsv(inputStream)

        val expected = listOf(
            SpeechModel("John Doe", "Health", LocalDate.of(2022, 1, 1), 100),
            SpeechModel("Jane Doe", "Technology", LocalDate.of(2022, 2, 1), 200)
        )

        assertEquals(expected.size, result.size)
        expected.zip(result).forEach { (expectedItem, resultItem) ->
            assertSameSpeechModel(expectedItem, resultItem)
        }
    }

    @Test
    fun `readCsv should handle incorrect format gracefully`() {
        val csvContent = """
            Speaker;Topic;Date;Words
            John Doe;Health
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csvContent.toByteArray())
        assertThrows<Exception> {
            csvUtil.readCsv(inputStream)
        }
    }

    private fun assertSameSpeechModel(expected: SpeechModel, actual: SpeechModel) {
        assertEquals(expected.speaker, actual.speaker)
        assertEquals(expected.topic, actual.topic)
        assertEquals(expected.date, actual.date)
        assertEquals(expected.words, actual.words)
    }
}