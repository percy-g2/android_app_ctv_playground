package com.androdevlinux.ctvplayground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.androdevlinux.ctvplayground.models.CTVContext
import com.androdevlinux.ctvplayground.models.Fields
import com.androdevlinux.ctvplayground.models.Output
import com.androdevlinux.ctvplayground.models.TxType
import com.androdevlinux.ctvplayground.ui.theme.CTVPlaygroundTheme
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.TestNet3Params

private val network: NetworkParameters = TestNet3Params.get()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        testBasicCTVTransactionCreation()
        enableEdgeToEdge()
        setContent {
            CTVPlaygroundTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CTVPlaygroundTheme {
        Greeting("Android")
    }
}

fun testBasicCTVTransactionCreation() {
    val context = CTVContext(
        network = network,
        txType = TxType.Segwit,
        fields = Fields(
            version = 2,
            locktime = 0,
            sequences = listOf(0xffffffffL),
            outputs = listOf(
                Output.Address(
                    // TestNet3 address
                    address = "tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx",
                    value = 100000L // 0.001 BTC in satoshis
                )
            ),
            inputIdx = 0
        )
    )

    val ctv = CTVTransaction(context)

    // Test locking script creation
    val lockingScript = ctv.getLockingScript().getOrThrow()
   // assertNotNull(lockingScript)
    println("assertNotNull" + lockingScript)
  //  assertTrue(lockingScript.program.isNotEmpty())
    println("assertTrue" + lockingScript.program.isNotEmpty())

    // Test address generation
    val address = ctv.getAddress().getOrThrow()
  //  assertNotNull(address)
    println("assertNotNull" + address)
   // assertTrue(address.toString().startsWith("2") || address.toString().startsWith("tb1"))
    println("assertTrue" + (address.toString().startsWith("2") || address.toString().startsWith("tb1")))
}