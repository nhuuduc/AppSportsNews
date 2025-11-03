package com.nhd.news.ui.screens.videos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.nhd.news.data.models.Video
import com.nhd.news.ui.components.VideoCard
import com.nhd.news.ui.theme.TinTứcThểThaoTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideosScreen(
    modifier: Modifier = Modifier,
    scrollToTopTrigger: Int = 0,
    viewModel: VideosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isLoading)
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Scroll to top khi double-tap
    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = "Video",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Làm mới"
                    )
                }
            }
        )
        
        // Categories Filter
        VideoCategoriesRow(
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = { viewModel.selectCategory(it) }
        )
        
        // Content with SwipeRefresh
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoading && uiState.videos.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.error != null && uiState.videos.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.error ?: "Có lỗi xảy ra",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.refresh() }) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                
                uiState.videos.isEmpty() && !uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Không có video nào",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                else -> {
                    // Videos List
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.videos) { video ->
                            VideoCard(
                                video = video,
                                onClick = { /* TODO: Navigate to video player */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoCategoriesRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("Tất cả", "Bóng đá", "Bóng rổ", "Tennis", "Khác")
    
    LazyRow(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                selected = selectedCategory == category
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VideosScreenPreview() {
    TinTứcThểThaoTheme {
        VideosScreen()
    }
}
