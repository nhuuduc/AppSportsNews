package com.nhd.news.ui.screens.articledetail

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nhd.news.ui.components.CommentItem
import com.nhd.news.ui.components.HtmlText
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    articleId: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArticleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(articleId) {
        viewModel.loadArticle(articleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết tin tức") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                actions = {
                    // Like button
                    IconButton(onClick = { 
                        android.util.Log.d("ArticleDetailScreen", "Toggle like clicked, current isLiked: ${uiState.article?.isLiked}")
                        viewModel.toggleArticleLike(articleId)
                    }) {
                        Icon(
                            imageVector = if (uiState.article?.isLiked == true) 
                                Icons.Filled.Favorite 
                            else 
                                Icons.Outlined.FavoriteBorder,
                            contentDescription = if (uiState.article?.isLiked == true) "Bỏ thích" else "Thích",
                            tint = if (uiState.article?.isLiked == true) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Share button
                    IconButton(onClick = { 
                        uiState.article?.let { article ->
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, article.title)
                                putExtra(Intent.EXTRA_TEXT, 
                                    "${article.title}\n\n${article.summary ?: ""}\n\nĐọc thêm tại: https://nhd.news/article/${article.slug}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ bài viết"))
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Chia sẻ"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.error ?: "Có lỗi xảy ra",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadArticle(articleId) }) {
                            Text("Thử lại")
                        }
                    }
                }

                uiState.article != null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            // Category chip
                            uiState.article?.categoryName?.let { category ->
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        item {
                            // Title
                            Text(
                                text = uiState.article?.title ?: "",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item {
                            // Author and date
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                uiState.article?.authorName?.let { author ->
                                    Text(
                                        text = "Bởi $author",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                uiState.article?.publishedAt?.let { date ->
                                    Text(
                                        text = formatDate(date),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        item {
                            // Thumbnail
                            uiState.article?.thumbnailUrl?.let { imageUrl ->
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = uiState.article?.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        item {
                            // Summary
                            uiState.article?.summary?.let { summary ->
                                Text(
                                    text = summary,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        item {
                            HorizontalDivider()
                        }

                        item {
                            // Content
                            uiState.article?.content?.let { content ->
                                HtmlText(
                                    html = content,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                                )
                            }
                        }

                        item {
                            // Interaction buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Like button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { 
                                        android.util.Log.d("ArticleDetailScreen", "Interaction like clicked, current isLiked: ${uiState.article?.isLiked}")
                                        viewModel.toggleArticleLike(articleId)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (uiState.article?.isLiked == true) 
                                            Icons.Filled.Favorite 
                                        else 
                                            Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (uiState.article?.isLiked == true) 
                                            MaterialTheme.colorScheme.error 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${uiState.article?.likeCount ?: 0}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                // Share button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { 
                                        uiState.article?.let { article ->
                                            val shareIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_SUBJECT, article.title)
                                                putExtra(Intent.EXTRA_TEXT, 
                                                    "${article.title}\n\n${article.summary ?: ""}\n\nĐọc thêm tại: https://nhd.news/article/${article.slug}")
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ bài viết"))
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Chia sẻ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                // Comment count
                                Text(
                                    text = "${uiState.comments.size} bình luận",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                // View count
                                Text(
                                    text = "${uiState.article?.viewCount ?: 0} lượt xem",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Comments section
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Comments header
                        item {
                            Text(
                                text = "Bình luận (${uiState.comments.size})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Comment input box
                        item {
                            CommentInputBox(
                                text = uiState.newCommentText,
                                onTextChange = { viewModel.updateCommentText(it) },
                                onSendClick = { viewModel.postComment(articleId) },
                                isPosting = uiState.isPostingComment,
                                replyToCommentId = uiState.replyToCommentId,
                                onCancelReply = { viewModel.setReplyTo(null) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Comments loading indicator
                        if (uiState.isLoadingComments) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        
                        // Comments list
                        if (!uiState.isLoadingComments) {
                            // Separate top-level and nested comments
                            val topLevelComments = uiState.comments.filter { it.parentCommentId == null }
                            val nestedComments = uiState.comments.filter { it.parentCommentId != null }
                            
                            items(topLevelComments) { comment ->
                                CommentItem(
                                    comment = comment,
                                    onLikeToggle = { viewModel.toggleCommentLike(it) },
                                    onReplyClick = { viewModel.setReplyTo(it) },
                                    isLoggedIn = viewModel.isUserLoggedIn(),
                                    isNested = false
                                )
                                
                                // Show nested replies for this comment
                                val replies = nestedComments.filter { it.parentCommentId == comment.commentId }
                                replies.forEach { reply ->
                                    CommentItem(
                                        comment = reply,
                                        onLikeToggle = { viewModel.toggleCommentLike(it) },
                                        onReplyClick = { },
                                        isLoggedIn = viewModel.isUserLoggedIn(),
                                        isNested = true
                                    )
                                }
                                
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                        
                        // Empty state
                        if (!uiState.isLoadingComments && uiState.comments.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Chưa có bình luận nào. Hãy là người đầu tiên!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Show success message
    if (uiState.successMessage != null) {
        LaunchedEffect(uiState.successMessage) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearSuccessMessage()
        }
        Snackbar(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(uiState.successMessage ?: "")
        }
    }
    
    // Show comment error dialog
    if (uiState.commentError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearCommentError() },
            title = { Text("Lỗi") },
            text = { Text(uiState.commentError ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearCommentError() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun CommentInputBox(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isPosting: Boolean,
    replyToCommentId: Int?,
    onCancelReply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Reply indicator
        if (replyToCommentId != null) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Đang trả lời bình luận",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    IconButton(
                        onClick = onCancelReply,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hủy trả lời",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Input field
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Viết bình luận...") },
            trailingIcon = {
                IconButton(
                    onClick = onSendClick,
                    enabled = text.isNotBlank() && !isPosting
                ) {
                    if (isPosting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Gửi",
                            tint = if (text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            maxLines = 4
        )
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

