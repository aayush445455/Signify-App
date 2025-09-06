package com.signify.app.data.repository

/**
 * A stub repository for translating between text, speech, and sign language.
 * You can swap in real ML or network logic later.
 */
interface TranslatorRepository {
    /** Convert plain text into a sequence of sign “codes” or placeholder strings. */
    suspend fun textToSign(text: String): List<String>

    /** Convert spoken audio into sign “codes”. (Stub uses text input for now.) */
    suspend fun speechToSign(spoken: String): List<String>

    /** Convert sign “codes” (camera‐detected) into text. */
    suspend fun signToText(signCodes: List<String>): String
}
