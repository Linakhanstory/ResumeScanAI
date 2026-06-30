package com.example.resumescanai

import android.app.Application
import com.cloudinary.android.MediaManager
import com.google.firebase.auth.FirebaseAuth

class ResumeScanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Cloudinary
        val config = mapOf(
            "cloud_name" to "dt4unxor3"
        )
        
        try {
            MediaManager.init(this, config)
        } catch (e: Exception) {
            // Already initialized
        }

        // Sign in anonymously for Firestore access
        FirebaseAuth.getInstance().signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    android.util.Log.d("ResumeScanAI", "Anonymous auth successful")
                } else {
                    android.util.Log.e("ResumeScanAI", "Anonymous auth failed", task.exception)
                }
            }
    }
}