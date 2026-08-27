package com.skyytdlp.poc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    private val viewModel: DownloadViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SkyYtDlpPocScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun SkyYtDlpPocScreen(viewModel: DownloadViewModel) {
    // Vimeo test URL taken directly from youtubedl-android's own README
    // usage example. Whether this specific 14-year-old video ID still
    // resolves is UNKNOWN — NEEDS LOCAL VERIFICATION. If it's gone, replace
    // with any other yt-dlp-supported non-YouTube/Instagram/TikTok/X URL.
    var url by remember { mutableStateOf("https://vimeo.com/22439234") }
    val state = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Sky yt-dlp — Android PoC",
            style = MaterialTheme.typography.headlineSmall
        )

        if (!AppInit.isInitialized) {
            Text(
                text = "youtubedl-android did not initialize: ${AppInit.initError ?: "unknown error"}",
                color = MaterialTheme.colorScheme.error
            )
        }

        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Media URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = state !is DownloadUiState.Running
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { viewModel.startDownload(url) },
                enabled = state !is DownloadUiState.Running
            ) {
                Text("Download")
            }
            OutlinedButton(
                onClick = { viewModel.cancelDownload() },
                enabled = state is DownloadUiState.Running
            ) {
                Text("Cancel")
            }
        }

        when (state) {
            is DownloadUiState.Idle -> {
                Text("Status: idle")
            }
            is DownloadUiState.Running -> {
                Text("Status: downloading")
                LinearProgressIndicator(
                    progress = (state.progress / 100f).coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Progress: ${state.progress}%  ETA: ${state.etaSeconds}s")
                if (state.lastLine.isNotBlank()) {
                    Text("yt-dlp: ${state.lastLine}", style = MaterialTheme.typography.bodySmall)
                }
            }
            is DownloadUiState.Completed -> {
                Text("Status: completed")
                Text("Saved to folder: ${state.outputDir}")
            }
            is DownloadUiState.Failed -> {
                Text("Status: failed", color = MaterialTheme.colorScheme.error)
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
            is DownloadUiState.Cancelled -> {
                Text("Status: cancelled")
            }
        }
    }
}
