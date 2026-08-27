package com.skyytdlp.poc

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

sealed class DownloadUiState {
    data object Idle : DownloadUiState()
    data class Running(val progress: Float, val etaSeconds: Long, val lastLine: String) : DownloadUiState()
    data class Completed(val outputDir: String) : DownloadUiState()
    data class Failed(val message: String) : DownloadUiState()
    data object Cancelled : DownloadUiState()
}

/**
 * Wraps youtubedl-android's execute()/destroyProcessById() calls with
 * Compose-observable state.
 *
 * *** CORRECTED IN MISSION 001-C — READ BEFORE TRUSTING THIS FILE BLINDLY ***
 *
 * Mission 001-B assumed a 2-argument progress callback `(Float, Long) ->
 * Unit` and the call shape `execute(request, callback, processId)`, based
 * on the library's README prose example. Re-checking against the
 * library's own real example source (DownloadingExampleActivity.java) and
 * an independent third-party summary of the same API surface surfaced a
 * conflict:
 *
 *   README prose:      execute(request, callback, processId)   2-arg callback
 *   Actual example src: execute(request, processId, callback)  3-arg callback
 *                       callback type: kotlin.jvm.functions.Function3
 *                       <Float, Long, String, Unit>  (progress, eta, outputLine)
 *
 * Two independent higher-fidelity sources (real source file + independent
 * summary) agree on the 3-arg/(request, processId, callback) shape, so
 * that is what's implemented below. This is INFERRED, not independently
 * compiled and confirmed — if Kotlin's compiler rejects this call shape,
 * swap the last two arguments back to (request, callback, processId) with
 * a 2-arg lambda as a fallback; the compiler's own type-mismatch error
 * will tell you unambiguously which one the installed AAR actually wants.
 */
class DownloadViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf<DownloadUiState>(DownloadUiState.Idle)
        private set

    private var activeProcessId: String? = null
    private var activeJob: Job? = null

    fun startDownload(url: String) {
        if (uiState is DownloadUiState.Running) return

        if (url.isBlank()) {
            uiState = DownloadUiState.Failed("URL is empty")
            return
        }

        if (!AppInit.isInitialized) {
            uiState = DownloadUiState.Failed(
                "youtubedl-android failed to initialize: ${AppInit.initError ?: "unknown error"}"
            )
            return
        }

        val processId = UUID.randomUUID().toString()
        activeProcessId = processId
        uiState = DownloadUiState.Running(progress = 0f, etaSeconds = 0L, lastLine = "")

        activeJob = viewModelScope.launch(Dispatchers.IO) {
            // App-specific external storage: no permission required on
            // modern Android, visible to the user via a file manager under
            // Android/data/com.skyytdlp.poc/files, removed on uninstall.
            val outputDir = getApplication<Application>().getExternalFilesDir(null)
                ?: getApplication<Application>().filesDir

            try {
                val request = YoutubeDLRequest(url.trim())
                request.addOption(
                    "-o",
                    File(outputDir, "%(title)s.%(ext)s").absolutePath
                )

                // Real yt-dlp execution — this call is synchronous/blocking,
                // hence Dispatchers.IO above. It is NOT simulated.
                //
                // Call shape per Mission 001-C correction: (request, processId, callback).
                // See class-level comment above if this fails to compile.
                YoutubeDL.getInstance().execute(request, processId) { progress, etaInSeconds, outputLine ->
                    viewModelScope.launch(Dispatchers.Main) {
                        uiState = DownloadUiState.Running(progress, etaInSeconds, outputLine)
                    }
                }

                viewModelScope.launch(Dispatchers.Main) {
                    if (uiState !is DownloadUiState.Cancelled) {
                        uiState = DownloadUiState.Completed(outputDir.absolutePath)
                    }
                }
            } catch (t: Throwable) {
                viewModelScope.launch(Dispatchers.Main) {
                    if (uiState !is DownloadUiState.Cancelled) {
                        uiState = DownloadUiState.Failed(t.message ?: t.javaClass.simpleName)
                    }
                }
            } finally {
                activeProcessId = null
            }
        }
    }

    fun cancelDownload() {
        val processId = activeProcessId ?: return
        uiState = DownloadUiState.Cancelled
        activeProcessId = null
        // VERIFIED via the library's README text (read earlier in this
        // investigation): destroyProcessById stops the in-flight yt-dlp
        // process associated with processId. Argument shape here is
        // unambiguous (single String), so it is not affected by the
        // execute() ordering conflict above.
        YoutubeDL.getInstance().destroyProcessById(processId)
        activeJob?.cancel()
    }

    fun reset() {
        if (uiState !is DownloadUiState.Running) {
            uiState = DownloadUiState.Idle
        }
    }
}
