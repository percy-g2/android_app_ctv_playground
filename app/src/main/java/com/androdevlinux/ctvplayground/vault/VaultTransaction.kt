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
import org.bitcoinj.script.ScriptOpCodes

class VaultTransaction(private val context: CTVContext) {
    fun calculateTemplateHash(): ByteArray {
        val tx = createBaseTx()
        // According to BIP-119, we need to calculate the template hash
        // in a specific way that includes all transaction details except signatures
        return CTVHashCalculator.calculateTemplateHash(tx, context.fields.inputIdx)
    }

    private fun createBaseTx(): Transaction {
        return Transaction().apply {
            // BIP-119 requires version 2
            setVersion(2)

            // Set locktime as specified in the context
            lockTime = context.fields.locktime

            // Add inputs with proper sequences
            context.fields.sequences.forEach { sequence ->
                addInput(TransactionInput(
                    null,
                    ScriptBuilder().build().program(),
                    TransactionOutPoint(0L, Sha256Hash.ZERO_HASH),
                    Coin.ZERO
                ).apply {
                    // BIP-119 requires explicit sequence numbers
                    withSequence(sequence)
                })
            }

            // Add outputs according to BIP-119 specifications
            context.fields.outputs.forEach { output ->
                when (output) {
                    is Output.Address -> {
                        val addr = AddressParser.getDefault(context.network).parseAddress(output.address)
                        addOutput(Coin.valueOf(output.value), addr.toOutputScript())
                    }
                    is Output.Data -> {
                        // OP_RETURN outputs as specified in BIP-119
                        val script = ScriptBuilder()
                            .op(ScriptOpCodes.OP_RETURN)
                            .data(output.data)
                            .build()
                        addOutput(Coin.ZERO, script)
                    }
                    is Output.Tree -> {
                        // Nested CTV outputs
                        val treeAddress = CTVTransaction(output.tree).getAddress().getOrThrow()
                        addOutput(Coin.valueOf(output.value), treeAddress.toOutputScript())
                    }
                }
            }
        }
    }
}