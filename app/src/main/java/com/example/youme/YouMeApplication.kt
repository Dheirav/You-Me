package com.example.youme

import android.app.Application
import com.google.firebase.FirebaseApp

class YouMeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
