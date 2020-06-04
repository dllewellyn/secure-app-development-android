@file:Suppress("RoomDatabaseUsage")

package com.dllewellyn.common.room

import androidx.room.Room
import com.dllewellyn.common.ViewDataViewModel
import com.dllewellyn.common.crypt.DecryptionUtil
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val filesModule = module {
    single {
        Room.databaseBuilder(
            get(),
            FilesDatabase::class.java,
            "files-database.db"
        ).build()
            .fileDao()
    }
}

val dataScreenModule = module {
    viewModel { ViewDataViewModel(DecryptionUtil(get()), get()) }
}