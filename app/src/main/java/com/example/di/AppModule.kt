package com.example.di

import com.example.data.AppDatabase
import com.example.data.CricketRepository
import com.example.data.OnboardingManager
import com.example.viewmodel.CricketViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.getDatabase(androidContext()) }
    single { CricketRepository(androidContext()) }
    single { OnboardingManager(androidContext()) }
    
    viewModel { CricketViewModel(get(), get()) }
}
