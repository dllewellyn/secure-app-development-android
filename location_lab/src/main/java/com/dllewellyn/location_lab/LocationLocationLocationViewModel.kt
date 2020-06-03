@file:Suppress("FileUsage", "RoomDatabaseUsage", "ShardPreferencesUsage")

package com.dllewellyn.location_lab

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.dllewellyn.common.models.FileTypes
import com.dllewellyn.common.room.FilesDao
import com.dllewellyn.common.room.NotesDatabase
import com.dllewellyn.common.room.SecureNoteEntity
import com.dllewellyn.common.room.SingleFileEntity
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class LocationLocationLocationViewModel(
    private val context: Application,
    private val filesDao: FilesDao
) :
    AndroidViewModel(context) {

    val isPrivate = MutableLiveData<Int>()
    val fileName = MutableLiveData<String>()
    val files = MutableLiveData<List<SingleFileEntity>>()
    val typeSelected = MutableLiveData<FileTypes>()

    private val basePrivateDirectory: File by lazy {
        File(context.filesDir, LOCATION_LAB_DIRECTORY)
    }

    private val basePublicDirectory: File by lazy {
        File(context.getExternalFilesDir(null), LOCATION_LAB_DIRECTORY)
    }

    val buttonIsEnabled = MediatorLiveData<Boolean>().apply {
        value = false
    }

    init {
        configureIsEnabledButton()
        refreshFilesList()
    }

    private fun refreshFilesList() = viewModelScope.launch {
        files.postValue(filesDao.getAllForModule(LOCATION_LAB_DIRECTORY))
    }

    fun createButtonClicked() {
        when (isPrivate.value) {
            R.id.privateStorage -> when (requireNotNull(typeSelected.value)) {
                FileTypes.ROOM_DATABASE -> createPrivateRoomDb()
                FileTypes.FILE -> createPrivateStorageFile()
                FileTypes.SHARED_PREFERENCES -> createSharedPrefs() // Shared prefs can't really be public
            }
            R.id.publicStorage -> when (requireNotNull(typeSelected.value)) {
                FileTypes.ROOM_DATABASE -> createPublicRoomDb()
                FileTypes.FILE -> createPublicStorageFile()
                FileTypes.SHARED_PREFERENCES -> createSharedPrefs() // Shared prefs can't really be public
            }
            else -> throw IllegalStateException()
        }

        refreshFilesList()
    }

    private fun createPrivateRoomDb() {

        // You don't actually need to specify the whole path - you can just pass in the name
        createRoomDatabase(File(basePrivateDirectory, requireNotNull(fileName.value)).path)
    }

    private fun createPublicRoomDb() {
        createRoomDatabase(File(basePublicDirectory, requireNotNull(fileName.value)).path)
    }

    private fun createRoomDatabase(filename: String) = viewModelScope.launch {
        Room.databaseBuilder(
            context,
            NotesDatabase::class.java,
            filename
        ).build()
            .notesDao()
            .insert(SecureNoteEntity(noteText = "Hello world", uid = -1))

        filesDao.insert(
            SingleFileEntity(
                filename,
                FileTypes.ROOM_DATABASE.s,
                LOCATION_LAB_DIRECTORY,
                false
            )
        )

        refreshFilesList()
    }

    private fun createSharedPrefs() {
        context.getSharedPreferences(requireNotNull(fileName.value), Context.MODE_PRIVATE)
            .edit()
            .putString("Stored data", "Data")
            .apply()
    }

    private fun createPrivateStorageFile() {
        if (basePrivateDirectory.exists().not()) {
            basePrivateDirectory.mkdir()
        }

        createFile(File(basePrivateDirectory, requireNotNull(fileName.value)))
    }

    private fun createPublicStorageFile() {
        if (basePublicDirectory.exists().not()) {
            basePublicDirectory.mkdir()
        }

        createFile(File(basePublicDirectory, requireNotNull(fileName.value)))
    }

    private fun createFile(path: File) = viewModelScope.launch {
        if (path.exists()) {
            path.delete();
        }


        buildString {
            for (i in 0..Random.nextInt(512)) {
                append(Random.nextInt(256).toChar())
            }

            path.writeText(this.toString())
        }

        filesDao.insert(
            SingleFileEntity(
                path.path,
                FileTypes.FILE.s,
                LOCATION_LAB_DIRECTORY,
                false
            )
        )
        refreshFilesList()
    }

    private fun shouldEnableButton() {
        buttonIsEnabled.postValue(
            isPrivate.value != null && fileName.value != null
        )
    }

    private fun configureIsEnabledButton() {
        buttonIsEnabled.addSource(isPrivate) {
            shouldEnableButton()
        }

        buttonIsEnabled.addSource(fileName) {
            shouldEnableButton()
        }
    }

    fun nothingSelected() {
        typeSelected.postValue(null)
    }

    fun selectedItem(selectedType: FileTypes) {
        typeSelected.postValue(selectedType)
    }

    companion object {
        private const val LOCATION_LAB_DIRECTORY = "location_lab";
    }
}