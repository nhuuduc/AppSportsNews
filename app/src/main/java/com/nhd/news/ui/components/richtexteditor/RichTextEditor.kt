package com.nhd.news.ui.components.richtexteditor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun RichTextEditor(
    state: RichTextState,
    onImageUploadRequest: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Nhập nội dung bài viết...",
    minHeight: Int = 300,
    enabled: Boolean = true
) {
    var showImagePicker by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        // Toolbar
        RichTextToolbar(
            state = state,
            onImageClick = { showImagePicker = true }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Text input area
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Display images inline
                val textWithImages = buildAnnotatedContent(state)
                
                TextField(
                    value = state.textFieldValue,
                    onValueChange = { state.updateText(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = minHeight.dp),
                    placeholder = { Text(placeholder) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    enabled = enabled,
                    maxLines = 15
                )
                
                // Display uploaded images
                state.images.forEach { imageData ->
                    Spacer(modifier = Modifier.height(8.dp))
                    ImagePreview(
                        imageUrl = imageData.imageUrl,
                        onRemove = { state.removeImage(imageData.id) }
                    )
                }
            }
        }
        
        // Character count
        Text(
            text = "${state.textFieldValue.text.length} ký tự",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )
    }
    
    // Image picker dialog
    if (showImagePicker) {
        ImagePickerDialog(
            onDismiss = { showImagePicker = false },
            onImageSelected = { 
                showImagePicker = false
                onImageUploadRequest()
            }
        )
    }
}

@Composable
private fun ImagePreview(
    imageUrl: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Uploaded image",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onRemove) {
                    Text("Xóa")
                }
            }
        }
    }
}

@Composable
private fun buildAnnotatedContent(state: RichTextState): String {
    // Build text content with image markers
    val text = state.textFieldValue.text
    val images = state.images
    
    if (images.isEmpty()) return text
    
    val result = StringBuilder()
    var lastIndex = 0
    
    for (image in images.sortedBy { it.position }) {
        if (image.position > lastIndex) {
            result.append(text.substring(lastIndex, minOf(image.position, text.length)))
        }
        result.append("\n[Ảnh đã tải lên]\n")
        lastIndex = image.position
    }
    
    if (lastIndex < text.length) {
        result.append(text.substring(lastIndex))
    }
    
    return result.toString()
}

@Composable
fun rememberRichTextState(): RichTextState {
    return remember { RichTextState() }
}

