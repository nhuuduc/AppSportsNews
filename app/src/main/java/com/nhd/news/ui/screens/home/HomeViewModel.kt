package com.nhd.news.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhd.news.data.models.Article
import com.nhd.news.data.models.Match
import com.nhd.news.data.models.Video
import com.nhd.news.data.repository.NewsRepository
import com.nhd.news.data.repository.AuthRepository
import com.nhd.news.data.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NewsRepository,
    private val authRepository: AuthRepository,
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            // Load trending articles
            repository.getTrendingArticles().collect { result ->
                result.fold(
                    onSuccess = { articles ->
                        _uiState.value = _uiState.value.copy(
                            trendingArticles = articles.take(5)
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Có lỗi xảy ra"
                        )
                    }
                )
            }
        }

        viewModelScope.launch {
            // Load live matches
            repository.getLiveMatches().collect { result ->
                result.fold(
                    onSuccess = { matches ->
                        _uiState.value = _uiState.value.copy(
                            liveMatches = matches.take(3)
                        )
                    },
                    onFailure = { /* Handle error silently for live matches */ }
                )
            }
        }

        viewModelScope.launch {
            // Load trending videos
            repository.getTrendingVideos().collect { result ->
                result.fold(
                    onSuccess = { videos ->
                        _uiState.value = _uiState.value.copy(
                            trendingVideos = videos.take(5),
                            isLoading = false
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Có lỗi xảy ra",
                            isLoading = false
                        )
                    }
                )
            }
        }
    }

    fun refresh() {
        loadHomeData()
    }
    
    /**
     * Toggle like for an article
     */
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
            val updatedArticles = _uiState.value.trendingArticles.map { a ->
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
            _uiState.value = _uiState.value.copy(trendingArticles = updatedArticles)
            
            // Make API call
            articleRepository.toggleArticleLike(article.articleId).collect { result ->
                result.onSuccess { likeResponse ->
                    // Update with server response
                    val finalArticles = _uiState.value.trendingArticles.map { a ->
                        if (a.articleId == article.articleId) {
                            a.copy(
                                isLiked = likeResponse.liked,
                                likeCount = likeResponse.likeCount ?: a.likeCount
                            )
                        } else {
                            a
                        }
                    }
                    _uiState.value = _uiState.value.copy(trendingArticles = finalArticles)
                }.onFailure { error ->
                    // Revert optimistic update on error
                    val revertedArticles = _uiState.value.trendingArticles.map { a ->
                        if (a.articleId == article.articleId) {
                            article // Revert to original state
                        } else {
                            a
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        trendingArticles = revertedArticles,
                        error = error.message
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class HomeUiState(
    val trendingArticles: List<Article> = emptyList(),
    val liveMatches: List<Match> = emptyList(),
    val trendingVideos: List<Video> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
