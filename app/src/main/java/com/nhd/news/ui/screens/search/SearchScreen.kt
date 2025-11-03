package com.nhd.news.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nhd.news.data.models.Article
import com.nhd.news.data.models.Video
import com.nhd.news.ui.components.ArticleCard
import com.nhd.news.ui.components.VideoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onArticleClick: (Int) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Search Bar
        TopAppBar(
            title = {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = { Text("Tìm kiếm tin tức, video...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Tìm kiếm"
                        )
                    },
                    trailingIcon = {
                        if (uiState.query.isNotEmpty()) {
                            IconButton(onClick = viewModel::clearQuery) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Xóa"
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            viewModel.search()
                            keyboardController?.hide()
                        }
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Quay lại"
                    )
                }
            }
        )

        // Search Type Tabs
        if (uiState.query.isNotEmpty()) {
            TabRow(
                selectedTabIndex = uiState.selectedTab.ordinal,
                modifier = Modifier.fillMaxWidth()
            ) {
                SearchTab.values().forEach { tab ->
                    Tab(
                        selected = uiState.selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Text(
                                text = when (tab) {
                                    SearchTab.ARTICLES -> "Tin tức"
                                    SearchTab.VIDEOS -> "Video"
                                }
                            )
                        }
                    )
                }
            }
        }

        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                uiState.query.isEmpty() -> {
                    SearchSuggestions(
                        onSuggestionClick = { suggestion ->
                            viewModel.onQueryChange(suggestion)
                            viewModel.search()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                uiState.error != null -> {
                    ErrorMessage(
                        error = uiState.error ?: "Có lỗi xảy ra",
                        onRetry = viewModel::search,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                else -> {
                    SearchResults(
                        uiState = uiState,
                        onArticleClick = onArticleClick,
                        onArticleLikeClick = { article -> viewModel.toggleArticleLike(article) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchSuggestions(
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestions = listOf(
        "Bóng đá Việt Nam",
        "World Cup 2026",
        "Premier League",
        "La Liga",
        "Champions League",
        "V.League",
        "Thể thao Olympic",
        "Tennis",
        "Basketball NBA"
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Gợi ý tìm kiếm",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        suggestions.forEach { suggestion ->
            SuggestionChip(
                onClick = { onSuggestionClick(suggestion) },
                label = { Text(suggestion) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SearchResults(
    uiState: SearchUiState,
    onArticleClick: (Int) -> Unit,
    onArticleLikeClick: (Article) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (uiState.selectedTab) {
            SearchTab.ARTICLES -> {
                if (uiState.articles.isEmpty()) {
                    item {
                        EmptyResults(
                            message = "Không tìm thấy tin tức nào",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    items(uiState.articles) { article ->
                        ArticleCard(
                            article = article,
                            onClick = { onArticleClick(article.articleId) },
                            onLikeClick = onArticleLikeClick
                        )
                    }
                }
            }
            
            SearchTab.VIDEOS -> {
                if (uiState.videos.isEmpty()) {
                    item {
                        EmptyResults(
                            message = "Không tìm thấy video nào",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
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

@Composable
private fun EmptyResults(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorMessage(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
                        Text(
                            text = error ?: "Có lỗi xảy ra",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onRetry) {
            Text("Thử lại")
        }
    }
}
