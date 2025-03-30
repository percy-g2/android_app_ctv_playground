package com.androdevlinux.ctvplayground.utils

import org.bitcoinj.crypto.ECKey
import java.security.MessageDigest

object BitcoinUtils {
    fun sha256(data: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data)
    }

    fun ripemd160(data: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("RIPEMD160")
        return digest.digest(data)
    }

    fun hash160(data: ByteArray): ByteArray {
        return ripemd160(sha256(data))
    }

    fun hash2curve(data: ByteArray): ECKey {
        var hashed = sha256(data)
        while (true) {
            try {
                return ECKey.fromPrivate(hashed)
            } catch (e: Exception) {
                hashed = sha256(hashed)
            }
        }
    }
}