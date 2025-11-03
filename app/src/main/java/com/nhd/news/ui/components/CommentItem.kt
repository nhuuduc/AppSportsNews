package com.nhd.news.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nhd.news.data.api.ApiConfig
import com.nhd.news.data.models.CommentItem
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CommentItem(
    comment: CommentItem,
    onLikeToggle: (Int) -> Unit,
    onReplyClick: (Int) -> Unit,
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier,
    isNested: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = if (isNested) 40.dp else 0.dp,
                top = 8.dp,
                bottom = 8.dp
            )
    ) {
        // Avatar
        val avatarUrl = if (!comment.authorAvatar.isNullOrBlank()) {
            ApiConfig.getAbsoluteImageUrl(comment.authorAvatar)
        } else {
            null
        }
        
        AsyncImage(
            model = avatarUrl ?: "https://via.placeholder.com/40",
            contentDescription = "Avatar của ${comment.authorName}",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Comment content
        Column(modifier = Modifier.weight(1f)) {
            // Author name
            Text(
                text = comment.authorName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Comment text
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Actions row (timestamp, like, reply)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Timestamp
                Text(
                    text = formatCommentDate(comment.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Like button - giống như ArticleCard
                if (isLoggedIn) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { 
                            onLikeToggle(comment.commentId)
                        }
                    ) {
                        Icon(
                            imageVector = if (comment.isLiked) 
                                Icons.Filled.Favorite 
                            else 
                                Icons.Outlined.FavoriteBorder,
                            contentDescription = if (comment.isLiked) "Bỏ thích" else "Thích",
                            tint = if (comment.isLiked) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        if (comment.likeCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = comment.likeCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (comment.isLiked) 
                                    MaterialTheme.colorScheme.error 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // Reply button (only for top-level comments)
                    if (!isNested && comment.parentCommentId == null) {
                        TextButton(
                            onClick = { onReplyClick(comment.commentId) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Trả lời",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Trả lời",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                } else {
                    // Show like count for non-logged in users
                    if (comment.likeCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = comment.likeCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatCommentDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        
        if (date != null) {
            val now = Date()
            val diffInMillis = now.time - date.time
            val diffInMinutes = diffInMillis / (1000 * 60)
            val diffInHours = diffInMillis / (1000 * 60 * 60)
            val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
            
            when {
                diffInMinutes < 1 -> "Vừa xong"
                diffInMinutes < 60 -> "${diffInMinutes} phút trước"
                diffInHours < 24 -> "${diffInHours} giờ trước"
                diffInDays < 7 -> "${diffInDays} ngày trước"
                else -> {
                    val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    outputFormat.format(date)
                }
            }
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}
