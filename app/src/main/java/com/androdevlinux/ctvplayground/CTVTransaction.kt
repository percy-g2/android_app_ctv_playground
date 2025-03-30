package com.androdevlinux.ctvplayground

import com.androdevlinux.ctvplayground.hash.CTVHashCalculator
import com.androdevlinux.ctvplayground.models.*
import com.androdevlinux.ctvplayground.utils.AddressUtils
import org.bitcoinj.base.Address
import org.bitcoinj.base.Coin
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.core.*
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder

class CTVTransaction(private val context: CTVContext) {

    fun getLockingScript(): Result<Script> = runCatching {
        val templateHash = CTVHashCalculator.calculateTemplateHash(createBaseTx(), context.fields.inputIdx)
        ScriptBuilder().apply {
            data(templateHash)
            op(CTVContext.CTV_OP_CODE)
        }.build()
    }


    fun getAddress(): Result<Address> = runCatching {
        val lockingScript = getLockingScript().getOrThrow()
        when (context.txType) {
            is TxType.Segwit -> {
                AddressUtils.createP2WSHAddress(lockingScript, context.network)
            }
            is TxType.Taproot -> {
                AddressUtils.createP2TRAddress(lockingScript, context.network)
            }
        }
    }

    fun createSpendingTransaction(prevTxId: Sha256Hash, outputIndex: Int): Result<List<Transaction>> = runCatching {
        val transactions = mutableListOf<Transaction>()

        val tx = Transaction(context.network).apply {
            setVersion(context.fields.version)
            lockTime = context.fields.locktime

            // Add input
            addInput(TransactionInput(
                null,
                ScriptBuilder().build().program,
                TransactionOutPoint(outputIndex.toLong(),prevTxId),
                Coin.valueOf(0)
            ))

            // Add outputs
            context.fields.outputs.forEach { output ->
                when (output) {
                    is Output.Address -> {
                        val addr = Address.fromString(context.network, output.address)
                        addOutput(Coin.valueOf(output.value), ScriptBuilder.createOutputScript(addr))
                    }
                    is Output.Data -> {
                        val script = ScriptBuilder().data(output.data).build()
                        addOutput(Coin.ZERO, script)
                    }
                    is Output.Tree -> {
                        val treeAddress = CTVTransaction(output.tree).getAddress().getOrThrow()
                        addOutput(Coin.valueOf(output.value), ScriptBuilder.createOutputScript(treeAddress))
                    }
                }
            }
        }

        transactions.add(tx)

        // Handle nested transactions
        context.fields.outputs.firstOrNull()?.let { output ->
            if (output is Output.Tree) {
                val nestedTx = CTVTransaction(output.tree)
                    .createSpendingTransaction(tx.txId, 0)
                    .getOrThrow()
                transactions.addAll(nestedTx)
            }
        }

        transactions
    }

    private fun createBaseTx(): Transaction {
        return Transaction(context.network).apply {
            setVersion(context.fields.version)
            lockTime = context.fields.locktime

            // Add dummy inputs with sequences
            context.fields.sequences.forEach { sequence ->
                addInput(TransactionInput(
                    null,
                    ScriptBuilder().build().program,
                    TransactionOutPoint( 0, Sha256Hash.ZERO_HASH),
                    Coin.valueOf(0)
                ).apply {
                    sequenceNumber
                })
            }

            // Add outputs
            context.fields.outputs.forEach { output ->
                when (output) {
                    is Output.Address -> {
                        val addr = Address.fromString(context.network, output.address)
                        addOutput(Coin.valueOf(output.value), ScriptBuilder.createOutputScript(addr))
                    }
                    is Output.Data -> {
                        val script = ScriptBuilder().data(output.data).build()
                        addOutput(Coin.ZERO, script)
                    }
                    is Output.Tree -> {
                        val treeAddress = CTVTransaction(output.tree).getAddress().getOrThrow()
                        addOutput(Coin.valueOf(output.value), ScriptBuilder.createOutputScript(treeAddress))
                    }
                }
            }
        }
    }
}