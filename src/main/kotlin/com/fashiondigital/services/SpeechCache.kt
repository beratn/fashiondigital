package com.fashiondigital.services

import com.fashiondigital.configs.ConfigProperties
import com.fashiondigital.models.SpeechModel
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

open class SpeechCache(private val redisClient: RedisClient) {
    private val cacheEnabled = ConfigProperties.cacheResults
    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
        .create()

    fun cacheSpeechModels(urlHash: String, speeches: List<SpeechModel>) {
        if (!cacheEnabled) {
            return
        }

        val jsonString = gson.toJson(speeches)
        redisClient.use { it.set(urlHash, jsonString) }
    }

    open fun getCachedSpeechModels(urlHash: String): List<SpeechModel>? {
        if (!cacheEnabled) {
            return null
        }
        val jsonString = redisClient.use { it.get(urlHash) } ?: return null
        return run {
            val type = object : TypeToken<List<SpeechModel>>() {}.type
            gson.fromJson(jsonString, type)
        }
    }
}