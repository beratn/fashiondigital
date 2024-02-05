package com.fashiondigital.configs

import java.util.*

object ConfigProperties {
    private val properties: Properties = Properties()

    init {
        val inputStream = this::class.java.classLoader.getResourceAsStream("config.properties")
        properties.load(inputStream)
    }

    val appHost: String by lazy { properties.getProperty("app.host", "localhost") }
    val appPort: Int by lazy { properties.getProperty("app.port", "8080").toInt() }
    val desiredSpeechTopic: String by lazy { properties.getProperty("desiredSpeechTopic", "homeland security") }
    val desiredMostSpeechYear: String by lazy { properties.getProperty("desiredMostSpeechYear", "2022") }
    val cacheResults: Boolean by lazy { properties.getProperty("cacheResults", "false").toBoolean() }
    val redisHost: String by lazy { properties.getProperty("redis.host", "localhost") }
    val redisPort: Int by lazy { properties.getProperty("redis.port", "6379").toInt() }
    val redisPassword: String? by lazy { properties.getProperty("redis.password") }
}