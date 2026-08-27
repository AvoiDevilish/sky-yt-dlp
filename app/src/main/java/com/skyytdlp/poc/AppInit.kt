package com.skyytdlp.poc

/**
 * Tracks whether youtubedl-android successfully initialized. Read by the UI
 * so a failed init produces a visible error instead of a silent crash on
 * first download attempt.
 */
object AppInit {
    @Volatile
    var isInitialized: Boolean = false
        internal set

    @Volatile
    var initError: String? = null
        internal set
}
