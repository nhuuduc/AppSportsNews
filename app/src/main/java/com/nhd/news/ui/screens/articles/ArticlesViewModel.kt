package com.nhd.news.ui.screens.articles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhd.news.data.models.Article
import com.nhd.news.data.models.Category
import com.nhd.news.data.repository.NewsRepository
import com.nhd.news.data.repository.AuthRepository
import com.nhd.news.data.repository.LikeRepository
import com.nhd.news.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticlesViewModel @Inject constructor(
    private val repository: NewsRepository,
    private val authRepository: AuthRepository,
    private val likeRepository: LikeRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticlesUiState())
    val uiState: StateFlow<ArticlesUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())

    init {
        loadCategories()
        loadArticles()
    }
    
    /**
     * 从API加载分类数据
     */
    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { result ->
                result.onSuccess { categories ->
                    _categories.value = categories
                    // 如果当前选中的分类不在新加载的分类列表中，重置为"全部"
                    if (_uiState.value.selectedCategoryId != null) {
                        val exists = categories.any { it.categoryId == _uiState.value.selectedCategoryId }
                        if (!exists) {
                            _uiState.value = _uiState.value.copy(selectedCategoryId = null)
                        }
                    }
                }.onFailure {
                    // 如果加载失败，保持空列表
                    _categories.value = emptyList()
                }
            }
        }
    }

    /**
     * 选择分类
     * @param categoryId 分类ID，null表示"全部"
     */
    fun selectCategory(categoryId: Int?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
        loadArticles()
    }

    fun loadArticles() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            // 使用 categoryId 来过滤文章，如果为 null 则获取所有文章
            repository.getArticles(category = "sports", page = 1).collect { result ->
                result.fold(
                    onSuccess = { articles ->
                        // 如果选中了特定分类，过滤文章
                        val filteredArticles = if (_uiState.value.selectedCategoryId != null) {
                            articles.filter { it.categoryId == _uiState.value.selectedCategoryId }
                        } else {
                            articles
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            articles = filteredArticles,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Có lỗi xảy ra khi tải tin tức"
                        )
                    }
                )
            }
        }
    }

    fun refresh() {
        loadCategories()
        loadArticles()
    }

    /**
     * 获取分类列表（包括"全部"选项）
     */
    fun getCategories(): List<CategoryItem> {
        val allCategory = CategoryItem(id = null, name = "Tất cả")
        val categories = _categories.value.map { 
            CategoryItem(id = it.categoryId, name = it.categoryName) 
        }
        return listOf(allCategory) + categories
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

data class ArticlesUiState(
    val articles: List<Article> = emptyList(),
    val selectedCategoryId: Int? = null, // null 表示"全部"
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 分类项（用于UI显示）
 */
data class CategoryItem(
    val id: Int?, // null 表示"全部"
    val name: String
)
