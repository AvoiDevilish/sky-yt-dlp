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
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    SkyYtDlpPocScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun SkyYtDlpPocScreen(viewModel: DownloadViewModel) {

    var url by remember {
        mutableStateOf("")
    }

    val state = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "UOG Downloader",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Local video archive downloader",
            style = MaterialTheme.typography.bodyMedium
        )

        if (!AppInit.isInitialized) {
            Text(
                text = "youtubedl-android did not initialize: " +
                    (AppInit.initError ?: "unknown error"),
                color = MaterialTheme.colorScheme.error
            )
        }

        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = {
                Text("Media URL")
            },
            placeholder = {
                Text("https://...")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = state !is DownloadUiState.Running
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Button(
                onClick = {
                    viewModel.startDownload(url)
                },
                enabled = state !is DownloadUiState.Running &&
                    url.isNotBlank()
            ) {
                Text("Download")
            }

            OutlinedButton(
                onClick = {
                    viewModel.cancelDownload()
                },
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
                    progress = {
                        (state.progress / 100f)
                            .coerceIn(0f, 1f)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Progress: ${state.progress}%  " +
                        "ETA: ${state.etaSeconds}s"
                )

                if (state.lastLine.isNotBlank()) {
                    Text(
                        text = "yt-dlp: ${state.lastLine}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            is DownloadUiState.Completed -> {

                Text(
                    text = "Status: completed",
                    color = MaterialTheme.colorScheme.primary
                )

                Text("Saved file:")

                Text(
                    text = state.outputFile,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            is DownloadUiState.Failed -> {

                Text(
                    text = "Status: failed",
                    color = MaterialTheme.colorScheme.error
                )

                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            is DownloadUiState.Cancelled -> {

                Text("Status: cancelled")

                OutlinedButton(
                    onClick = {
                        viewModel.reset()
                    }
                ) {
                    Text("Reset")
                }
            }
        }
    }
}
