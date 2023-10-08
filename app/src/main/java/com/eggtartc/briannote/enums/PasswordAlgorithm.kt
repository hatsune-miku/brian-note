package com.eggtartc.briannote.enums

enum class PasswordAlgorithm(val rawValue: String) {
    MD5("MD5"), SHA256("SHA-256");

    companion object {
        fun fromRawValue(rawValue: String): PasswordAlgorithm {
            return when (rawValue) {
                "MD5" -> MD5
                "SHA-256" -> SHA256
                else -> throw IllegalArgumentException("Unknown password algorithm: $rawValue")
            }
        }
    }
}
