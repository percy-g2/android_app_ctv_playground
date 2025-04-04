package com.androdevlinux.ctvplayground.vault

import org.bitcoinj.base.AddressParser
import org.bitcoinj.base.Coin
import org.bitcoinj.base.Network
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.core.Transaction

class VaultManager {
    fun createVault(
        hotAddress: String,
        coldAddress: String,
        amount: Long,
        network: Network,
        delay: Int,
        useTaproot: Boolean = false
    ): Result<Vault> = runCatching {
        val hot = AddressParser.getDefault(network).parseAddress(hotAddress)
        val cold = AddressParser.getDefault(network).parseAddress(coldAddress)

        Vault(
            hot = hot,
            cold = cold,
            amount = Coin.valueOf(amount),
            network = network,
            delay = delay,
            taproot = useTaproot
        )
    }

    fun createVaultingTransaction(
        vault: Vault,
        fundingTxId: String,
        vout: Int
    ): Result<Transaction> = runCatching {
        val txId = Sha256Hash.wrap(fundingTxId)
        vault.createVaultingTransaction(txId, vout).getOrThrow()
    }

    fun createUnvaultingTransaction(
        vault: Vault,
        vaultTxId: String,
        vout: Int
    ): Result<Transaction> = runCatching {
        val txId = Sha256Hash.wrap(vaultTxId)
        vault.createUnvaultingTransaction(txId, vout).getOrThrow()
    }

    fun createSpendingTransactions(
        vault: Vault,
        unvaultTxId: String,
        vout: Int
    ): Result<Pair<Transaction, Transaction>> = runCatching {
        val txId = Sha256Hash.wrap(unvaultTxId)
        vault.createSpendingTransactions(txId, vout).getOrThrow()
    }
}