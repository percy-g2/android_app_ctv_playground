package com.androdevlinux.ctvplayground.utils

import org.bitcoinj.base.Address
import org.bitcoinj.base.LegacyAddress
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.script.Script

object AddressUtils {
    fun createP2WSHAddress(script: Script, network: NetworkParameters): Address {
        // Create P2WSH using RIPEMD160(SHA256()) of the script
        val scriptHash = HashUtils.calculateScriptHash(script)
        return LegacyAddress.fromScriptHash(network, scriptHash)
    }

    fun createP2TRAddress(script: Script, network: NetworkParameters): Address {
        // Simplified P2TR implementation - for now using P2WSH
        val scriptHash = HashUtils.calculateScriptHash(script)
        return LegacyAddress.fromScriptHash(network, scriptHash)
    }
}