package com.nhd.news.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nhd.news.ui.components.ArticleCard
import com.nhd.news.ui.components.VideoCard
import com.nhd.news.ui.theme.TinTứcThểThaoTheme
import com.nhd.news.ui.theme.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch

// Custom SVG Icons
private val SunIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Sun",
        defaultWidth = 24.0.dp,
        defaultHeight = 24.0.dp,
        viewportWidth = 24.0f,
        viewportHeight = 24.0f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            // Sun center circle
            moveTo(12f, 8f)
            arcTo(4f, 4f, 0f, true, false, 12f, 16f)
            arcTo(4f, 4f, 0f, true, false, 12f, 8f)
            close()
            // Sun rays
            moveTo(12f, 2f)
            verticalLineTo(4f)
            moveTo(12f, 20f)
            verticalLineTo(22f)
            moveTo(4f, 12f)
            horizontalLineTo(2f)
            moveTo(22f, 12f)
            horizontalLineTo(20f)
            moveTo(6.34f, 6.34f)
            lineTo(4.93f, 4.93f)
            moveTo(19.07f, 19.07f)
            lineTo(17.66f, 17.66f)
            moveTo(6.34f, 17.66f)
            lineTo(4.93f, 19.07f)
            moveTo(19.07f, 4.93f)
            lineTo(17.66f, 6.34f)
        }
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f
        ) {
            moveTo(12f, 2f)
            verticalLineTo(4f)
            moveTo(12f, 20f)
            verticalLineTo(22f)
            moveTo(4f, 12f)
            horizontalLineTo(2f)
            moveTo(22f, 12f)
            horizontalLineTo(20f)
            moveTo(6.34f, 6.34f)
            lineTo(4.93f, 4.93f)
            moveTo(19.07f, 19.07f)
            lineTo(17.66f, 17.66f)
            moveTo(6.34f, 17.66f)
            lineTo(4.93f, 19.07f)
            moveTo(19.07f, 4.93f)
            lineTo(17.66f, 6.34f)
        }
    }.build()

private val MoonIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Moon",
        defaultWidth = 24.0.dp,
        defaultHeight = 24.0.dp,
        viewportWidth = 24.0f,
        viewportHeight = 24.0f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(21f, 12.79f)
            arcTo(9f, 9f, 0f, true, true, 11.21f, 3f)
            arcTo(7f, 7f, 0f, false, false, 21f, 12.79f)
            close()
        }
    }.build()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    scrollToTopTrigger: Int = 0,
    onSearchClick: () -> Unit = {},
    onArticleClick: (Int) -> Unit = {},
    onCreatePostClick: () -> Unit = {},
    themeViewModel: ThemeViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    val uiState by homeViewModel.uiState.collectAsState()
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
    
    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // Error will be shown in SnackbarHost
            homeViewModel.clearError()
        }
    }
    
    Scaffold(
        modifier = modifier.fillMaxSize()
        // TODO: Tạo bài viết - sẽ cập nhật sau
        // floatingActionButton = {
        //     FloatingActionButton(
        //         onClick = onCreatePostClick,
        //         containerColor = MaterialTheme.colorScheme.primary
        //     ) {
        //         Icon(Icons.Default.Add, "Tạo bài viết")
        //     }
        // }
    ) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = "Trang chủ",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { themeViewModel.toggleDarkMode() }) {
                    Icon(
                        imageVector = if (isDarkMode) SunIcon else MoonIcon,
                        contentDescription = if (isDarkMode) "Chế độ sáng" else "Chế độ tối"
                    )
                }
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Tìm kiếm"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
        
        // Content with SwipeRefresh
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { homeViewModel.refresh() }
        ) {
            when {
                uiState.isLoading && uiState.trendingArticles.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.error != null && uiState.trendingArticles.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = uiState.error ?: "Có lỗi xảy ra",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { homeViewModel.refresh() }) {
                                Text("Thử lại")
                            }
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Live Matches Section
                        if (uiState.liveMatches.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Trận đấu trực tiếp")
                                Spacer(modifier = Modifier.height(8.dp))
                                LiveMatchesRow(matches = uiState.liveMatches)
                            }
                        }
                        
                        // Trending Articles Section
                        if (uiState.trendingArticles.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Tin tức nổi bật")
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            items(uiState.trendingArticles) { article ->
                                ArticleCard(
                                    article = article,
                                    onClick = { onArticleClick(article.articleId) },
                                    onLikeClick = { homeViewModel.toggleArticleLike(it) }
                                )
                            }
                        }
                        
                        // Trending Videos Section
                        if (uiState.trendingVideos.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Video nổi bật")
                                Spacer(modifier = Modifier.height(8.dp))
                                TrendingVideosRow(videos = uiState.trendingVideos)
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreakingNewsCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        onClick = { /* TODO: Navigate to article */ }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = "https://via.placeholder.com/400x200",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "TIN NÓNG",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    Text(
                        text = "Tin tức nóng hổi nhất trong ngày",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun FeaturedArticlesRow(
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(5) { index ->
            FeaturedArticleCard(
                title = "Bài viết nổi bật ${index + 1}",
                imageUrl = "https://via.placeholder.com/200x120"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeaturedArticleCard(
    title: String,
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(200.dp),
        onClick = { /* TODO: Navigate to article */ }
    ) {
        Column {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
fun LiveMatchesRow(
    matches: List<com.nhd.news.data.models.Match>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(matches) { match ->
            MatchCard(
                homeTeam = match.homeTeam?.teamName ?: "Team A",
                awayTeam = match.awayTeam?.teamName ?: "Team B",
                homeScore = match.homeScore?.toString() ?: "-",
                awayScore = match.awayScore?.toString() ?: "-",
                status = when (match.status ?: "") {
                    "live" -> "LIVE"
                    "finished" -> "KT"
                    "scheduled" -> "Sắp diễn ra"
                    else -> "TBD"
                }
            )
        }
    }
}

@Composable
fun TrendingVideosRow(
    videos: List<com.nhd.news.data.models.Video>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(videos) { video ->
            VideoCard(
                video = video,
                onClick = { /* TODO: Navigate to video player */ }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchCard(
    homeTeam: String,
    awayTeam: String,
    homeScore: String,
    awayScore: String,
    status: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(200.dp),
        onClick = { /* TODO: Navigate to match detail */ }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status
            Surface(
                color = if (status == "LIVE") MaterialTheme.colorScheme.error 
                       else MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (status == "LIVE") MaterialTheme.colorScheme.onError 
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Home Team
            Text(
                text = homeTeam,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Score
            Text(
                text = "$homeScore - $awayScore",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Away Team
            Text(
                text = awayTeam,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleCard(
    title: String,
    summary: String,
    imageUrl: String,
    category: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = { /* TODO: Navigate to article */ }
    ) {
        Row(
            modifier = Modifier.padding(12.dp)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 12.dp),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    TinTứcThểThaoTheme {
        HomeScreen()
    }
}
