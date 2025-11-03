package com.nhd.news.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhd.news.data.models.Article
import com.nhd.news.data.repository.LikeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedArticlesUiState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val hasMore: Boolean = true
)

@HiltViewModel
class SavedArticlesViewModel @Inject constructor(
    private val likeRepository: LikeRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SavedArticlesUiState())
    val uiState: StateFlow<SavedArticlesUiState> = _uiState.asStateFlow()
    
    init {
        loadSavedArticles()
    }
    
    fun loadSavedArticles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, currentPage = 1) }
            
            likeRepository.getLikedArticles(page = 1, limit = 20).collect { result ->
                result.onSuccess { response ->
                    _uiState.update { 
                        it.copy(
                            articles = response.articles,
                            isLoading = false,
                            error = null,
                            currentPage = 1,
                            hasMore = response.articles.size >= 20
                        ) 
                    }
                }.onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = exception.message ?: "Không thể tải bài viết"
                        ) 
                    }
                }
            }
        }
    }
    
    fun loadMore() {
        if (_uiState.value.isLoading || !_uiState.value.hasMore) return
        
        viewModelScope.launch {
            val nextPage = _uiState.value.currentPage + 1
            _uiState.update { it.copy(isLoading = true) }
            
            likeRepository.getLikedArticles(page = nextPage, limit = 20).collect { result ->
                result.onSuccess { response ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            articles = currentState.articles + response.articles,
                            isLoading = false,
                            currentPage = nextPage,
                            hasMore = response.articles.size >= 20
                        )
                    }
                }.onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Không thể tải thêm bài viết"
                        ) 
                    }
                }
            }
        }
    }
    
    fun toggleLike(article: Article) {
        viewModelScope.launch {
            // Toggle like - if currently liked, remove from list
            likeRepository.toggleArticleLike(article.articleId).collect { result ->
                result.onSuccess { likeResponse ->
                    if (!likeResponse.liked) {
                        // Article was unliked, remove from saved list
                        _uiState.update { currentState ->
                            currentState.copy(
                                articles = currentState.articles.filter { it.articleId != article.articleId }
                            )
                        }
                    }
                }.onFailure {
                    _uiState.update { it.copy(error = "Không thể bỏ lưu bài viết") }
                }
            }
        }
    }
}

