package com.androdevlinux.ctvplayground.hash

import org.bitcoinj.core.Transaction
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

object CTVHashCalculator {
    fun calculateTemplateHash(tx: Transaction, inputIndex: Int): ByteArray {
        val buffer = ByteArrayOutputStream()

        // 1. Version (4 bytes, little-endian)
        buffer.write(intToLE(tx.version.toInt()))

        // 2. Locktime (4 bytes, little-endian)
        buffer.write(intToLE(tx.lockTime.toInt()))

        // 3. Input count (4 bytes, little-endian)
        buffer.write(intToLE(tx.inputs.size))

        // 4. Sequences hash (32 bytes)
        buffer.write(calculateSequencesHash(tx))

        // 5. Output count (4 bytes, little-endian)
        buffer.write(intToLE(tx.outputs.size))

        // 6. Outputs hash (32 bytes)
        buffer.write(calculateOutputsHash(tx))

        // 7. Input index (4 bytes, little-endian)
        buffer.write(intToLE(inputIndex))

        // Double SHA256 as specified in BIP-119
        return sha256(sha256(buffer.toByteArray()))
    }

    private fun calculateSequencesHash(tx: Transaction): ByteArray {
        val buffer = ByteArrayOutputStream()
        tx.inputs.forEach { input ->
            buffer.write(intToLE(input.sequenceNumber.toInt()))
        }
        return sha256(buffer.toByteArray())
    }

    private fun calculateOutputsHash(tx: Transaction): ByteArray {
        val buffer = ByteArrayOutputStream()
        tx.outputs.forEach { output ->
            // Amount (8 bytes, little-endian)
            buffer.write(longToLE(output.value.value))
            // Script
            val script = output.scriptPubKey.program
            buffer.write(writeVarInt(script.size.toLong()))
            buffer.write(script)
        }
        return sha256(buffer.toByteArray())
    }

    private fun writeVarInt(value: Long): ByteArray {
        return when {
            value < 0xfd -> byteArrayOf(value.toByte())
            value <= 0xffff -> byteArrayOf(
                0xfd.toByte(),
                value.toByte(),
                (value ushr 8).toByte()
            )
            value <= 0xffffffffL -> byteArrayOf(
                0xfe.toByte(),
                value.toByte(),
                (value ushr 8).toByte(),
                (value ushr 16).toByte(),
                (value ushr 24).toByte()
            )
            else -> byteArrayOf(
                0xff.toByte(),
                value.toByte(),
                (value ushr 8).toByte(),
                (value ushr 16).toByte(),
                (value ushr 24).toByte(),
                (value ushr 32).toByte(),
                (value ushr 40).toByte(),
                (value ushr 48).toByte(),
                (value ushr 56).toByte()
            )
        }
    }

    private fun intToLE(value: Int): ByteArray {
        return byteArrayOf(
            value.toByte(),
            (value ushr 8).toByte(),
            (value ushr 16).toByte(),
            (value ushr 24).toByte()
        )
    }

    private fun longToLE(value: Long): ByteArray {
        return byteArrayOf(
            value.toByte(),
            (value ushr 8).toByte(),
            (value ushr 16).toByte(),
            (value ushr 24).toByte(),
            (value ushr 32).toByte(),
            (value ushr 40).toByte(),
            (value ushr 48).toByte(),
            (value ushr 56).toByte()
        )
    }

    private fun sha256(data: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data)
    }
}