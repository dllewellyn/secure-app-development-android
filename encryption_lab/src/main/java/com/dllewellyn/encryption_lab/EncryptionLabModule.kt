package com.dllewellyn.encryption_lab

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val encryptionLab =  module {
    viewModel {
        EncryptionLabViewModel(get(), EncryptionLabUseCase(get()), get())
    }
}