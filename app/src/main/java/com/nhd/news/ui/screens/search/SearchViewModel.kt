package com.nhd.news.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhd.news.data.models.Article
import com.nhd.news.data.models.Video
import com.nhd.news.data.repository.NewsRepository
import com.nhd.news.data.repository.AuthRepository
import com.nhd.news.data.repository.LikeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: NewsRepository,
    private val authRepository: AuthRepository,
    private val likeRepository: LikeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        
        // Auto search after user stops typing for 500ms
        searchJob?.cancel()
        if (query.isNotEmpty()) {
            searchJob = viewModelScope.launch {
                delay(500)
                search()
            }
        } else {
            clearResults()
        }
    }

    fun clearQuery() {
        _uiState.value = _uiState.value.copy(query = "")
        clearResults()
    }

    fun selectTab(tab: SearchTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun search() {
        val query = _uiState.value.query.trim()
        if (query.isEmpty()) return

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            when (_uiState.value.selectedTab) {
                SearchTab.ARTICLES -> searchArticles(query)
                SearchTab.VIDEOS -> searchVideos(query)
            }
        }
    }

    private suspend fun searchArticles(query: String) {
        repository.searchArticles(query).collect { result ->
            result.fold(
                onSuccess = { articles ->
                    _uiState.value = _uiState.value.copy(
                        articles = articles,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Có lỗi xảy ra khi tìm kiếm"
                    )
                }
            )
        }
    }

    private suspend fun searchVideos(query: String) {
        repository.searchVideos(query).collect { result ->
            result.fold(
                onSuccess = { videos ->
                    _uiState.value = _uiState.value.copy(
                        videos = videos,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Có lỗi xảy ra khi tìm kiếm"
                    )
                }
            )
        }
    }

    private fun clearResults() {
        _uiState.value = _uiState.value.copy(
            articles = emptyList(),
            videos = emptyList(),
            isLoading = false,
            error = null
        )
    }
    
    fun toggleArticleLike(article: Article) {
        if (!authRepository.isLoggedIn()) {
            _uiState.value = _uiState.value.copy(error = "Bạn cần đăng nhập để thích bài viết")
            return
        }
        
        if (!authRepository.isEmailVerified()) {
            _uiState.value = _uiState.value.copy(error = "Bạn cần xác thực email trước")
            return
        }
        
        viewModelScope.launch {
            // Optimistic update
            val updatedArticles = _uiState.value.articles.map { a ->
                if (a.articleId == article.articleId) {
                    if (article.isLiked) {
                        a.copy(isLiked = false, likeCount = (a.likeCount - 1).coerceAtLeast(0))
                    } else {
                        a.copy(isLiked = true, likeCount = a.likeCount + 1)
                    }
                } else {
                    a
                }
            }
            _uiState.value = _uiState.value.copy(articles = updatedArticles)
            
            // Call API
            likeRepository.toggleArticleLike(article.articleId).collect { result ->
                result.onSuccess { likeResponse ->
                    // Update with server response
                    val finalArticles = _uiState.value.articles.map { a ->
                        if (a.articleId == article.articleId) {
                            a.copy(
                                isLiked = likeResponse.liked,
                                likeCount = likeResponse.likeCount ?: a.likeCount
                            )
                        } else {
                            a
                        }
                    }
                    _uiState.value = _uiState.value.copy(articles = finalArticles)
                }.onFailure { error ->
                    // Revert optimistic update
                    val revertedArticles = _uiState.value.articles.map { a ->
                        if (a.articleId == article.articleId) {
                            article // Revert to original state
                        } else {
                            a
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        articles = revertedArticles,
                        error = error.message
                    )
                }
            }
        }
    }
}

data class SearchUiState(
    val query: String = "",
    val selectedTab: SearchTab = SearchTab.ARTICLES,
    val articles: List<Article> = emptyList(),
    val videos: List<Video> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class SearchTab {
    ARTICLES,
    VIDEOS
}
