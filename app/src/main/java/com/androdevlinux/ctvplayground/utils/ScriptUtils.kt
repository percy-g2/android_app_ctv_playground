package com.androdevlinux.ctvplayground.utils

import android.util.Log
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.script.ScriptOpCodes

object ScriptUtils {
    fun createCTVScript(templateHash: ByteArray): Script {
        return ScriptBuilder().apply {
            data(templateHash)
            op(ScriptOpCodes.OP_NOP4)
        }.build()
    }

    // In ScriptUtils.kt
    fun createP2WSHScript(script: Script): Script {
        val witnessScript = BitcoinUtils.sha256(script.program())
        Log.d("ScriptUtils", "Original Script: ${script.chunks().joinToString(" ")}")
        Log.d("ScriptUtils", "Witness Script Hash: ${witnessScript.toHex()}")
        return ScriptBuilder().apply {
            number(0)
            data(witnessScript)
        }.build().also {
            Log.d("ScriptUtils", "P2WSH Script: ${it.chunks().joinToString(" ")}")
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    fun createP2TRScript(script: Script): Script {
        // P2TR: OP_1 <32-byte-hash>
        val scriptHash = BitcoinUtils.sha256(script.program())
        return ScriptBuilder().apply {
            // Use the correct OP_1 value (1)
            number(1)
            data(scriptHash)
        }.build()
    }

    fun createUnvaultScript(
        delay: Int,
        hotTemplateHash: ByteArray,
        coldTemplateHash: ByteArray
    ): Script {
        return ScriptBuilder().apply {
            op(ScriptOpCodes.OP_IF)
            // Hot path with timelock
            number(delay.toLong())
            op(ScriptOpCodes.OP_CHECKSEQUENCEVERIFY)
            op(ScriptOpCodes.OP_DROP)
            data(hotTemplateHash)
            op(ScriptOpCodes.OP_NOP4)
            // Cold path
            op(ScriptOpCodes.OP_ELSE)
            data(coldTemplateHash)
            op(ScriptOpCodes.OP_NOP4)
            op(ScriptOpCodes.OP_ENDIF)
        }.build()
    }
}

fun decodeScriptPubKey(scriptPubKey: Script): String {
    return scriptPubKey.chunks().joinToString(" ") { chunk ->
        when {
            chunk.isOpCode -> ScriptOpCodes.getOpCodeName(chunk.opcode)
            chunk.data != null -> {
                val hexData = chunk.data!!.joinToString("") { "%02x".format(it) }
                "PUSH(${hexData})"
            }
            else -> "UNKNOWN"
        }
    }.also { decoded ->
        Log.d("ScriptUtils", "Decoded ScriptPubKey: $decoded")
    }
}
