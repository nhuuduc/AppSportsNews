package com.nhd.news.ui.components.richtexteditor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ImagePickerDialog(
    onDismiss: () -> Unit,
    onImageSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        if (uri != null) {
            onImageSelected()
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm ảnh") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Chọn nguồn ảnh:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Gallery option
                Button(
                    onClick = {
                        imagePickerLauncher.launch("image/*")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📁 Chọn từ thư viện")
                }
                
                // Camera option (for future implementation)
                OutlinedButton(
                    onClick = { /* TODO: Implement camera */ },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                ) {
                    Text("📷 Chụp ảnh")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}
