package com.androdevlinux.ctvplayground.hash

import org.bitcoinj.core.Transaction
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

interface TemplateHash {
    fun templateHash(inputIndex: Int): Result<ByteArray>
}

object CTVHashCalculator {
    fun calculateTemplateHash(tx: Transaction, inputIndex: Int): ByteArray {
        val buffer = ByteArrayOutputStream()

        // Write version
        buffer.write(intToLE(tx.version.toInt()))

        // Write locktime
        buffer.write(longToLE(tx.lockTime))

        // Calculate and write script sigs if present
        calculateScriptSigs(tx)?.let { buffer.write(it) }

        // Write number of inputs
        buffer.write(intToLE(tx.inputs.size))

        // Write sequences
        buffer.write(calculateSequences(tx))

        // Write number of outputs
        buffer.write(intToLE(tx.outputs.size))

        // Write outputs
        buffer.write(calculateOutputs(tx))

        // Write input index
        buffer.write(intToLE(inputIndex))

        return sha256(buffer.toByteArray())
    }

    private fun calculateScriptSigs(tx: Transaction): ByteArray? {
        if (tx.inputs.all { it.scriptSig.program.isEmpty() }) {
            return null
        }

        val buffer = ByteArrayOutputStream()
        tx.inputs.forEach { input ->
            buffer.write(input.scriptSig.program)
        }
        return sha256(buffer.toByteArray())
    }

    private fun calculateSequences(tx: Transaction): ByteArray {
        val buffer = ByteArrayOutputStream()
        tx.inputs.forEach { input ->
            buffer.write(longToLE(input.sequenceNumber))
        }
        return sha256(buffer.toByteArray())
    }

    private fun calculateOutputs(tx: Transaction): ByteArray {
        val buffer = ByteArrayOutputStream()
        tx.outputs.forEach { output ->
            buffer.write(longToLE(output.value.value))
            buffer.write(output.scriptPubKey.program)
        }
        return sha256(buffer.toByteArray())
    }

    private fun sha256(data: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data)
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
}