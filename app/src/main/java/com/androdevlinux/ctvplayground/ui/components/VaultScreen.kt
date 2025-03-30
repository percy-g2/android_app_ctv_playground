package com.androdevlinux.ctvplayground.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.androdevlinux.ctvplayground.ui.viewmodel.CTVViewModel
import com.androdevlinux.ctvplayground.ui.viewmodel.VaultState

@Composable
fun VaultScreen(
    viewModel: CTVViewModel,
    modifier: Modifier = Modifier
) {
    var hotAddress by remember { mutableStateOf("tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx") }
    var coldAddress by remember { mutableStateOf("tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx") }
    var amount by remember { mutableStateOf("100000") }
    var delay by remember { mutableStateOf("144") }
    val vaultState by viewModel.vaultState.collectAsState()

    val isValidHotAddress = remember(hotAddress) { hotAddress.isNotBlank() }
    val isValidColdAddress = remember(coldAddress) { coldAddress.isNotBlank() }
    val isValidAmount = remember(amount) {
        amount.isNotBlank() && amount.toLongOrNull()?.let { it > 0 } == true
    }
    val isValidDelay = remember(delay) {
        delay.isNotBlank() && delay.toIntOrNull()?.let { it > 0 } == true
    }
    val isFormValid = remember(isValidHotAddress, isValidColdAddress, isValidAmount, isValidDelay) {
        isValidHotAddress && isValidColdAddress && isValidAmount && isValidDelay
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Bitcoin Vault Creator",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = hotAddress,
            onValueChange = { hotAddress = it },
            label = { Text("Hot Wallet Address") },
            supportingText = {
                if (!isValidHotAddress) {
                    Text(
                        text = "Hot wallet address cannot be empty",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingIcon = {
                if (hotAddress.isNotEmpty()) {
                    IconButton(
                        onClick = { hotAddress = "" }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            },
            isError = !isValidHotAddress,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = coldAddress,
            onValueChange = { coldAddress = it },
            label = { Text("Cold Storage Address") },
            supportingText = {
                if (!isValidColdAddress) {
                    Text(
                        text = "Cold storage address cannot be empty",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingIcon = {
                if (coldAddress.isNotEmpty()) {
                    IconButton(
                        onClick = { coldAddress = "" }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            },
            isError = !isValidColdAddress,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount (satoshis)") },
            supportingText = {
                if (!isValidAmount) {
                    Text(
                        text = "Amount must be greater than 0",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingIcon = {
                if (amount.isNotEmpty()) {
                    IconButton(
                        onClick = { amount = "" }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            },
            isError = !isValidAmount,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = delay,
            onValueChange = { delay = it },
            label = { Text("Time Lock (blocks)") },
            supportingText = {
                if (!isValidDelay) {
                    Text(
                        text = "Time lock must be greater than 0",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingIcon = {
                if (delay.isNotEmpty()) {
                    IconButton(
                        onClick = { delay = "" }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            },
            isError = !isValidDelay,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            shape = RoundedCornerShape(8.dp),
            onClick = {
                viewModel.testVault(
                    hotAddress = hotAddress,
                    coldAddress = coldAddress,
                    amount = amount.toLongOrNull() ?: 0L,
                    delay = delay.toIntOrNull() ?: 0
                )
            },
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Vault")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = vaultState) {
            is VaultState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is VaultState.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Vaulting Transaction
                        Text(
                            text = "Vaulting Transaction",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                SelectionContainer {
                                    Text(
                                        text = "TXID: ${state.vaultingTx.txId}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Text(
                                    text = "Size: ${state.vaultingTx.size} bytes",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Output Script:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                SelectionContainer {
                                    Text(
                                        text = state.vaultingTx.outputScript,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // Unvaulting Transaction
                        Text(
                            text = "Unvaulting Transaction",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                SelectionContainer {
                                    Text(
                                        text = "TXID: ${state.unvaultingTx.txId}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Text(
                                    text = "Size: ${state.unvaultingTx.size} bytes",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Output Script:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                SelectionContainer {
                                    Text(
                                        text = state.unvaultingTx.outputScript,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // Spending Transactions
                        Text(
                            text = "Spending Transactions",
                            style = MaterialTheme.typography.titleMedium
                        )

                        // Cold TX
                        Text(
                            text = "Cold Storage Path",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                SelectionContainer {
                                    Text(
                                        text = "TXID: ${state.spendingTxs.coldTx.txId}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Text(
                                    text = "Output Script:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                SelectionContainer {
                                    Text(
                                        text = state.spendingTxs.coldTx.outputScript,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        // Hot TX
                        Text(
                            text = "Hot Wallet Path",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                SelectionContainer {
                                    Text(
                                        text = "TXID: ${state.spendingTxs.hotTx.txId}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Text(
                                    text = "Output Script:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                SelectionContainer {
                                    Text(
                                        text = state.spendingTxs.hotTx.outputScript,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
            is VaultState.Error -> {
                ErrorCard(
                    error = state.message,
                    onRetry = {
                        viewModel.testVault(
                            hotAddress = hotAddress,
                            coldAddress = coldAddress,
                            amount = amount.toLongOrNull() ?: 0L,
                            delay = delay.toIntOrNull() ?: 0
                        )
                    }
                )
            }
            else -> Unit
        }
    }
}