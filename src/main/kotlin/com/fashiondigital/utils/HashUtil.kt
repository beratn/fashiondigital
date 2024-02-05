package com.fashiondigital.utils

import java.security.MessageDigest

open class HashUtil {
    open fun hashUrl(url: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashBytes = messageDigest.digest(url.toByteArray(Charsets.UTF_8))
        return bytesToHex(hashBytes)
    }

    fun bytesToHex(hash: ByteArray): String {
        val hexString = StringBuilder(2 * hash.size)
        for (b in hash) {
            val hex = Integer.toHexString(0xff and b.toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }
}