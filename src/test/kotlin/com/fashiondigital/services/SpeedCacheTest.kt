package com.fashiondigital.services

import com.google.gson.Gson
import junit.framework.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever

class SpeechCacheTest {
    private lateinit var speechCache: SpeechCache
    private val redisClient: RedisClient = mock()
    private val gson: Gson = mock()

    @BeforeEach
    fun setUp() {
        speechCache = SpeechCache(redisClient)
    }

    @Test
    fun `getCachedSpeechModels should return null when data does not exist`() {
        val urlHash = "nonExistingUrlHash"
        whenever(redisClient.use<Any>(anyOrNull())).thenReturn(null)

        val result = speechCache.getCachedSpeechModels(urlHash)

        assertEquals(null, result)
    }
}