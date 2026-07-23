package com.example

import android.app.Application
import com.example.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CricketApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@CricketApplication)
            modules(appModule)
        }
    }
}
