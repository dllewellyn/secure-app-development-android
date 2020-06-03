package com.dllewellyn.common

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dllewellyn.common.decryption.DecryptionUtil
import com.dllewellyn.common.models.EncryptionType
import com.dllewellyn.common.models.FileTypes
import com.dllewellyn.common.room.NotesDatabase
import com.dllewellyn.common.room.SingleFileEntity
import com.dllewellyn.common.utils.SingleLiveEvent
import info.guardianproject.iocipher.VirtualFileSystem
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.io.File

class ViewDataViewModel(val decryptionUtil: DecryptionUtil, val context: Application) :
    AndroidViewModel(context) {

    lateinit var singleFileEntity: SingleFileEntity

    val dataToShow = MutableLiveData<String>().apply {
        value = "Loading data"
    }

    val needPassword = SingleLiveEvent<Boolean>()

    fun loadWithEntity(entity: SingleFileEntity) = viewModelScope.launch {

        singleFileEntity = entity

        val data = if (entity.encrypted) {
            when (EncryptionType.stringToType(requireNotNull(entity.encryptionType))) {
                EncryptionType.PASSWORD -> when (FileTypes.stringToType(entity.type)) {
                    FileTypes.ROOM_DATABASE -> needPassword.call().let {
                        "Enter password"
                    }
                    FileTypes.FILE -> needPassword.call().let {
                        "Enter password"
                    }
                    FileTypes.SHARED_PREFERENCES -> "Shared preferences will not be shown"
                }
                EncryptionType.KEYSTORE -> when (FileTypes.stringToType(entity.type)) {
                    FileTypes.ROOM_DATABASE -> Room.databaseBuilder(
                        context,
                        NotesDatabase::class.java,
                        entity.path
                    ).build().run { readDatabaseToString(this) }

                    FileTypes.FILE -> decryptionUtil.keystoreDecryptFile(entity.path)
                    FileTypes.SHARED_PREFERENCES -> "Shared preferences will not be shown"
                }
            }
        } else {
            when (FileTypes.stringToType(entity.type)) {
                FileTypes.ROOM_DATABASE -> buildUnencryptedDatabase(entity).build()
                    .run { readDatabaseToString(this) }

                FileTypes.FILE -> File(entity.path).readText()
                FileTypes.SHARED_PREFERENCES -> "Shared preferences will not be shown"
            }
        }

        dataToShow.postValue(data)
    }

    private fun buildUnencryptedDatabase(entity: SingleFileEntity): RoomDatabase.Builder<NotesDatabase> {
        return Room.databaseBuilder(
            context,
            NotesDatabase::class.java,
            entity.path
        )
    }

    private fun buildEncryptedDatabaseWithPassword(filename: String, password: String) =
        Room.databaseBuilder(context, NotesDatabase::class.java, filename).openHelperFactory(
            SupportFactory(
                SQLiteDatabase.getBytes(password.toCharArray())
            )
        ).build()

    private suspend fun readDatabaseToString(database: NotesDatabase): String = database.notesDao()
        .getAll()
        .map { it.noteText }
        .joinToString(",")

    fun passwordEntered(password: String) =
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            needPassword.call()
            Log.e("Error", throwable.message, throwable)
        }) {
            val data: String = when (FileTypes.stringToType(singleFileEntity.type)) {

                FileTypes.ROOM_DATABASE -> buildEncryptedDatabaseWithPassword(
                    singleFileEntity.path,
                    requireNotNull(
                        decryptionUtil.generatePasswordWithPbkf(
                            password,
                            singleFileEntity.path
                        )
                    )
                ).run { readDatabaseToString(this) }
                FileTypes.FILE -> {
                    VirtualFileSystem.get().run {
                        mount(
                            singleFileEntity.path,
                            decryptionUtil.generatePasswordWithPbkf(password, singleFileEntity.path)
                        )
                        info.guardianproject.iocipher.File(singleFileEntity.path + "_").readText()
                            .also {
                                unmount()
                            }
                    }
                }
                FileTypes.SHARED_PREFERENCES -> "Shared Prefs not supported"
            }

            dataToShow.postValue(data)
        }
}