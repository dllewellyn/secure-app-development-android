package com.dllewellyn.location_lab

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val locationLabModule = module {

    viewModel {
        LocationLocationLocationViewModel(get())
    }
}