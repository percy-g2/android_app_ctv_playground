package com.androdevlinux.ctvplayground

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.androdevlinux.ctvplayground.models.CTVContext
import com.androdevlinux.ctvplayground.models.Fields
import com.androdevlinux.ctvplayground.models.Output
import com.androdevlinux.ctvplayground.models.TxType
import com.androdevlinux.ctvplayground.ui.theme.CTVPlaygroundTheme
import com.androdevlinux.ctvplayground.vault.VaultManager
import org.bitcoinj.base.Sha256Hash
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.TestNet3Params

private const val TAG = "CTV_TEST"
private val network: NetworkParameters = TestNet3Params.get()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Test both CTV and Vault functionality
        testCTV()
        testVault()

        enableEdgeToEdge()
        setContent {
            CTVPlaygroundTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "CTV Playground",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun testCTV() {
        try {
            Log.d(TAG, "Testing basic CTV functionality...")

            // Create CTV context
            val context = CTVContext(
                network = network,
                txType = TxType.Segwit,
                fields = Fields(
                    version = 2,
                    locktime = 0,
                    sequences = listOf(0xffffffffL),
                    outputs = listOf(
                        Output.Address(
                            address = "tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx",
                            value = 100000L // 0.001 BTC
                        )
                    ),
                    inputIdx = 0
                )
            )

            val ctv = CTVTransaction(context)

            // Test locking script
            val lockingScript = ctv.getLockingScript().getOrThrow()
            Log.d(TAG, "Locking Script: $lockingScript")
            Log.d(TAG, "Script Program Size: ${lockingScript.program.size}")

            // Test address generation
            val address = ctv.getAddress().getOrThrow()
            Log.d(TAG, "CTV Address: $address")

            // Test spending transaction creation
            val spendingTxs = ctv.createSpendingTransaction(
                Sha256Hash.wrap("0000000000000000000000000000000000000000000000000000000000000000"),
                0
            ).getOrThrow()

            spendingTxs.forEachIndexed { index, tx ->
                Log.d(TAG, "Spending TX $index: ${tx.txId}")
                Log.d(TAG, "TX Size: ${tx.messageSize}")
                Log.d(TAG, "Output Count: ${tx.outputs.size}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "CTV Test Error: ${e.message}", e)
        }
    }

    private fun testVault() {
        try {
            Log.d(TAG, "Testing Vault functionality...")

            val vaultManager = VaultManager()

            // Create vault
            val vault = vaultManager.createVault(
                hotAddress = "tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx",
                coldAddress = "tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx",
                amount = 100000L,
                network = network,
                delay = 144,
                useTaproot = false
            ).getOrThrow()

            // Test vaulting transaction
            val vaultingTx = vaultManager.createVaultingTransaction(
                vault = vault,
                fundingTxId = "0000000000000000000000000000000000000000000000000000000000000000",
                vout = 0
            ).getOrThrow()

            Log.d(TAG, "Vaulting TX: ${vaultingTx.txId}")
            Log.d(TAG, "Vaulting TX Size: ${vaultingTx.messageSize}")
            Log.d(TAG, "Vaulting Output Script: ${vaultingTx.outputs[0].scriptPubKey}")

            // Test unvaulting transaction
            val unvaultingTx = vaultManager.createUnvaultingTransaction(
                vault = vault,
                vaultTxId = vaultingTx.txId.toString(),
                vout = 0
            ).getOrThrow()

            Log.d(TAG, "Unvaulting TX: ${unvaultingTx.txId}")
            Log.d(TAG, "Unvaulting TX Size: ${unvaultingTx.messageSize}")
            Log.d(TAG, "Unvaulting Output Script: ${unvaultingTx.outputs[0].scriptPubKey}")

            // Test spending transactions
            val (coldTx, hotTx) = vaultManager.createSpendingTransactions(
                vault = vault,
                unvaultTxId = unvaultingTx.txId.toString(),
                vout = 0
            ).getOrThrow()

            Log.d(TAG, "Cold TX: ${coldTx.txId}")
            Log.d(TAG, "Cold TX Output Script: ${coldTx.outputs[0].scriptPubKey}")
            Log.d(TAG, "Hot TX: ${hotTx.txId}")
            Log.d(TAG, "Hot TX Output Script: ${hotTx.outputs[0].scriptPubKey}")

        } catch (e: Exception) {
            Log.e(TAG, "Vault Test Error: ${e.message}", e)
            e.printStackTrace()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}