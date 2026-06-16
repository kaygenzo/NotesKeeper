package com.telen.noteskeeper

import android.app.Application
import android.util.Log
import androidx.room.Room
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.telen.noteskeeper.data.local.db.AppDatabase
import com.telen.noteskeeper.data.local.file.PhotoFileStorage
import com.telen.noteskeeper.di.coreModule
import com.telen.noteskeeper.di.networkModule
import com.telen.noteskeeper.di.repositoryModule
import com.telen.noteskeeper.di.useCaseModule
import com.telen.noteskeeper.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber

class TestApp : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Timber.plant(Timber.DebugTree())
        startKoin {
            androidContext(this@TestApp)
            modules(
                coreModule,
                testDatabaseModule,
                networkModule,
                repositoryModule,
                useCaseModule,
                viewModelModule,
            )
        }
        // Periodic WorkManager cleanup job is intentionally omitted in tests.
    }
}

private val testDatabaseModule = module {
    single {
        Room.inMemoryDatabaseBuilder(androidContext(), AppDatabase::class.java).build()
    }
    single { get<AppDatabase>().noteDao() }
    single { get<AppDatabase>().subNoteDao() }
    single { get<AppDatabase>().photoDao() }
    single { PhotoFileStorage(androidContext()) }
}
