package com.nhd.news.ui.components.richtexteditor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RichTextToolbar(
    state: RichTextState,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bold
            TextToolbarButton(
                text = "B",
                contentDescription = "Bold",
                isSelected = state.isBold,
                onClick = { state.toggleBold() },
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            // Italic
            TextToolbarButton(
                text = "I",
                contentDescription = "Italic",
                isSelected = state.isItalic,
                onClick = { state.toggleItalic() },
                fontStyle = FontStyle.Italic
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            // Underline
            TextToolbarButton(
                text = "U",
                contentDescription = "Underline",
                isSelected = state.isUnderline,
                onClick = { state.toggleUnderline() },
                textDecoration = TextDecoration.Underline
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            VerticalDivider(
                modifier = Modifier.height(24.dp),
                color = MaterialTheme.colorScheme.outline
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Image
            TextToolbarButton(
                text = "📷",
                contentDescription = "Insert Image",
                isSelected = false,
                onClick = onImageClick
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            // Link (placeholder for future)
            TextToolbarButton(
                text = "🔗",
                contentDescription = "Insert Link",
                isSelected = false,
                onClick = { /* TODO: Implement link insertion */ },
                enabled = false
            )
        }
    }
}

@Composable
private fun TextToolbarButton(
    text: String,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    textDecoration: TextDecoration = TextDecoration.None
) {
    Button(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) 
                MaterialTheme.colorScheme.onPrimaryContainer 
            else 
                MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            textDecoration = textDecoration
        )
    }
}

@Composable
private fun IconToolbarButton(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) 
                MaterialTheme.colorScheme.onPrimaryContainer 
            else 
                MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp)
        )
    }
}
