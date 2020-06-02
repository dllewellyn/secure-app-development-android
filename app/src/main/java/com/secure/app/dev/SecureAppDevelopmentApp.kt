package com.secure.app.dev

import android.app.Application
import com.dllewellyn.encryption_lab.encryptionLab
import com.dllewellyn.location_lab.locationLabModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SecureAppDevelopmentApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@SecureAppDevelopmentApp)
            modules(
                locationLabModule + encryptionLab
            )
        }
    }
}