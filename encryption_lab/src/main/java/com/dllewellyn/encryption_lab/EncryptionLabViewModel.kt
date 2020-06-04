package com.dllewellyn.encryption_lab

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dllewellyn.common.models.EncryptionType
import com.dllewellyn.common.models.FileTypes
import com.dllewellyn.common.room.FilesDao
import com.dllewellyn.common.room.SingleFileEntity
import kotlinx.coroutines.launch
import java.io.File

@Suppress("FileUsage")
class EncryptionLabViewModel(
    private val context: Application,
    private val useCase: EncryptionLabUseCase,
    private val filesDao: FilesDao
) :
    AndroidViewModel(context) {

    val fileName = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val passwordOrKeystore = MutableLiveData<Int>()
    val files = MutableLiveData<List<SingleFileEntity>>()
    val typeSelected = MutableLiveData<FileTypes>()

    val passwordFieldVisibility = MediatorLiveData<Int>().apply {
        value = View.GONE
    }

    val buttonIsEnabled = MediatorLiveData<Boolean>().apply {
        value = false
    }

    init {
        configureIsEnabledButton()
        refreshFilesList()
        configurePasswordFieldVisibility()
    }

    private fun configurePasswordFieldVisibility() {
        passwordFieldVisibility.addSource(passwordOrKeystore) {
            passwordFieldVisibility.postValue(
                when (it) {
                    R.id.passwordEncryption -> View.VISIBLE
                    R.id.keystoreEncryption -> View.GONE
                    else -> View.GONE
                }
            )
        }
    }

    private fun refreshFilesList() = viewModelScope.launch {
        files.postValue(filesDao.getAllForModule(EncryptionLabUseCase.ENCRYPTION_LAB_DIRECTORY))
    }

    fun createButtonClicked() = viewModelScope.launch {
        when (passwordOrKeystore.value) {
            R.id.passwordEncryption -> {

                when (typeSelected.value) {
                    FileTypes.ROOM_DATABASE -> generateEncryptedDatabaseWithPassword()

                    FileTypes.FILE -> createEncryptedFileWithPassword()
                    FileTypes.SHARED_PREFERENCES -> useCase.createEncryptedSharedPrefsAndPopulate(
                        requireNotNull(fileName.value)
                    )
                    null -> throw java.lang.IllegalArgumentException()
                }

            }

            R.id.keystoreEncryption -> {
                when (typeSelected.value) {
                    FileTypes.ROOM_DATABASE ->
                        createKeystoreEncryptedPassword()
                    FileTypes.FILE -> {
                        createEncryptedFile()
                    }
                    FileTypes.SHARED_PREFERENCES ->
                        useCase.createEncryptedSharedPrefsAndPopulate(
                            requireNotNull(fileName.value)
                        )
                    null -> throw IllegalArgumentException()
                }
            }
            else -> throw IllegalStateException()
        }

        refreshFilesList()
    }

    private fun createEncryptedFileWithPassword() = viewModelScope.launch {
        val fileName = useCase.createEncryptedFileWithPassword(
            requireNotNull(
                fileName.value
            ), requireNotNull(password.value)
        )

        filesDao.insert(
            SingleFileEntity(
                fileName,
                FileTypes.FILE.s,
                EncryptionLabUseCase.ENCRYPTION_LAB_DIRECTORY,
                true,
                EncryptionType.PASSWORD.s
            )
        )

        refreshFilesList()
    }

    private suspend fun createEncryptedFile() {
        val file = useCase.createEncryptedFileForKeystore(requireNotNull(fileName.value))
        filesDao.insert(
            SingleFileEntity(
                file,
                FileTypes.FILE.s,
                EncryptionLabUseCase.ENCRYPTION_LAB_DIRECTORY,
                true,
                EncryptionType.KEYSTORE.s
            )
        )


        refreshFilesList()
    }

    private suspend fun createKeystoreEncryptedPassword() {
        val filename = useCase.createEncryptedDatabase(
            File(useCase.basePrivateDirectory, requireNotNull(fileName.value)).path
        )

        filesDao.insert(
            SingleFileEntity(
                filename,
                FileTypes.ROOM_DATABASE.s,
                EncryptionLabUseCase.ENCRYPTION_LAB_DIRECTORY,
                true,
                EncryptionType.KEYSTORE.s
            )
        )

        refreshFilesList()
    }

    private suspend fun generateEncryptedDatabaseWithPassword() {
        useCase.createKeystoreUserPasswordRoomDatabase(
            requireNotNull(fileName.value),
            requireNotNull(password.value)
        )

        filesDao.insert(
            SingleFileEntity(
                File(useCase.basePrivateDirectory, requireNotNull(fileName.value)).path,
                FileTypes.ROOM_DATABASE.s,
                EncryptionLabUseCase.ENCRYPTION_LAB_DIRECTORY,
                true,
                EncryptionType.PASSWORD.s
            )
        )


        refreshFilesList()
    }

    private fun shouldEnableButton() {
        buttonIsEnabled.postValue(
            fileName.value != null
                    && passwordOrKeystore.value != null
                    && if (passwordOrKeystore.value == R.id.passwordEncryption) password.value != null else true
        )
    }

    private fun configureIsEnabledButton() {
        buttonIsEnabled.addSource(fileName) {
            shouldEnableButton()
        }

        buttonIsEnabled.addSource(passwordOrKeystore) {
            shouldEnableButton()
        }

        buttonIsEnabled.addSource(password) {
            shouldEnableButton()
        }
    }

    fun nothingSelected() {
        typeSelected.postValue(null)
    }

    fun selectedItem(selectedType: FileTypes) {
        typeSelected.postValue(selectedType)
    }

}