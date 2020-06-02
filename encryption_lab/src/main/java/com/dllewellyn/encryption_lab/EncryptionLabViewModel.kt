package com.dllewellyn.encryption_lab

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dllewellyn.common.models.FileTypes
import kotlinx.coroutines.launch
import java.io.File

class EncryptionLabViewModel(
    private val context: Application,
    private val useCase: EncryptionLabUseCase
) :
    AndroidViewModel(context) {

    val fileName = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val passwordOrKeystore = MutableLiveData<Int>()
    val files = MutableLiveData<List<File>>()
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
        val privs = useCase.basePrivateDirectory.listFiles()?.toList() ?: listOf()
        files.postValue(privs)
    }

    fun createButtonClicked() = viewModelScope.launch {
        when (passwordOrKeystore.value) {
            R.id.passwordEncryption -> {

                when (typeSelected.value) {
                    FileTypes.ROOM_DATABASE -> generateEncryptedDatabaseWithPassword()

                    FileTypes.FILE -> TODO()
                    FileTypes.SHARED_PREFERENCES -> TODO()
                    null -> throw java.lang.IllegalArgumentException()
                }

            }

            R.id.keystoreEncryption -> {
                when (typeSelected.value) {
                    FileTypes.ROOM_DATABASE ->
                        useCase.createKeystoreEncryptedRoomDatabase(
                            File(useCase.basePrivateDirectory, requireNotNull(fileName.value)).path,
                            useCase.retrieveOrGeneratePasswordForFile(
                                File(
                                    useCase.basePrivateDirectory,
                                    fileName.value
                                )
                            )
                        )

                    FileTypes.FILE -> {
                        useCase.createEncryptedFile(requireNotNull(fileName.value))
                    }
                    FileTypes.SHARED_PREFERENCES -> {
                        useCase.createEncryptedSharedPrefsAndPopulate(
                            requireNotNull(fileName.value)
                        )

                    }
                    null -> throw IllegalArgumentException()
                }
            }
            else -> throw IllegalStateException()
        }

        refreshFilesList()
    }

    private suspend fun generateEncryptedDatabaseWithPassword() {
        useCase.createKeystoreUserPasswordRoomDatabase(
            requireNotNull(fileName.value),
            requireNotNull(password.value)
        )
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