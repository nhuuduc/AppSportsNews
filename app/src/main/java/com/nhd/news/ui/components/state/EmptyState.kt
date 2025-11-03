package com.nhd.news.ui.components.state

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Empty State Component
 * Hiển thị khi không có dữ liệu
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onActionClick,
                modifier = Modifier.widthIn(min = 200.dp)
            ) {
                Text(text = actionText)
            }
        }
    }
}

/**
 * Empty Articles State
 */
@Composable
fun EmptyArticlesState(
    modifier: Modifier = Modifier,
    onRefresh: (() -> Unit)? = null
) {
    EmptyState(
        icon = Icons.Default.Info,
        title = "Không có bài viết",
        message = "Hiện tại chưa có bài viết nào.\nVui lòng quay lại sau.",
        modifier = modifier,
        actionText = if (onRefresh != null) "Làm mới" else null,
        onActionClick = onRefresh
    )
}

/**
 * Empty Search Results State
 */
@Composable
fun EmptySearchResultsState(
    query: String,
    modifier: Modifier = Modifier,
    onClearSearch: (() -> Unit)? = null
) {
    EmptyState(
        icon = Icons.Default.Search,
        title = "Không tìm thấy kết quả",
        message = "Không tìm thấy kết quả cho \"$query\".\nThử tìm kiếm với từ khóa khác.",
        modifier = modifier,
        actionText = if (onClearSearch != null) "Xóa tìm kiếm" else null,
        onActionClick = onClearSearch
    )
}

/**
 * Empty Matches State
 */
@Composable
fun EmptyMatchesState(
    modifier: Modifier = Modifier,
    onRefresh: (() -> Unit)? = null
) {
    EmptyState(
        icon = Icons.Default.Info,
        title = "Không có trận đấu",
        message = "Hiện tại không có trận đấu nào.\nVui lòng quay lại sau.",
        modifier = modifier,
        actionText = if (onRefresh != null) "Làm mới" else null,
        onActionClick = onRefresh
    )
}

/**
 * Empty Videos State
 */
@Composable
fun EmptyVideosState(
    modifier: Modifier = Modifier,
    onRefresh: (() -> Unit)? = null
) {
    EmptyState(
        icon = Icons.Default.PlayArrow,
        title = "Không có video",
        message = "Hiện tại chưa có video nào.\nVui lòng quay lại sau.",
        modifier = modifier,
        actionText = if (onRefresh != null) "Làm mới" else null,
        onActionClick = onRefresh
    )
}

/**
 * Empty Comments State
 */
@Composable
fun EmptyCommentsState(
    modifier: Modifier = Modifier,
    onAddComment: (() -> Unit)? = null
) {
    EmptyState(
        icon = Icons.Default.Info,
        title = "Chưa có bình luận",
        message = "Hãy là người đầu tiên bình luận về bài viết này!",
        modifier = modifier,
        actionText = if (onAddComment != null) "Thêm bình luận" else null,
        onActionClick = onAddComment
    )
}

/**
 * Empty Favorites State
 */
@Composable
fun EmptyFavoritesState(
    modifier: Modifier = Modifier,
    onBrowseArticles: (() -> Unit)? = null
) {
    EmptyState(
        icon = Icons.Default.FavoriteBorder,
        title = "Chưa có bài viết yêu thích",
        message = "Bạn chưa lưu bài viết nào.\nHãy khám phá và lưu những bài viết yêu thích!",
        modifier = modifier,
        actionText = if (onBrowseArticles != null) "Khám phá ngay" else null,
        onActionClick = onBrowseArticles
    )
}

/**
 * Empty Notifications State
 */
@Composable
fun EmptyNotificationsState(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Notifications,
        title = "Không có thông báo",
        message = "Bạn chưa có thông báo nào.\nChúng tôi sẽ thông báo khi có tin mới.",
        modifier = modifier
    )
}

/**
 * No Internet State
 */
@Composable
fun NoInternetState(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    EmptyState(
        icon = Icons.Default.Info,
        title = "Không có kết nối internet",
        message = "Vui lòng kiểm tra kết nối của bạn\nvà thử lại.",
        modifier = modifier,
        actionText = "Thử lại",
        onActionClick = onRetry
    )
}

