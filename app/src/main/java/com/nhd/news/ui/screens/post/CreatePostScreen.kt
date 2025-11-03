package com.nhd.news.ui.screens.post

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhd.news.data.api.ApiConfig
import com.nhd.news.ui.components.richtexteditor.RichTextEditor
import com.nhd.news.ui.components.richtexteditor.rememberRichTextState
import com.nhd.news.ui.components.richtexteditor.PostPreviewDialog
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    viewModel: PostViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Int?>(null) }
    var expandedCategory by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    
    val richTextState = rememberRichTextState()
    
    val categories = listOf(
        1 to "Bóng đá",
        2 to "Bóng rổ",
        3 to "Quần vợt",
        4 to "Các môn khác"
    )
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                // Copy URI to file
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileName = "image_${System.currentTimeMillis()}.jpg"
                val tempFile = File(context.cacheDir, fileName)
                
                inputStream?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Upload image
                viewModel.uploadImage(
                    tempFile,
                    onSuccess = { imageUrl ->
                        // Convert relative URL to absolute
                        val baseUrl = ApiConfig.BASE_URL.substringBeforeLast("api/")
                        val absoluteUrl = if (imageUrl.startsWith("http")) {
                            imageUrl
                        } else {
                            baseUrl + imageUrl.trimStart('/')
                        }
                        
                        // Insert image into editor
                        richTextState.insertImage(absoluteUrl)
                        
                        // Clean up
                        tempFile.delete()
                    },
                    onError = { error ->
                        // Error will be shown via uiState
                        tempFile.delete()
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            richTextState.clear()
            title = ""
            selectedCategory = null
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo bài viết") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Quay lại")
                    }
                }
            )
        },
        snackbarHost = {
            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Đóng")
                        }
                    }
                ) {
                    Text(uiState.error ?: "")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Lưu ý",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Bài viết sẽ được kiểm duyệt trước khi công khai. Sử dụng toolbar để format text và thêm ảnh.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tiêu đề") },
                placeholder = { Text("Nhập tiêu đề bài viết") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Category
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory }
            ) {
                OutlinedTextField(
                    value = categories.find { it.first == selectedCategory }?.second ?: "Chọn chuyên mục",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Chuyên mục") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = !uiState.isLoading
                )
                
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { (id, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                selectedCategory = id
                                expandedCategory = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Rich Text Editor
            Text(
                text = "Nội dung",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            RichTextEditor(
                state = richTextState,
                onImageUploadRequest = {
                    imagePickerLauncher.launch("image/*")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Loading indicator for image upload
            if (uiState.isUploadingImage) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Đang upload ảnh...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Preview button
                OutlinedButton(
                    onClick = { 
                        if (title.isNotBlank() && richTextState.textFieldValue.text.isNotBlank()) {
                            showPreview = true
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    enabled = title.isNotBlank() && 
                             richTextState.textFieldValue.text.isNotBlank() && 
                             !uiState.isLoading
                ) {
                    Icon(Icons.Default.Info, "Xem trước")
                    Spacer(Modifier.width(8.dp))
                    Text("Xem trước", style = MaterialTheme.typography.titleSmall)
                }
                
                // Submit button
                Button(
                    onClick = {
                        val content = richTextState.toHtml()
                        if (title.isNotBlank() && selectedCategory != null && content.isNotBlank()) {
                            viewModel.createPost(
                                title = title,
                                content = content,
                                categoryId = selectedCategory!!
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    enabled = title.isNotBlank() && selectedCategory != null && 
                             richTextState.textFieldValue.text.isNotBlank() && 
                             !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Send, "Đăng bài")
                        Spacer(Modifier.width(8.dp))
                        Text("Đăng bài", style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    
    // Preview dialog
    if (showPreview) {
        PostPreviewDialog(
            title = title,
            htmlContent = richTextState.toHtml(),
            onDismiss = { showPreview = false }
        )
    }
}
