# CTV Playground - Android Implementation

A native Android implementation and demonstration of Bitcoin's proposed OP_CHECKTEMPLATEVERIFY (CTV) soft fork, including a CTV Vault implementation.

## What is OP_CHECKTEMPLATEVERIFY?

OP_CHECKTEMPLATEVERIFY (CTV) is a proposed Bitcoin soft fork (BIP-119) that introduces a new opcode to enable covenant-style transactions. CTV allows you to commit to specific spending conditions for your Bitcoin, creating predetermined transaction paths that enhance Bitcoin's programmability while maintaining its security model.

### Key Features of CTV

1. **Transaction Templates**: CTV allows you to create a template that specifies exactly how funds can be spent in the future.
2. **Covenants**: Enables covenant-style restrictions on how Bitcoin can be spent.
3. **Atomic Multi-Path Commitments**: Create multiple predetermined spending paths that are enforced at the protocol level.
4. **Congestion Control**: Enables batching and better fee management through transaction trees.

## Technical Details

### How CTV Works

1. **Template Hash Creation**:
kotlin val templateHash = sha256( version + // Transaction version locktime + // Transaction locktime scriptSigs + // Hash of input script signatures sequences + // Hash of input sequences outputs // Hash of all outputs )

2. **Locking Script**:
<template_hash> OP_CTV

3. **Validation**:
   - When spending, the transaction must match the committed template exactly
   - All outputs, sequences, version, and locktime must match
   - Only the input scripts can differ

### CTV Use Cases

1. **Vaults**:
   - Two-path spending: immediate to cold storage or time-delayed to hot wallet
   - Enhanced security for large holdings
kotlin val vault = Vault( hotAddress = "hot_wallet_address", coldAddress = "cold_storage_address", amount = amount, delay = 144 // ~1 day timelock )
2. **Payment Pools**:
   - Efficient batching of multiple payments
   - Reduced on-chain footprint
kotlin val outputs = listOf( Output.Address(address1, value1), Output.Address(address2, value2) )
3. **Congestion Control**:
   - Transaction trees for better fee management
   - Predictable mempool behavior
kotlin val tree = Output.Tree( tree = nestedCTV, amount = amount )

## Implementation Details

### Core Components

1. **CTV Transaction**:
kotlin class CTVTransaction( network: NetworkParameters, txType: TxType, fields: Fields )
2. **Vault Implementation**:
kotlin class Vault( hot: Address, cold: Address, amount: Coin, delay: Int )
### Key Features

- Native Android implementation using Jetpack Compose
- Full bitcoinj integration
- Support for both Segwit and Taproot
- Comprehensive transaction validation
- Real-time template hash calculation
- Interactive UI for testing and demonstration

## Benefits of CTV Soft Fork

1. **Enhanced Security**:
   - Predetermined spending paths
   - Protocol-level enforcement
   - No trusted setup required

2. **Scalability Improvements**:
   - Better batching capabilities
   - Reduced on-chain footprint
   - Improved fee management

3. **New Use Cases**:
   - Vaults without presigned transactions
   - Payment pools with guaranteed outputs
   - Congestion control mechanisms
   - Cross-chain atomic swaps

4. **Developer Benefits**:
   - Simple and auditable
   - No new cryptographic assumptions
   - Compatible with existing Bitcoin infrastructure

## Technical Requirements

- Android Studio Arctic Fox or later
- Kotlin 1.8+
- Android API Level 29+
- bitcoinj 0.17+
- BouncyCastle for cryptographic operations

## Building and Testing

1. Clone the repository: bash git clone https://github.com/percy-g2/android_app_ctv_playground.git
2. Open in Android Studio
3. Build and run on an emulator or device

## Future Improvements

1. **Technical Enhancements**:
   - Advanced fee estimation
   - Multiple vault configurations
   - Enhanced transaction tree visualization
   - Additional covenant patterns

2. **User Experience**:
   - Transaction simulation
   - Visual transaction flow
   - Advanced debugging tools
   - Network status monitoring

## Contributing

Contributions are welcome! Please read our contributing guidelines and submit pull requests for any improvements.

## References

- [BIP-119 Specification](https://github.com/bitcoin/bips/blob/master/bip-0119.mediawiki)
- [CTV Playground Website](https://ctv.ursus.camp)
- [Bitcoin Inquisition](https://github.com/bitcoin-inquisition/bitcoin)

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Jeremy Rubin for the original CTV proposal
- Bitcoin Inquisition team
- bitcoinj developers
- Android/Kotlin community
- ursuscamp for rust CTV playground

## Disclaimer

This is experimental software for testing and educational purposes. Do not use with real Bitcoin without thorough testing and understanding of the risks involved.
