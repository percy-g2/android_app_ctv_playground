// utils/Extensions.kt
package com.androdevlinux.ctvplayground.utils

import org.bitcoinj.base.Address
import org.bitcoinj.core.Transaction
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.script.ScriptOpCodes
import java.security.MessageDigest

fun Transaction.getHash(): String {
    val bytes = this.bitcoinSerialize()
    val digest = MessageDigest.getInstance("SHA-256")
    val firstHash = digest.digest(bytes)
    val secondHash = digest.digest(firstHash)
    return secondHash.reversedArray().toHex()
}

fun ByteArray.toHex(): String =
    joinToString("") { "%02x".format(it) }

fun Address.toOutputScript(): Script {
    return ScriptBuilder.createOutputScript(this)
}

fun Script.toP2WSHScript(): Script {
    val scriptHash = HashUtils.calculateScriptHash(this)
    return ScriptBuilder().apply {
        op(ScriptOpCodes.OP_0)
        data(scriptHash)
    }.build()
}