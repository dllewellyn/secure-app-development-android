package com.dllewellyn.encryption_lab

import android.content.Context
import android.util.Base64
import androidx.room.Room
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.dllewellyn.common.room.NotesDatabase
import com.dllewellyn.common.room.SecureNoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.io.File
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class EncryptionLabUseCase(private val context: Context) {
    val basePrivateDirectory: File by lazy {
        File(context.filesDir, ENCRYPTION_LAB_DIRECTORY)
    }

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

    fun retrieveOrGeneratePasswordForFile(filePath: File, length: Int = 256): String {
        createEncryptedSharedPrefs("encrypted_keys")
            .apply {
                return if (contains(filePath.path)) {
                    requireNotNull(getString(filePath.path, null))
                } else {
                    generateSafeToken(length).apply {
                        edit()
                            .putString(filePath.path, this)
                            .apply()
                    }
                }
            }
    }

    suspend fun createKeystoreUserPasswordRoomDatabase(filename: String, password: String) {
        createKeystoreEncryptedRoomDatabase(
            filename,
            requireNotNull(
                generatePasswordWithPbkf(
                    password,
                    filename
                )
            )
        )
    }

    suspend fun createKeystoreEncryptedRoomDatabase(filename: String, password: String) =
        Room.databaseBuilder(context, NotesDatabase::class.java, filename).openHelperFactory(
            SupportFactory(
                SQLiteDatabase.getBytes(password.toCharArray())
            )
        )
            .build()
            .notesDao()
            .insert(SecureNoteEntity(noteText = "Hello world", uid = -1))

    fun createEncryptedSharedPrefsAndPopulate(filename: String) {
        createEncryptedSharedPrefs(filename)
            .edit()
            .putString("Stored data", "Data")
            .apply()
    }


    suspend fun createEncryptedFile(filename: String) = withContext(Dispatchers.IO) {
        if (basePrivateDirectory.exists().not()) {
            basePrivateDirectory.mkdir()
        }

        createEncryptedFile(
            EncryptedFile.Builder(
                File(basePrivateDirectory, filename),
                context,
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
        )
    }

    private fun createEncryptedSharedPrefs(filename: String) =
        EncryptedSharedPreferences.create(
            filename,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )


    private suspend fun createEncryptedFile(path: EncryptedFile) = withContext(Dispatchers.IO) {
        path.openFileOutput().use {
            it.writer().write("Test string")
        }
    }


    private fun generateSafeToken(length: Int): String {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, 0)
    }

    companion object {
        private const val ENCRYPTION_LAB_DIRECTORY = "encryption_lab";
    }

}