// utils/Extensions.kt
package com.androdevlinux.ctvplayground.utils

import org.bitcoinj.base.Address
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.script.ScriptOpCodes

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