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
import com.androdevlinux.ctvplayground.ui.viewmodel.CTVState
import com.androdevlinux.ctvplayground.ui.viewmodel.CTVViewModel


@Composable
fun CTVScreen(
    viewModel: CTVViewModel,
    modifier: Modifier = Modifier
) {
    var address by remember { mutableStateOf("tb1qw508d6qejxtdg4y5r3zarvary0c5xw7kxpjzsx") }
    var amount by remember { mutableStateOf("100000") }
    val ctvState by viewModel.ctvState.collectAsState()

    val isValidAddress = remember(address) { address.isNotBlank() }
    val isValidAmount = remember(amount) {
        amount.isNotBlank() && amount.toLongOrNull()?.let { it > 0 } == true
    }
    val isFormValid = remember(isValidAddress, isValidAmount) {
        isValidAddress && isValidAmount
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "CTV Transaction Creator",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Destination Address") },
            supportingText = {
                if (!isValidAddress) {
                    Text(
                        text = "Address cannot be empty",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingIcon = {
                if (address.isNotEmpty()) {
                    IconButton(
                        onClick = { address = "" }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            },
            isError = !isValidAddress,
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

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            shape = RoundedCornerShape(8.dp),
            onClick = {
                viewModel.testCTV(
                    address = address,
                    amount = amount.toLongOrNull() ?: 0L
                )
            },
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create CTV Transaction")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = ctvState) {
            is CTVState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is CTVState.Success -> {
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
                        Text(
                            text = "CTV Address",
                            style = MaterialTheme.typography.titleMedium
                        )
                        SelectionContainer {
                            Text(
                                text = state.address,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Text(
                            text = "Locking Script",
                            style = MaterialTheme.typography.titleMedium
                        )
                        SelectionContainer {
                            Text(
                                text = state.lockingScript,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        Text(
                            text = "Script Program Size: ${state.scriptProgramSize} bytes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Text(
                            text = "Spending Transactions",
                            style = MaterialTheme.typography.titleMedium
                        )
                        state.transactions.forEachIndexed { index, tx ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "Transaction #${index + 1}",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    SelectionContainer {
                                        Text(
                                            text = "TXID: ${tx.txId}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Text(
                                        text = "Size: ${tx.size} bytes",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Outputs: ${tx.outputCount}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
            is CTVState.Error -> {
                ErrorCard(
                    error = state.message,
                    onRetry = {
                        viewModel.testCTV(address, amount.toLongOrNull() ?: 0L)
                    }
                )
            }
            else -> Unit
        }
    }
}