package com.nhd.news.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

/**
 * Pull to Refresh wrapper component
 * Sử dụng Accompanist SwipeRefresh
 */
@Composable
fun PullToRefreshLayout(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)
    
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = onRefresh,
        modifier = modifier,
        indicator = { state, refreshTrigger ->
            SwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = refreshTrigger,
                contentColor = indicatorColor,
                backgroundColor = backgroundColor,
                scale = true
            )
        }
    ) {
        content()
    }
}

/**
 * Pull to Refresh cho danh sách
 * Bao gồm cả logic xử lý empty và error states
 */
@Composable
fun <T> PullToRefreshList(
    isLoading: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    loadingContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    PullToRefreshLayout(
        isRefreshing = isRefreshing && !isLoading,
        onRefresh = onRefresh,
        modifier = modifier
    ) {
        if (isLoading && !isRefreshing) {
            // Show initial loading
            loadingContent()
        } else {
            // Show content
            content()
        }
    }
}

