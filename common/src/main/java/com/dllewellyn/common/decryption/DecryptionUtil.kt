package com.dllewellyn.common.decryption

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.File
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class DecryptionUtil(val context: Context) {

    fun keystoreDecryptFile(filename: String): String =
        EncryptedFile.Builder(
            File(filename),
            context,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
            .openFileInput()
            .reader()
            .readText()

    fun generatePasswordWithPbkf(password: String, filepath: String): String? {
        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return Base64.encodeToString(
            keyFactory.generateSecret(
                PBEKeySpec(
                    password.toCharArray(),
                    retrieveOrGeneratePasswordForFile(File(filepath)).toByteArray(),
                    20000,
                    256
                )
            ).encoded, 0
        )
    }

    private fun retrieveOrGeneratePasswordForFile(filePath: File): String {
        createEncryptedSharedPrefs("encrypted_keys")
            .apply {
                return if (contains(filePath.path)) {
                    requireNotNull(getString(filePath.path, null))
                } else {
                    throw IllegalStateException()
                }
            }
    }

    private fun createEncryptedSharedPrefs(filename: String) =
        EncryptedSharedPreferences.create(
            filename,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
}