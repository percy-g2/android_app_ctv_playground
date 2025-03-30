package com.androdevlinux.ctvplayground.utils

import org.bitcoinj.crypto.ECKey
import java.security.MessageDigest
import java.security.SecureRandom

object CTVUtils {
    fun hash2curve(data: ByteArray): ECKey {
        var hashed = sha256(data)
        var key: ECKey? = null

        while (key == null) {
            try {
                key = ECKey.fromPrivate(hashed)
            } catch (e: Exception) {
                hashed = sha256(hashed)
            }
        }

        return key
    }

    fun sha256(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(data)
    }

    fun generateRandomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        SecureRandom().nextBytes(bytes)
        return bytes
    }
}