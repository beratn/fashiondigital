package com.fashiondigital.utils

import junit.framework.Assert.assertEquals
import org.junit.jupiter.api.Test

class HashUtilTest {
    private val hashUtil = HashUtil()

    @Test
    fun `hashUrl should produce expected SHA-256 hash for a given URL`() {
        val url = "https://example.com"
        val expectedHash = "100680ad546ce6a577f42f52df33b4cfdca756859e664b8d7de329b150d09ce9"
        val actualHash = hashUtil.hashUrl(url)
        assertEquals("The hash value should match the expected SHA-256 hash.", expectedHash, actualHash)
    }
}