package com.dllewellyn.common.crypt

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class EncryptedSharedPrefsGenerator(private val context: Context) {

    fun createOrRetrieveKeyStorage() = createEncryptedSharedPrefs("encrypted_keys")

    fun createEncryptedSharedPrefs(filename: String) =
        EncryptedSharedPreferences.create(
            filename,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

}