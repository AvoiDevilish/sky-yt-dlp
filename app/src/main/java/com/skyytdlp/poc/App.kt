package com.skyytdlp.poc

import android.app.Application
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException

/**
 * Initializes youtubedl-android once, at process start, per the library's
 * own documented usage ("preferably in onCreate").
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            YoutubeDL.getInstance().init(this)
            AppInit.isInitialized = true
        } catch (e: YoutubeDLException) {
            Log.e(TAG, "youtubedl-android init failed", e)
            AppInit.isInitialized = false
            AppInit.initError = e.message ?: e.javaClass.simpleName
        }
    }

    companion object {
        private const val TAG = "SkyYtDlpPoC"
    }
}
