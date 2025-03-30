package com.androdevlinux.ctvplayground.models

import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.crypto.ECKey

data class CTVContext(
    val network: NetworkParameters,
    val txType: TxType,
    val fields: Fields
) {
    companion object {
        const val CTV_OP_CODE = 0xb3 // OP_NOP4
    }
}

data class Fields(
    val version: Int = 2,
    val locktime: Long = 0,
    val sequences: List<Long>,
    val outputs: List<Output>,
    val inputIdx: Int
)

sealed class TxType {
    object Segwit : TxType()
    data class Taproot(val internalKey: ECKey) : TxType()
}

sealed class Output {
    data class Address(
        val address: String,
        val value: Long // Changed from Coin to Long
    ) : Output()

    data class Data(
        val data: ByteArray
    ) : Output() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Data
            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }

    data class Tree(
        val tree: CTVContext,
        val value: Long // Changed from Coin to Long
    ) : Output()
}