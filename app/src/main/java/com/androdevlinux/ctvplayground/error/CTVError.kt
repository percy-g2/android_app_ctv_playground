package com.androdevlinux.ctvplayground.error

sealed class CTVError : Exception() {
    data class UnknownError(override val message: String) : CTVError()
    object MissingSequence : CTVError()
    data class BitcoinError(override val message: String) : CTVError()
    data class ScriptError(override val message: String) : CTVError()
    data class ValidationError(override val message: String) : CTVError()
}