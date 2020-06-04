package com.dllewellyn.common.crypt

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class SecretKeyGenerator(private val encryptedSharedPreferencesGenerator: EncryptedSharedPrefsGenerator) {


    fun retrieveOrGeneratePasswordForFile(filePath: String, length: Int = 256): String {
        encryptedSharedPreferencesGenerator.createOrRetrieveKeyStorage()
            .apply {
                return if (contains(filePath)) {
                    requireNotNull(getString(filePath, null))
                } else {
                    generateSafeToken(length).apply {
                        edit()
                            .putString(filePath, this)
                            .apply()
                    }
                }
            }
    }

    fun generatePasswordWithPbkf(password: String, filepath: String): String? {
        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return Base64.encodeToString(
            keyFactory.generateSecret(
                PBEKeySpec(
                    password.toCharArray(),
                    retrieveOrGeneratePasswordForFile(filepath).toByteArray(),
                    20000,
                    256
                )
            ).encoded, 0
        )
    }

    private fun generateSafeToken(length: Int): String {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, 0)
    }
}