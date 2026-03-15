package com.weatherapp.model

import java.security.MessageDigest

/**
 * Serial-key activation for premium.
 * Valid codes are stored as SHA-256 hashes — the codes themselves are not in source.
 * To add a new code: echo -n "YOUR-CODE" | sha256sum
 */
object PremiumActivation {

    private val VALID_HASHES = setOf(
        "8a1e442e50ab1f564f6fb3531ef33593b09d48923cb0a3596801ab6e76d1b893",
        "cfca761e055d4c521fa68f3ce5768451891df42be13d4ca2ae4f1c391ffa9c66",
        "bb82ac9daad7d413af8eba52bdf9241b05c6b84805e0ee2635d92b9e13c2fdff",
        "c4bb3d815bf2917464a0efab04c572c2ea57dd34d3d1b39200bae51a5b39985e"
    )

    /** Returns true if the entered code is valid. Case-insensitive, trims whitespace. */
    fun isValidCode(input: String): Boolean {
        val normalised = input.trim().uppercase()
        val hash = sha256(normalised)
        return hash in VALID_HASHES
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
