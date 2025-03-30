package com.androdevlinux.ctvplayground.vault

import java.security.MessageDigest

object VaultUtils {
    fun sha256(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(data)
    }

    fun ripemd160(data: ByteArray): ByteArray {
        return MessageDigest.getInstance("RIPEMD160").digest(data)
    }

    fun sha256hash160(data: ByteArray): ByteArray {
        return ripemd160(sha256(data))
    }
}