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
import com.nhd.news.utils.AppError

/**
 * Error State Component
 * Hiển thị khi có lỗi xảy ra
 */
@Composable
fun ErrorState(
    error: AppError,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    val errorInfo: Pair<ImageVector, String> = when (error) {
        is AppError.NetworkError.NoConnection -> Pair(Icons.Default.Info, "Không có kết nối")
        is AppError.NetworkError.Timeout -> Pair(Icons.Default.Info, "Hết thời gian chờ")
        is AppError.NetworkError -> Pair(Icons.Default.Info, "Lỗi mạng")
        is AppError.ServerError.NotFound -> Pair(Icons.Default.Search, "Không tìm thấy")
        is AppError.ServerError.Unauthorized -> Pair(Icons.Default.Lock, "Chưa đăng nhập")
        is AppError.ServerError -> Pair(Icons.Default.Warning, "Lỗi server")
        is AppError.AuthError.TokenExpired -> Pair(Icons.Default.Info, "Phiên hết hạn")
        is AppError.AuthError -> Pair(Icons.Default.Lock, "Lỗi xác thực")
        is AppError.ValidationError -> Pair(Icons.Default.Warning, "Dữ liệu không hợp lệ")
        else -> Pair(Icons.Default.Warning, "Đã xảy ra lỗi")
    }
    val (icon, title) = errorInfo
    
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
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = error.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                modifier = Modifier.widthIn(min = 200.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Thử lại")
            }
        }
    }
}

/**
 * Generic Error State với custom message
 */
@Composable
fun GenericErrorState(
    icon: ImageVector = Icons.Default.Warning,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
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
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                modifier = Modifier.widthIn(min = 200.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Thử lại")
            }
        }
    }
}

/**
 * Network Error State
 */
@Composable
fun NetworkErrorState(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    ErrorState(
        error = AppError.NetworkError.NoConnection(),
        modifier = modifier,
        onRetry = onRetry
    )
}

/**
 * Server Error State
 */
@Composable
fun ServerErrorState(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    ErrorState(
        error = AppError.ServerError.InternalServerError(),
        modifier = modifier,
        onRetry = onRetry
    )
}

/**
 * Load Failed State (cho image, video loading)
 */
@Composable
fun LoadFailedState(
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    GenericErrorState(
        icon = Icons.Default.Info,
        title = "Tải thất bại",
        message = "Không thể tải nội dung.\nVui lòng thử lại.",
        modifier = modifier,
        onRetry = onRetry
    )
}

/**
 * Authentication Required State
 */
@Composable
fun AuthRequiredState(
    modifier: Modifier = Modifier,
    onLogin: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        )
        
        Text(
            text = "Yêu cầu đăng nhập",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Bạn cần đăng nhập để sử dụng tính năng này.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = onLogin,
            modifier = Modifier.widthIn(min = 200.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Đăng nhập")
        }
    }
}

/**
 * Inline Error Message (cho form validation)
 */
@Composable
fun InlineErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}

/**
 * Snackbar Error (để show error trong Snackbar)
 */
@Composable
fun ShowErrorSnackbar(
    snackbarHostState: SnackbarHostState,
    error: AppError
) {
    // Sử dụng trong LaunchedEffect để hiển thị Snackbar
    // LaunchedEffect(error) {
    //     snackbarHostState.showSnackbar(
    //         message = error.message,
    //         actionLabel = "Đóng",
    //         duration = SnackbarDuration.Short
    //     )
    // }
}

