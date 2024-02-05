package com.fashiondigital.services

import com.fashiondigital.models.SpeechModel
import com.fashiondigital.utils.CsvUtil
import com.fashiondigital.utils.HashUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SpeechProcessorTest {
    private lateinit var speechProcessor: SpeechProcessor
    private val csvUtil: CsvUtil = mock()
    private val speechCache: SpeechCache = mock()
    private val hashUtil: HashUtil = mock()

    @BeforeEach
    fun setUp() {
        speechProcessor = SpeechProcessor(csvUtil, speechCache, hashUtil)
    }

    @Test
    fun `process should return correct results for cached and fetched speeches`() {
        val urls = listOf("https://google.com", "https://amazon.com")
        val hashUrl1 = "hash1"
        val hashUrl2 = "hash2"
        val speech1 = SpeechModel("Speaker1", "homeland security", LocalDate.of(2022, 1, 1), 100)
        val speech2 = SpeechModel("Speaker2", "Health", LocalDate.of(2022, 1, 1), 200)
        val speech3 = SpeechModel("Speaker1", "Health", LocalDate.of(2022, 1, 1), 200)

        whenever(hashUtil.hashUrl(urls[0])).thenReturn(hashUrl1)
        whenever(hashUtil.hashUrl(urls[1])).thenReturn(hashUrl2)
        whenever(speechCache.getCachedSpeechModels(hashUrl1)).thenReturn(listOf(speech2, speech1, speech3))
        whenever(speechCache.getCachedSpeechModels(hashUrl2)).thenReturn(null)
        whenever(csvUtil.readCsv(anyOrNull())).thenReturn(listOf(speech2, speech1, speech3))

        val result = speechProcessor.process(urls)

        assertNotNull(result["mostSpeeches"])
        assertEquals("Speaker1", result["mostSpeeches"])
        assertEquals("Speaker1", result["mostSecurity"])
        assertEquals("Speaker2", result["leastWordy"])

        verify(speechCache, times(0)).cacheSpeechModels(hashUrl2, listOf(speech2))
    }

    @Test
    fun `process should handle empty url list`() {
        val urls = emptyList<String>()

        val result = speechProcessor.process(urls)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `process should handle fetch errors gracefully`() {
        val urls = listOf("urlForExceptionHandling")
        val hashUrl = "hashForExceptionHandling"

        whenever(hashUtil.hashUrl(anyOrNull())).thenReturn(hashUrl)
        whenever(csvUtil.readCsv(anyOrNull())).thenThrow(RuntimeException("Intentional testing exception"))

        val result = speechProcessor.process(urls)

        assertTrue(result.isEmpty(), "Result should be empty when an exception occurs in fetchSpeech")
    }

    @Test
    fun `process should correctly identify speaker with most speeches in a specific year`() {
        val urls = listOf("https://google.com")
        val hashUrl = "hashYearSpecific"
        val speeches = listOf(
            SpeechModel("SpeakerA", "TopicA", LocalDate.of(2022, 1, 1), 120),
            SpeechModel("SpeakerA", "TopicB", LocalDate.of(2022, 1, 1), 150),
            SpeechModel("SpeakerB", "TopicC", LocalDate.of(2022, 1, 1), 100)
        )

        whenever(hashUtil.hashUrl(urls[0])).thenReturn(hashUrl)
        whenever(csvUtil.readCsv(anyOrNull())).thenReturn(speeches)

        val result = speechProcessor.process(urls)

        assertEquals("SpeakerA", result["mostSpeeches"], "SpeakerA should be identified as the speaker with the most speeches in 2022")
    }

    @Test
    fun `process should correctly identify speaker with most security speeches`() {
        val urls = listOf("http://google.com")
        val hashUrl = "hashSecurityTopic"
        val speeches = listOf(
            SpeechModel("SpeakerX", "homeland security", LocalDate.of(2022, 1, 1), 100),
            SpeechModel("SpeakerX", "homeland security", LocalDate.of(2022, 1, 1), 150),
            SpeechModel("SpeakerY", "Health", LocalDate.of(2022, 1, 1), 300)
        )

        whenever(hashUtil.hashUrl(urls[0])).thenReturn(hashUrl)
        whenever(csvUtil.readCsv(anyOrNull())).thenReturn(speeches)

        val result = speechProcessor.process(urls)

        assertEquals("SpeakerX", result["mostSecurity"], "SpeakerX should be identified as the speaker with the most security speeches")
    }

    @Test
    fun `process should correctly identify the least wordy speaker`() {
        val urls = listOf("https://google.com")
        val hashUrl = "hashWordCount"
        val speeches = listOf(
            SpeechModel("Speaker1", "Topic1", LocalDate.of(2022, 1, 1), 500),
            SpeechModel("Speaker2", "Topic2", LocalDate.of(2022, 1, 1), 300),
            SpeechModel("Speaker1", "Topic3", LocalDate.of(2022, 1, 1), 700)
        )

        whenever(hashUtil.hashUrl(urls[0])).thenReturn(hashUrl)
        whenever(speechCache.getCachedSpeechModels(eq(hashUrl))).thenReturn(null)
        whenever(csvUtil.readCsv(anyOrNull())).thenReturn(speeches)

        val result = speechProcessor.process(urls)

        assertEquals("Speaker2", result["leastWordy"], "Speaker2 should be identified as the least wordy speaker")
        verify(speechCache, times(1)).cacheSpeechModels(hashUrl, speeches)
    }

    @Test
    fun `caching behavior should ensure speeches are cached only once`() {
        val url = "https://google.com"
        val hashUrl = "uniqueHashForCachingTest"
        val speeches = listOf(SpeechModel("SpeakerForCaching", "Topic", LocalDate.of(2022, 1, 1), 200))

        whenever(hashUtil.hashUrl(url)).thenReturn(hashUrl)
        whenever(csvUtil.readCsv(anyOrNull())).thenReturn(speeches)

        speechProcessor.process(listOf(url))

        verify(speechCache, times(1)).cacheSpeechModels(hashUrl, speeches)
    }

    @Test
    fun `should use cache when available and not fetch speeches again`() {
        val url = "https://google.com"
        val hashUrl = "hashForCacheUsageTest"
        val speeches = listOf(SpeechModel("CachedSpeaker", "CachedTopic", LocalDate.of(2022, 1, 1), 150))

        whenever(hashUtil.hashUrl(url)).thenReturn(hashUrl)
        whenever(speechCache.getCachedSpeechModels(hashUrl)).thenReturn(speeches)

        speechProcessor.process(listOf(url))

        verify(speechCache, times(0)).cacheSpeechModels(hashUrl, speeches)
    }

    @Test
    fun `fetchSpeech should handle and log exceptions without crashing`() {
        val url = "urlForExceptionHandling"
        val hashUrl = "hashForExceptionHandling"

        whenever(hashUtil.hashUrl(url)).thenReturn(hashUrl)
        whenever(csvUtil.readCsv(anyOrNull())).thenThrow(RuntimeException("Intentional testing exception"))

        val result = speechProcessor.process(listOf(url))

        assertTrue(result.isEmpty(), "Result should be empty when an exception occurs in fetchSpeech")
    }
}