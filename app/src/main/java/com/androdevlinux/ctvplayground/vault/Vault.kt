package com.androdevlinux.ctvplayground.vault

import com.androdevlinux.ctvplayground.models.CTVContext
import com.androdevlinux.ctvplayground.models.Fields
import com.androdevlinux.ctvplayground.models.Output
import com.androdevlinux.ctvplayground.models.TxType
import com.androdevlinux.ctvplayground.utils.BitcoinUtils
import com.androdevlinux.ctvplayground.utils.HashUtils
import com.androdevlinux.ctvplayground.utils.ScriptUtils
import org.bitcoinj.base.Address
import org.bitcoinj.base.Coin
import org.bitcoinj.base.Network
import org.bitcoinj.base.ScriptType
import org.bitcoinj.base.SegwitAddress
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionInput
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.crypto.ECKey
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.script.ScriptOpCodes

class Vault(
    private val hot: Address,
    private val cold: Address,
    private val amount: Coin,
    private val network: Network,
    private val delay: Int,
    private val taproot: Boolean,
    private val ecKey: ECKey = ECKey()
) {

    private fun createVaultingAddress(): Result<Address> = runCatching {
        val vaultScript = createVaultScript().getOrThrow()
        if (taproot) {
            createTaprootAddress(vaultScript)
        } else {
            createSegwitAddress()
        }
    }

    fun createVaultingTransaction(
        fundingTxId: Sha256Hash,
        vout: Int
    ): Result<Transaction> = runCatching {
        Transaction().apply {
            setVersion(2)

            // Add input
            addInput(TransactionInput(
                null,
                ScriptBuilder().build().program(),
                TransactionOutPoint(vout.toLong(), fundingTxId),
                Coin.valueOf(0)
            ))

            // Add output to vault address
            val vaultAddress = createVaultingAddress().getOrThrow()
            println("vaultAddress $vaultAddress")
            addOutput(
                TransactionOutput(
                    null,
                    amount,
                    ScriptBuilder.createOutputScript(vaultAddress).program()
                )
            )
        }
    }

    fun createUnvaultingTransaction(
        vaultTxId: Sha256Hash,
        vout: Int
    ): Result<Transaction> = runCatching {
        val unvaultScript = createUnvaultScript().getOrThrow()
       // val witnessProgram = ScriptUtils.createP2WSHScript(unvaultScript)

        Transaction().apply {
            setVersion(2)

            // Add input from vault
            addInput(TransactionInput(
                null,
                ScriptBuilder().build().program(),
                TransactionOutPoint(vout.toLong(), vaultTxId),
                Coin.valueOf(0)
            ))

            // Add output with unvault script
            addOutput(
                TransactionOutput(
                    null,
                    amount.subtract(Coin.valueOf(600)),
                    unvaultScript.program() // Use program bytes directly
                )
            )
        }
    }

    fun createSpendingTransactions(
        unvaultTxId: Sha256Hash,
        vout: Int
    ): Result<Pair<Transaction, Transaction>> = runCatching {
        val coldTx = createColdSpendingTx(unvaultTxId, vout).getOrThrow()
        val hotTx = createHotSpendingTx(unvaultTxId, vout).getOrThrow()
        coldTx to hotTx
    }

    private fun createColdSpendingTx(
        txId: Sha256Hash,
        vout: Int
    ): Result<Transaction> = runCatching {
        Transaction().apply {
            setVersion(2)

            addInput(TransactionInput(
                null,
                ScriptBuilder().build().program(),
                TransactionOutPoint(vout.toLong(), txId),
                Coin.valueOf(0)
            ))

            addOutput(
                TransactionOutput(
                    null,
                    amount.subtract(Coin.valueOf(1200)),
                    ScriptBuilder.createOutputScript(cold).program()
                )
            )
        }
    }

    private fun createHotSpendingTx(
        txId: Sha256Hash,
        vout: Int
    ): Result<Transaction> = runCatching {
        Transaction().apply {
            setVersion(2)

            addInput(TransactionInput(
                null,
                ScriptBuilder().build().program(),
                TransactionOutPoint(vout.toLong(), txId),
                Coin.valueOf(0)
            ).apply {
                withSequence(delay.toLong())
            })

            addOutput(
                TransactionOutput(
                    null,
                    amount.subtract(Coin.valueOf(1200)),
                    ScriptBuilder.createOutputScript(hot).program()
                )
            )
        }
    }

    private fun createSegwitAddress(): Address {
      //  val scriptHash = HashUtils.calculateScriptHash(script)
        val hotHash = calculateHotTemplateHash().getOrThrow()
        val coldHash = calculateColdTemplateHash().getOrThrow()
        val redeemScript = ScriptUtils.createUnvaultScript(24 * 60, hotHash, coldHash)
        val p2wshScript = ScriptBuilder.createP2WSHOutputScript(redeemScript)
        val scriptHash = HashUtils.calculateScriptHash(p2wshScript)
        return SegwitAddress.fromHash(network, scriptHash)
    }

    private fun createTaprootAddress(script: Script): Address {
        val ecKey = ECKey()
        return ecKey.toAddress(ScriptType.P2PKH, network)
    }

    private fun createUnvaultingAddress(): Result<Address> = runCatching {
        if (taproot) {
            ecKey.toAddress(ScriptType.P2PKH, network)
        } else {
           createSegwitAddress()
        }
    }

    private fun createVaultScript(): Result<Script> = runCatching {
        val templateHash = calculateTemplateHash().getOrThrow()
        ScriptBuilder().apply {
            // Standard BIP-119 CTV script
            data(templateHash)            // Push template hash
            op(ScriptOpCodes.OP_NOP4)                   // CheckTemplateVerify opcode
            op(ScriptOpCodes.OP_DROP)    // Clean up stack
            // Add verification for spending
            op(ScriptOpCodes.OP_TRUE)    // Allow spending if template matches
        }.build()
    }

    private fun createUnvaultScript(): Result<Script> = runCatching {
        val hotHash = calculateHotTemplateHash().getOrThrow()
        val coldHash = calculateColdTemplateHash().getOrThrow()
        ScriptUtils.createUnvaultScript(delay, hotHash, coldHash)
    }

    private fun calculateTemplateHash(): Result<ByteArray> = runCatching {
        val ctx = CTVContext(
            network = network,
            txType = if (taproot) TxType.Taproot(numsPoint()) else TxType.Segwit,
            fields = Fields(
                version = 2,
                locktime = 0,
                sequences = listOf(0xFFFFFFFFL),
                outputs = listOf(
                    Output.Address(
                        address = createUnvaultingAddress().getOrThrow().toString(),
                        value = amount.value - 600
                    )
                ),
                inputIdx = 0
            )
        )
        val vault_ctv = VaultTransaction(ctx)
        println(ctx.fields.outputs[0])
        vault_ctv.calculateTemplateHash()
    }

    private fun calculateHotTemplateHash(): Result<ByteArray> = runCatching {
        val ctx = CTVContext(
            network = network,
            txType = if (taproot) TxType.Taproot(numsPoint()) else TxType.Segwit,
            fields = Fields(
                version = 2,
                locktime = 0,
                sequences = listOf(delay.toLong()),
                outputs = listOf(
                    Output.Address(
                        address = hot.toString(),
                        value = amount.value - 1200
                    )
                ),
                inputIdx = 0
            )
        )
        VaultTransaction(ctx).calculateTemplateHash()
    }

    private fun calculateColdTemplateHash(): Result<ByteArray> = runCatching {
        val ctx = CTVContext(
            network = network,
            txType = if (taproot) TxType.Taproot(numsPoint()) else TxType.Segwit,
            fields = Fields(
                version = 2,
                locktime = 0,
                sequences = listOf(0L),
                outputs = listOf(
                    Output.Address(
                        address = cold.toString(),
                        value = amount.value - 1200
                    )
                ),
                inputIdx = 0
            )
        )
        VaultTransaction(ctx).calculateTemplateHash()
    }

    private fun numsPoint(): ECKey {
        return BitcoinUtils.hash2curve("Activate CTV now!".toByteArray())
    }
}