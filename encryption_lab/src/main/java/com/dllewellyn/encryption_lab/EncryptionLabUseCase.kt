package com.dllewellyn.encryption_lab

import android.content.Context
import androidx.room.Room
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.dllewellyn.common.crypt.EncryptedSharedPrefsGenerator
import com.dllewellyn.common.crypt.SecretKeyGenerator
import com.dllewellyn.common.room.NotesDatabase
import com.dllewellyn.common.room.SecureNoteEntity
import info.guardianproject.iocipher.VirtualFileSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.io.File
import java.nio.charset.Charset

@Suppress("FileUsage")
class EncryptionLabUseCase(
    private val context: Context,
    private val encryptedSharedPrefsGenerator: EncryptedSharedPrefsGenerator = EncryptedSharedPrefsGenerator(
        context
    ),
    private val secretKeyGenerator: SecretKeyGenerator = SecretKeyGenerator(
        encryptedSharedPrefsGenerator
    )
) {
    val basePrivateDirectory: File by lazy {
        File(context.filesDir, ENCRYPTION_LAB_DIRECTORY)
    }

    // Room database
    suspend fun createKeystoreUserPasswordRoomDatabase(filename: String, password: String) {
        createEncryptedDatabase(
            filename,
            requireNotNull(
                secretKeyGenerator.generatePasswordWithPbkf(
                    password,
                    filename
                )
            )
        )
    }

    suspend fun createEncryptedDatabase(filename: String): String {
        val generatedPassword = secretKeyGenerator.retrieveOrGeneratePasswordForFile(filename)
        return createEncryptedDatabase(generatedPassword)
    }

    private suspend fun createEncryptedDatabase(filename: String, password: String): String {

        if (File(filename).exists()) {
            File(filename).delete()
        }

        Room.databaseBuilder(
            context,
            NotesDatabase::class.java,
            filename
        ).openHelperFactory(
            SupportFactory(
                SQLiteDatabase.getBytes(password.toCharArray())
            )
        ).build().notesDao().insert(SecureNoteEntity(noteText = "Hello world", uid = -1))

        return filename
    }


    // Encrypted files

    suspend fun createEncryptedFileForKeystore(filename: String): String =
        withContext(Dispatchers.IO) {
            if (basePrivateDirectory.exists().not()) {
                basePrivateDirectory.mkdir()
            }

            val file = File(basePrivateDirectory, filename)

            if (file.exists()) {
                file.delete()
            }

            EncryptedFile.Builder(
                file,
                context,
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
                .openFileOutput()
                .writer(Charset.defaultCharset()).apply {
                    write("Test string")
                    close()
                }

            file.path
        }


    fun createEncryptedFileWithPassword(filename: String, password: String): String {

        val file = File(basePrivateDirectory, filename)

        if (file.exists()) file.delete()

        val generatedPassword = secretKeyGenerator.generatePasswordWithPbkf(password, filename)

        VirtualFileSystem.get().apply {
            createNewContainer(file.path, generatedPassword)
            mount(file.path, generatedPassword)
            info.guardianproject.iocipher.File(file.path + "_").writeText("Hello world")
            unmount()
        }

        return file.path
    }


    // Shared preferences
    fun createEncryptedSharedPrefsAndPopulate(filename: String) {
        createEncryptedSharedPrefs(filename)
            .edit()
            .putString("Stored data", "Data")
            .apply()
    }


    private fun createEncryptedSharedPrefs(filename: String) =
        encryptedSharedPrefsGenerator.createEncryptedSharedPrefs(filename)




    companion object {
        const val ENCRYPTION_LAB_DIRECTORY = "encryption_lab";
    }

}