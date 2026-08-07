package com.example

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.di.appModule
import com.example.worker.MatchUpdateWorker
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class CricketApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@CricketApplication)
            modules(appModule)
        }

        val workRequest = PeriodicWorkRequestBuilder<MatchUpdateWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MatchUpdatePeriodic",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
