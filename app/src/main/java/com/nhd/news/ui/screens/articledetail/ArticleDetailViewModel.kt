package com.nhd.news.ui.screens.articledetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhd.news.data.models.Article
import com.nhd.news.data.models.CommentItem
import com.nhd.news.data.repository.AuthRepository
import com.nhd.news.data.repository.NewsRepository
import com.nhd.news.data.repository.LikeRepository
import com.nhd.news.data.repository.CommentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArticleDetailUiState(
    val article: Article? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val comments: List<CommentItem> = emptyList(),
    val isLoadingComments: Boolean = false,
    val commentError: String? = null,
    val newCommentText: String = "",
    val isPostingComment: Boolean = false,
    val replyToCommentId: Int? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val repository: NewsRepository,
    private val authRepository: AuthRepository,
    private val likeRepository: LikeRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleDetailUiState())
    val uiState: StateFlow<ArticleDetailUiState> = _uiState.asStateFlow()

    fun loadArticle(articleId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            repository.getArticleById(articleId).collect { result ->
                result.onSuccess { article ->
                    _uiState.value = _uiState.value.copy(
                        article = article,
                        isLoading = false
                    )
                    // Load comments after article is loaded
                    loadComments(articleId)
                    
                    // Load like status if user is logged in to ensure accurate state
                    if (authRepository.isLoggedIn()) {
                        loadArticleLikeStatus(articleId)
                    }
                    
                    // Tăng lượt xem bài viết
                    incrementArticleView(articleId)
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Không thể tải bài viết"
                    )
                }
            }
        }
    }
    
    private fun incrementArticleView(articleId: Int) {
        viewModelScope.launch {
            repository.incrementArticleView(articleId).collect { result ->
                result.onSuccess {
                    // Tăng lượt xem thành công, cập nhật UI nếu cần
                    android.util.Log.d("ArticleDetailViewModel", "Article view incremented successfully")
                }.onFailure { error ->
                    // Không hiển thị lỗi cho user, chỉ log
                    android.util.Log.w("ArticleDetailViewModel", "Failed to increment view: ${error.message}")
                }
            }
        }
    }
    
    private fun loadArticleLikeStatus(articleId: Int) {
        viewModelScope.launch {
            likeRepository.getArticleLikeStatus(articleId).collect { result ->
                result.onSuccess { (isLiked, likeCount) ->
                    // Update article with accurate like status from server
                    android.util.Log.d("ArticleDetailViewModel", "Loaded like status: isLiked=$isLiked, likeCount=$likeCount")
                    _uiState.value = _uiState.value.copy(
                        article = _uiState.value.article?.copy(
                            isLiked = isLiked,
                            likeCount = likeCount
                        )
                    )
                }.onFailure { error ->
                    // Log error for debugging
                    android.util.Log.e("ArticleDetailViewModel", "Failed to load like status: ${error.message}")
                }
            }
        }
    }
    
    fun loadComments(articleId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingComments = true, commentError = null)
            
            commentRepository.getComments(articleId).collect { result ->
                result.onSuccess { comments ->
                    _uiState.value = _uiState.value.copy(
                        comments = comments,
                        isLoadingComments = false
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingComments = false,
                        commentError = error.message ?: "Không thể tải bình luận"
                    )
                }
            }
        }
    }
    
    fun updateCommentText(text: String) {
        _uiState.value = _uiState.value.copy(newCommentText = text)
    }
    
    fun setReplyTo(commentId: Int?) {
        _uiState.value = _uiState.value.copy(replyToCommentId = commentId)
        if (commentId == null) {
            // Clear text when canceling reply
            _uiState.value = _uiState.value.copy(newCommentText = "")
        }
    }
    
    fun postComment(articleId: Int) {
        val content = _uiState.value.newCommentText.trim()
        if (content.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPostingComment = true, commentError = null)
            
            commentRepository.postComment(
                articleId = articleId,
                content = content,
                parentCommentId = _uiState.value.replyToCommentId
            ).collect { result ->
                result.onSuccess { newComment ->
                    // Add new comment to the list
                    val updatedComments = _uiState.value.comments + newComment
                    _uiState.value = _uiState.value.copy(
                        comments = updatedComments,
                        isPostingComment = false,
                        newCommentText = "",
                        replyToCommentId = null,
                        successMessage = "Đã đăng bình luận"
                    )
                    // Reload comments to get updated count and order
                    loadComments(articleId)
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isPostingComment = false,
                        commentError = error.message ?: "Không thể đăng bình luận"
                    )
                }
            }
        }
    }
    
    fun toggleCommentLike(commentId: Int) {
        viewModelScope.launch {
            val comment = _uiState.value.comments.find { it.commentId == commentId } ?: return@launch
            val wasLiked = comment.isLiked
            
            // Optimistic update
            val updatedComments = _uiState.value.comments.map { c ->
                if (c.commentId == commentId) {
                    c.copy(
                        isLiked = !wasLiked,
                        likeCount = if (wasLiked) (c.likeCount - 1).coerceAtLeast(0) else c.likeCount + 1
                    )
                } else {
                    c
                }
            }
            _uiState.value = _uiState.value.copy(comments = updatedComments)
            
            commentRepository.toggleCommentLike(commentId).collect { result ->
                result.onSuccess { likeResponse ->
                    // Update with actual values from server
                    val finalComments = _uiState.value.comments.map { c ->
                        if (c.commentId == commentId) {
                            c.copy(
                                isLiked = likeResponse.isLiked,
                                likeCount = likeResponse.likeCount
                            )
                        } else {
                            c
                        }
                    }
                    _uiState.value = _uiState.value.copy(comments = finalComments)
                }.onFailure { error ->
                    // Revert optimistic update
                    val revertedComments = _uiState.value.comments.map { c ->
                        if (c.commentId == commentId) {
                            c.copy(
                                isLiked = wasLiked,
                                likeCount = comment.likeCount
                            )
                        } else {
                            c
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        comments = revertedComments,
                        commentError = error.message ?: "Không thể thích bình luận"
                    )
                }
            }
        }
    }
    
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
    
    fun clearCommentError() {
        _uiState.value = _uiState.value.copy(commentError = null)
    }
    
    fun isUserLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }
    
    fun toggleArticleLike(articleId: Int) {
        if (!authRepository.isLoggedIn()) {
            _uiState.value = _uiState.value.copy(
                commentError = "Bạn cần đăng nhập để thích bài viết"
            )
            return
        }
        
        val article = _uiState.value.article ?: return
        val wasLiked = article.isLiked
        
        viewModelScope.launch {
            // Optimistic update
            _uiState.value = _uiState.value.copy(
                article = article.copy(
                    isLiked = !wasLiked,
                    likeCount = if (wasLiked) 
                        (article.likeCount - 1).coerceAtLeast(0) 
                    else 
                        article.likeCount + 1
                )
            )
            
            likeRepository.toggleArticleLike(articleId).collect { result ->
                result.onSuccess { likeResponse ->
                    // Update with actual values from server
                    android.util.Log.d("ArticleDetailViewModel", "Like response: liked=${likeResponse.liked}, likeCount=${likeResponse.likeCount}")
                    _uiState.value = _uiState.value.copy(
                        article = _uiState.value.article?.copy(
                            isLiked = likeResponse.liked,
                            likeCount = likeResponse.likeCount ?: (_uiState.value.article?.likeCount ?: 0)
                        ),
                        successMessage = if (likeResponse.liked) "Đã thích bài viết" else "Đã bỏ thích bài viết"
                    )
                }.onFailure { error ->
                    // Revert optimistic update
                    android.util.Log.e("ArticleDetailViewModel", "Failed to toggle like: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        article = _uiState.value.article?.copy(
                            isLiked = wasLiked,
                            likeCount = article.likeCount
                        ),
                        commentError = error.message ?: "Không thể thực hiện thao tác"
                    )
                }
            }
        }
    }
}

