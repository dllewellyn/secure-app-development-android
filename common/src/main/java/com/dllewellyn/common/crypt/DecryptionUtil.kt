package com.dllewellyn.common.crypt

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import java.io.File

class DecryptionUtil(
    val context: Context
) {

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
}