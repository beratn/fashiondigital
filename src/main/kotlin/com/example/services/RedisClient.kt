package com.example.services

import com.example.configs.ConfigProperties
import redis.clients.jedis.Jedis

class RedisClient {
    private val jedis: Jedis by lazy {
        val jedis = Jedis(ConfigProperties.redisHost, ConfigProperties.redisPort)
        if (!ConfigProperties.redisPassword.isNullOrEmpty()) {
            jedis.auth(ConfigProperties.redisPassword)
        }
        jedis
    }

    fun <T> use(block: (Jedis) -> T): T = jedis.use(block)
}