package com.telen.noteskeeper

import android.app.Application
import android.util.Log
import androidx.work.Configuration

class TestApplication : Application(), Configuration.Provider {
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
}
