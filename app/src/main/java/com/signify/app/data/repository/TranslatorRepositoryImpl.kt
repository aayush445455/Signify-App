package com.signify.app.data.repository

import kotlinx.coroutines.delay

class TranslatorRepositoryImpl : TranslatorRepository {

    override suspend fun textToSign(text: String): List<String> {
        delay(300) // simulate work
        // stub: return each word prefixed with “SIGN-”
        return text
            .split("\\s+".toRegex())
            .map { word -> "SIGN-$word" }
    }

    override suspend fun speechToSign(spoken: String): List<String> {
        delay(300)
        // stub: mirror textToSign
        return textToSign(spoken)
    }

    override suspend fun signToText(signCodes: List<String>): String {
        delay(300)
        // stub: strip “SIGN-” prefix and join
        return signCodes.joinToString(" ") { code ->
            code.removePrefix("SIGN-")
        }
    }
}
