package com.signify.app

import android.app.Application
import androidx.room.Room
import com.signify.app.data.local.AppDatabase

class SignifyApplication : Application() {
    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "signify_db"
        ).build()
    }
}
