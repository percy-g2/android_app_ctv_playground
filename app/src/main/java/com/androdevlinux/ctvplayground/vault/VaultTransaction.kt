package com.androdevlinux.ctvplayground.vault

import com.androdevlinux.ctvplayground.CTVTransaction
import com.androdevlinux.ctvplayground.hash.CTVHashCalculator
import com.androdevlinux.ctvplayground.models.CTVContext
import com.androdevlinux.ctvplayground.models.Output
import com.androdevlinux.ctvplayground.utils.toOutputScript
import org.bitcoinj.base.AddressParser
import org.bitcoinj.base.Coin
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionInput
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.script.ScriptBuilder

class VaultTransaction(private val context: CTVContext) {
    fun calculateTemplateHash(): ByteArray {
        val tx = createBaseTx()
        return CTVHashCalculator.calculateTemplateHash(tx, context.fields.inputIdx)
    }

    private fun createBaseTx(): Transaction {
        return Transaction().apply {
            setVersion(context.fields.version)
            lockTime = context.fields.locktime

            // Add dummy inputs with sequences
            context.fields.sequences.forEach { sequence ->
                addInput(
                    TransactionInput(
                        null,
                        ScriptBuilder().build().program(),
                        TransactionOutPoint(0L, Sha256Hash.ZERO_HASH),
                        Coin.ZERO
                    ).apply {
                        withSequence(sequence)
                    }
                )
            }

            // Add outputs
            context.fields.outputs.forEach { output ->
                when (output) {
                    is Output.Address -> {
                        val addr = AddressParser.getDefault(context.network).parseAddress(output.address)
                        addOutput(Coin.valueOf(output.value), addr.toOutputScript())
                    }
                    is Output.Data -> {
                        val script = ScriptBuilder().data(output.data).build()
                        addOutput(Coin.ZERO, script)
                    }
                    is Output.Tree -> {
                        val treeAddress = CTVTransaction(output.tree).getAddress().getOrThrow()
                        addOutput(Coin.valueOf(output.value), treeAddress.toOutputScript())
                    }
                }
            }
        }
    }
}