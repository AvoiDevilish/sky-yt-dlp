package com.skyytdlp.poc

import android.app.Application
import android.util.Log
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import kotlin.concurrent.thread

/**
 * Initializes youtubedl-android and updates the bundled yt-dlp binary
 * in a background thread so network I/O never runs on the Android main thread.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            Log.i(TAG, "Initializing youtubedl-android...")

            YoutubeDL.getInstance().init(this)
			FFmpeg.getInstance().init(this)
			Log.i(TAG, "youtubedl-android and FFmpeg initialized successfully.")

            AppInit.isInitialized = true
            AppInit.initError = null

            thread(name = "yt-dlp-updater") {
                updateYoutubeDL()
            }

        } catch (e: YoutubeDLException) {
            Log.e(TAG, "youtubedl-android initialization failed", e)

            AppInit.isInitialized = false
            AppInit.initError = e.message ?: e.javaClass.simpleName

        } catch (e: Exception) {
            Log.e(TAG, "Unexpected initialization error", e)

            AppInit.isInitialized = false
            AppInit.initError = e.message ?: e.javaClass.simpleName
        }
    }

    private fun updateYoutubeDL() {
        try {
            Log.i(TAG, "Updating yt-dlp to the latest STABLE release...")

            YoutubeDL.getInstance().updateYoutubeDL(
                this,
                YoutubeDL.UpdateChannel.STABLE
            )

            Log.i(TAG, "yt-dlp update completed successfully.")

        } catch (e: YoutubeDLException) {
            Log.e(TAG, "yt-dlp update failed", e)

        } catch (e: Exception) {
            Log.e(TAG, "Unexpected yt-dlp update error", e)
        }
    }

    companion object {
        private const val TAG = "SkyYtDlpPoC"
    }
}
