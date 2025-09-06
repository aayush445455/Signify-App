// SignifyApplication.kt
package com.signify.app

import android.app.Application
import com.signify.app.di.AppContainer

class SignifyApplication : Application() {

    /** Our one‐stop shop for DAOs, Repos, etc. */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // Initialize your container here
        container = AppContainer(this)
    }
}
