package com.fashiondigital.services

import com.fashiondigital.configs.ConfigProperties
import com.fashiondigital.models.SpeechModel
import com.fashiondigital.utils.CsvUtil
import com.fashiondigital.utils.HashUtil
import java.net.URL
import java.util.Locale

class SpeechProcessor {
    private val csvUtil: CsvUtil
    private val speechCache: SpeechCache
    private val hashUtil: HashUtil

    constructor(csvUtil: CsvUtil, speechCache: SpeechCache, hashUtil: HashUtil) {
        this.speechCache = speechCache
        this.hashUtil = hashUtil
        this.csvUtil = csvUtil
    }

    fun process(urls: List<String>): Map<String, String?> {
        val speechesByUrl = mutableListOf<SpeechModel>()
        val newlyFetchedUrls = mutableListOf<String>()

        urls.forEach { url ->
            val cachedResult = speechCache.getCachedSpeechModels(hashUtil.hashUrl(url))

            if (cachedResult != null && cachedResult.orEmpty().isNotEmpty()) {
                speechesByUrl.addAll(cachedResult)
            } else {
                newlyFetchedUrls.add(url)
            }
        }

        if (newlyFetchedUrls.isNotEmpty()) {
            newlyFetchedUrls.forEach { url ->
                val modelResults = fetchSpeech(url) ?: return@forEach
                if (modelResults.isNotEmpty()) {
                    speechesByUrl.addAll(modelResults)
                    speechCache.cacheSpeechModels(hashUtil.hashUrl(url), modelResults)
                }
            }
        }

        if (speechesByUrl.isEmpty()) {
            return emptyMap<String, String>()
        }

        val mostSpeeches = calculateMostSpeeches(speechesByUrl)
        val mostSecuritySpeeches = calculateMostSecuritySpeeches(speechesByUrl)
        val leastWordy = calculateLeastWordy(speechesByUrl)

        return mapOf(
            "mostSpeeches" to mostSpeeches,
            "mostSecurity" to mostSecuritySpeeches,
            "leastWordy" to leastWordy
        )
    }

    fun fetchSpeech(urlString: String): List<SpeechModel>? {
        return try {
            URL(urlString).openStream().use { inputStream ->
                csvUtil.readCsv(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateMostSpeeches(speeches: List<SpeechModel>): String? {
        val desiredMostSpeechYear = ConfigProperties.desiredMostSpeechYear.lowercase(Locale.getDefault())
        return speeches
            .filter { it.date.year.toString() == desiredMostSpeechYear }
            .groupingBy { it.speaker }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
    }

    private fun calculateMostSecuritySpeeches(speeches: List<SpeechModel>): String? {
        val desiredSpeechTopic = ConfigProperties.desiredSpeechTopic.lowercase(Locale.getDefault())
        return speeches.filter { it.topic.lowercase(Locale.getDefault()) == desiredSpeechTopic }
            .groupingBy { it.speaker }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
    }

    private fun calculateLeastWordy(speeches: List<SpeechModel>): String? {
        return speeches.groupBy { it.speaker }
            .mapValues { (_, speeches) -> speeches.sumOf { it.words } }
            .minByOrNull { it.value }
            ?.key
    }
}