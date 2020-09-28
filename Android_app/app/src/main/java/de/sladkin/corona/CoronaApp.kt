package de.sladkin.corona

import android.app.Application
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CoronaApp : Application() {

    companion object {
        const val MAXIMUM_ALLOWED_PEOPLE_NEARBY = 1
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@CoronaApp)
            modules(mainModule)
        }
    }
}