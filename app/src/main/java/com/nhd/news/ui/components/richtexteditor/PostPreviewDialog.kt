package com.nhd.news.ui.components.richtexteditor

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostPreviewDialog(
    title: String,
    htmlContent: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Xem trước bài viết") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Đóng")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Tiêu đề",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Content section with WebView
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Nội dung",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // WebView to render HTML
                        HtmlContentView(
                            htmlContent = htmlContent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(500.dp)
                        )
                    }
                }
                
                // Info message
                Text(
                    text = "Đây là bản xem trước. Bài viết thực tế có thể khác một chút.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun HtmlContentView(
    htmlContent: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = false // Disable JS for security
                    builtInZoomControls = false
                    displayZoomControls = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                }
                
                // Create full HTML page with styling
                val fullHtml = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            body {
                                font-family: 'Roboto', sans-serif;
                                font-size: 16px;
                                line-height: 1.6;
                                color: #333;
                                padding: 16px;
                                margin: 0;
                                background-color: #fff;
                            }
                            p {
                                margin: 0 0 16px 0;
                                text-align: justify;
                            }
                            img {
                                max-width: 100%;
                                height: auto;
                                display: block;
                                margin: 16px 0;
                                border-radius: 8px;
                            }
                            strong, b {
                                font-weight: bold;
                            }
                            em, i {
                                font-style: italic;
                            }
                            u {
                                text-decoration: underline;
                            }
                            br {
                                display: block;
                                content: "";
                                margin: 8px 0;
                            }
                        </style>
                    </head>
                    <body>
                        $htmlContent
                    </body>
                    </html>
                """.trimIndent()
                
                loadDataWithBaseURL(null, fullHtml, "text/html", "UTF-8", null)
            }
        },
        modifier = modifier
    )
}

