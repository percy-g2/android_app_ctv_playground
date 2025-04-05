package com.androdevlinux.ctvplayground.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androdevlinux.ctvplayground.CTVTransaction
import com.androdevlinux.ctvplayground.models.CTVContext
import com.androdevlinux.ctvplayground.models.Fields
import com.androdevlinux.ctvplayground.models.Output
import com.androdevlinux.ctvplayground.models.TxType
import com.androdevlinux.ctvplayground.utils.decodeScriptPubKey
import com.androdevlinux.ctvplayground.vault.VaultManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.params.SigNetParams

class CTVViewModel : ViewModel() {
    private val _ctvState = MutableStateFlow<CTVState>(CTVState.Initial)
    val ctvState = _ctvState.asStateFlow()

    private val _vaultState = MutableStateFlow<VaultState>(VaultState.Initial)
    val vaultState = _vaultState.asStateFlow()

    fun testCTV(address: String, amount: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _ctvState.value = CTVState.Loading
            delay(500)
            try {
                val context = CTVContext(
                    network = SigNetParams.get().network(),
                    txType = TxType.Segwit,
                    fields = Fields(
                        version = 2,
                        locktime = 0,
                        sequences = listOf(0xffffffffL),
                        outputs = listOf(
                            Output.Address(
                                address = address,
                                value = amount
                            )
                        ),
                        inputIdx = 0
                    )
                )

                val ctv = CTVTransaction(context)
                val lockingScript = ctv.getLockingScript().getOrThrow()
                val ctvAddress = ctv.getAddress().getOrThrow()
                val spendingTxs = ctv.createSpendingTransaction(
                    Sha256Hash.ZERO_HASH,
                    0
                ).getOrThrow()

                _ctvState.value = CTVState.Success(
                    lockingScript = lockingScript.toString(),
                    scriptProgramSize = lockingScript.program().size,
                    address = ctvAddress.toString(),
                    transactions = spendingTxs.map { tx ->
                        TransactionDetails(
                            txHash = tx.serialize().joinToString("") { "%02x".format(it) },
                            size = tx.messageSize(),
                            outputCount = tx.outputs.size
                        )
                    }
                )
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message.isNullOrEmpty() -> "Unknown error occurred"
                    else -> e.message!!
                }
                _ctvState.value = CTVState.Error(errorMessage)
                e.printStackTrace()
            }
        }
    }

    fun testVault(
        hotAddress: String,
        coldAddress: String,
        amount: Long,
        delay: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _vaultState.value = VaultState.Loading
            delay(500)
            try {
                val vaultManager = VaultManager()
                val vault = vaultManager.createVault(
                    hotAddress = hotAddress,
                    coldAddress = coldAddress,
                    amount = amount,
                    network = SigNetParams.get().network(),
                    delay = delay,
                    useTaproot = false
                ).getOrThrow()

                val vaultingTx = vaultManager.createVaultingTransaction(
                    vault = vault,
                    fundingTxId = Sha256Hash.ZERO_HASH.toString(),
                    vout = 0
                ).getOrThrow()

                val unvaultingTx = vaultManager.createUnvaultingTransaction(
                    vault = vault,
                    vaultTxId = vaultingTx.txId.toString(),
                    vout = 0
                ).getOrThrow()

                val (coldTx, hotTx) = vaultManager.createSpendingTransactions(
                    vault = vault,
                    unvaultTxId = unvaultingTx.txId.toString(),
                    vout = 0
                ).getOrThrow()

                _vaultState.value = VaultState.Success(
                    vaultingTx = VaultingTxDetails(
                        txHash = vaultingTx.serialize().joinToString("") { "%02x".format(it) },
                        size = vaultingTx.messageSize(),
                        outputScript = decodeScriptPubKey(vaultingTx.outputs[0].scriptPubKey)
                    ),
                    unvaultingTx = UnvaultingTxDetails(
                        txHash = unvaultingTx.serialize().joinToString("") { "%02x".format(it) },
                        size = unvaultingTx.messageSize(),
                        outputScript = decodeScriptPubKey(unvaultingTx.outputs[0].scriptPubKey)
                    ),
                    spendingTxs = SpendingTxDetails(
                        coldTx = TxDetails(
                            txHash = coldTx.serialize().joinToString("") { "%02x".format(it) },
                            outputScript = decodeScriptPubKey(coldTx.outputs[0].scriptPubKey)
                        ),
                        hotTx = TxDetails(
                            txHash = hotTx.serialize().joinToString("") { "%02x".format(it) },
                            outputScript = decodeScriptPubKey(hotTx.outputs[0].scriptPubKey)
                        )
                    )
                )
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message.isNullOrEmpty() -> "Unknown error occurred"
                    else -> e.message!!
                }
                _vaultState.value = VaultState.Error(errorMessage)
                e.printStackTrace()
            }
        }
    }
}

sealed class CTVState {
    data object Initial : CTVState()
    data object Loading : CTVState()
    data class Success(
        val lockingScript: String,
        val scriptProgramSize: Int,
        val address: String,
        val transactions: List<TransactionDetails>
    ) : CTVState()
    data class Error(val message: String) : CTVState()
}

data class TransactionDetails(
    val txHash: String,
    val size: Int,
    val outputCount: Int
)

sealed class VaultState {
    data object Initial : VaultState()
    data object Loading : VaultState()
    data class Success(
        val vaultingTx: VaultingTxDetails,
        val unvaultingTx: UnvaultingTxDetails,
        val spendingTxs: SpendingTxDetails
    ) : VaultState()
    data class Error(val message: String) : VaultState()
}

data class VaultingTxDetails(
    val txHash: String,
    val size: Int,
    val outputScript: String
)

data class UnvaultingTxDetails(
    val txHash: String,
    val size: Int,
    val outputScript: String
)

data class SpendingTxDetails(
    val coldTx: TxDetails,
    val hotTx: TxDetails
)

data class TxDetails(
    val txHash: String,
    val outputScript: String
)