package com.nhd.news.ui.components.richtexteditor

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration

@Stable
class RichTextState {
    var textFieldValue by mutableStateOf(TextFieldValue())
        private set
    
    var isBold by mutableStateOf(false)
        private set
    
    var isItalic by mutableStateOf(false)
        private set
    
    var isUnderline by mutableStateOf(false)
        private set
    
    var currentStyle by mutableStateOf(SpanStyle())
        private set
    
    // List of images with their positions in text
    private val _images = mutableListOf<ImageData>()
    val images: List<ImageData> get() = _images
    
    data class ImageData(
        val imageUrl: String,
        val position: Int,
        val id: String = java.util.UUID.randomUUID().toString()
    )
    
    fun updateText(newValue: TextFieldValue) {
        textFieldValue = newValue
        updateCurrentStyle()
    }
    
    fun toggleBold() {
        isBold = !isBold
        applyStyle()
    }
    
    fun toggleItalic() {
        isItalic = !isItalic
        applyStyle()
    }
    
    fun toggleUnderline() {
        isUnderline = !isUnderline
        applyStyle()
    }
    
    private fun applyStyle() {
        currentStyle = SpanStyle(
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
            textDecoration = if (isUnderline) TextDecoration.Underline else TextDecoration.None
        )
    }
    
    private fun updateCurrentStyle() {
        val selection = textFieldValue.selection
        if (selection.start == selection.end && selection.start > 0) {
            // Check style at cursor position
            val annotations = textFieldValue.annotatedString
            // Update toolbar state based on current position
            // This is simplified - in production you'd check actual spans
        }
    }
    
    fun insertImage(imageUrl: String) {
        val currentPosition = textFieldValue.selection.start
        val imageMarker = "\n[IMAGE:$imageUrl]\n"
        
        val newText = textFieldValue.text.substring(0, currentPosition) +
                imageMarker +
                textFieldValue.text.substring(currentPosition)
        
        _images.add(ImageData(imageUrl, currentPosition))
        
        textFieldValue = TextFieldValue(
            text = newText,
            selection = TextRange(currentPosition + imageMarker.length)
        )
    }
    
    fun removeImage(imageId: String) {
        _images.removeAll { it.id == imageId }
    }
    
    fun toHtml(): String {
        val text = textFieldValue.text
        var html = StringBuilder()
        
        // Convert text to HTML with formatting
        var currentIndex = 0
        var inBold = false
        var inItalic = false
        var inUnderline = false
        
        // Simple conversion - replace line breaks with <p> tags
        val paragraphs = text.split("\n\n")
        
        for (paragraph in paragraphs) {
            if (paragraph.trim().isEmpty()) continue
            
            // Check if it's an image marker
            if (paragraph.trim().startsWith("[IMAGE:") && paragraph.trim().endsWith("]")) {
                val imageUrl = paragraph.trim().substring(7, paragraph.trim().length - 1)
                html.append("<p><img src=\"$imageUrl\" alt=\"Uploaded image\" style=\"max-width: 100%; height: auto;\"></p>")
            } else {
                // Regular text paragraph
                html.append("<p>")
                
                // Replace single line breaks with <br>
                val lines = paragraph.split("\n")
                for ((index, line) in lines.withIndex()) {
                    html.append(line.trim())
                    if (index < lines.size - 1) {
                        html.append("<br>")
                    }
                }
                
                html.append("</p>")
            }
        }
        
        return html.toString()
    }
    
    fun clear() {
        textFieldValue = TextFieldValue()
        isBold = false
        isItalic = false
        isUnderline = false
        currentStyle = SpanStyle()
        _images.clear()
    }
}

