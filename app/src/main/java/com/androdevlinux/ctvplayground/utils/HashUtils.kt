package com.androdevlinux.ctvplayground.utils

import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.script.Script
import java.security.MessageDigest

object HashUtils {
    fun sha256hash160(data: ByteArray): ByteArray {
        val sha256 = MessageDigest.getInstance("SHA-256")
        val ripemd160 = MessageDigest.getInstance("RIPEMD160")

        val sha256Hash = sha256.digest(data)
        return ripemd160.digest(sha256Hash)
    }

    fun calculateScriptHash(script: Script): ByteArray {
        val ripemd160 = MessageDigest.getInstance("RIPEMD160")
        return ripemd160.digest(Sha256Hash.hash(script.program()))
    }
}