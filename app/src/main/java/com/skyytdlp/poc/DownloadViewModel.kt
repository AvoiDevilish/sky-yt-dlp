package com.skyytdlp.poc

import android.app.Application
import android.os.Environment
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

    data class Running(
        val progress: Float,
        val etaSeconds: Long,
        val lastLine: String
    ) : DownloadUiState()

    data class Completed(
        val outputFile: String
    ) : DownloadUiState()

    data class Failed(
        val message: String
    ) : DownloadUiState()

    data object Cancelled : DownloadUiState()
}

class DownloadViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf<DownloadUiState>(DownloadUiState.Idle)
        private set

    private var activeProcessId: String? = null
    private var activeJob: Job? = null

    fun startDownload(url: String) {
        if (uiState is DownloadUiState.Running) return

        val cleanUrl = url.trim()

        if (cleanUrl.isBlank()) {
            uiState = DownloadUiState.Failed("URL is empty")
            return
        }

        if (!AppInit.isInitialized) {
            uiState = DownloadUiState.Failed(
                "youtubedl-android failed to initialize: " +
                    (AppInit.initError ?: "unknown error")
            )
            return
        }

        val processId = UUID.randomUUID().toString()
        activeProcessId = processId

        uiState = DownloadUiState.Running(
            progress = 0f,
            etaSeconds = 0L,
            lastLine = ""
        )

        activeJob = viewModelScope.launch(Dispatchers.IO) {

            val downloadsRoot = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )

            val outputDir = File(downloadsRoot, "Sky yt-dlp")

            if (!outputDir.exists() && !outputDir.mkdirs()) {
                publishFailure(
                    "Could not create download directory:\n${outputDir.absolutePath}"
                )
                activeProcessId = null
                return@launch
            }

            val filesBefore = outputDir
                .listFiles()
                ?.associateBy { it.absolutePath }
                ?: emptyMap()

            try {
                val request = YoutubeDLRequest(cleanUrl)

                request.addOption(
                    "-f",
                    "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best"
                )

                request.addOption(
                    "-o",
                    File(
                        outputDir,
                        "%(title)s.%(ext)s"
                    ).absolutePath
                )

                YoutubeDL.getInstance().execute(
                    request,
                    processId
                ) { progress, etaInSeconds, outputLine ->

                    viewModelScope.launch(Dispatchers.Main) {
                        if (uiState !is DownloadUiState.Cancelled) {
                            uiState = DownloadUiState.Running(
                                progress = progress,
                                etaSeconds = etaInSeconds,
                                lastLine = outputLine
                            )
                        }
                    }
                }

                val outputFile = findDownloadedMediaFile(
                    outputDir = outputDir,
                    filesBefore = filesBefore
                )

                if (outputFile != null) {
                    viewModelScope.launch(Dispatchers.Main) {
                        if (uiState !is DownloadUiState.Cancelled) {
                            uiState = DownloadUiState.Completed(
                                outputFile = outputFile.absolutePath
                            )
                        }
                    }
                } else {
                    publishFailure(
                        "Download completed, but the output file could not be located.\n" +
                            "Output directory:\n${outputDir.absolutePath}"
                    )
                }

            } catch (t: Throwable) {

                publishFailure(
                    t.message?.takeIf { it.isNotBlank() }
                        ?: t.javaClass.simpleName
                )

            } finally {
                activeProcessId = null
            }
        }
    }

    private fun findDownloadedMediaFile(
        outputDir: File,
        filesBefore: Map<String, File>
    ): File? {

        val mediaExtensions = setOf(
            "mp4",
            "mkv",
            "webm",
            "mov",
            "avi",
            "flv",
            "ts",
            "m4v",
            "3gp",
            "mp3",
            "m4a",
            "opus",
            "wav",
            "flac"
        )

        return outputDir
            .listFiles()
            ?.filter { file ->
                file.isFile &&
                    file.extension.lowercase() in mediaExtensions &&
                    (
                        !filesBefore.containsKey(file.absolutePath) ||
                            file.lastModified() >
                            filesBefore[file.absolutePath]!!.lastModified()
                    )
            }
            ?.maxByOrNull { it.lastModified() }
    }

    private fun publishFailure(message: String) {
        viewModelScope.launch(Dispatchers.Main) {
            if (uiState !is DownloadUiState.Cancelled) {
                uiState = DownloadUiState.Failed(message)
            }
        }
    }

    fun cancelDownload() {
        val processId = activeProcessId ?: return

        uiState = DownloadUiState.Cancelled
        activeProcessId = null

        YoutubeDL.getInstance().destroyProcessById(processId)

        activeJob?.cancel()
        activeJob = null
    }

    fun reset() {
        if (uiState !is DownloadUiState.Running) {
            uiState = DownloadUiState.Idle
        }
    }
}
