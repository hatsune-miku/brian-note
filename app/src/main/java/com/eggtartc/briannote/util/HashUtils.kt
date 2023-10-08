package com.eggtartc.briannote.util

object HashUtils {
    private fun hash(algorithm: String, input: String): String {
        val bytes = java.security.MessageDigest.getInstance(algorithm).digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun sha256(input: String): String {
        return hash("SHA-256", input)
    }

    fun md5(input: String): String {
        return hash("MD5", input)
    }
}
