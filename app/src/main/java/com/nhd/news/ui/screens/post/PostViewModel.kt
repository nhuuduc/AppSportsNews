package com.nhd.news.ui.screens.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhd.news.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class MyPost(
    val article_id: Int,
    val title: String,
    val content: String,
    val category_id: Int,
    val category_name: String?,
    val status: String,
    val created_at: String,
    val like_count: Int = 0,
    val comment_count: Int = 0
)

data class PostUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val myPosts: List<MyPost> = emptyList(),
    val showDeleteDialog: Boolean = false,
    val postToDelete: Int? = null,
    val isUploadingImage: Boolean = false,
    val editorReady: Boolean = false
)

@HiltViewModel
class PostViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PostUiState())
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()
    
    fun createPost(
        title: String,
        content: String,
        categoryId: Int
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            postRepository.createPost(title, content, categoryId).collect { result ->
                result.onSuccess { message ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = message
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Tạo bài viết thất bại"
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
    
    fun loadMyPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            postRepository.getMyPosts().collect { result ->
                result.onSuccess { response ->
                    // Parse response to MyPost list
                    val posts = response.articles?.mapNotNull { article ->
                        try {
                            when (article) {
                                is Map<*, *> -> {
                                    MyPost(
                                        article_id = (article["article_id"] as? Number)?.toInt() ?: 0,
                                        title = article["title"] as? String ?: "",
                                        content = article["content"] as? String ?: "",
                                        category_id = (article["category_id"] as? Number)?.toInt() ?: 0,
                                        category_name = article["category_name"] as? String,
                                        status = article["status"] as? String ?: "draft",
                                        created_at = article["created_at"] as? String ?: "",
                                        like_count = (article["like_count"] as? Number)?.toInt() ?: 0,
                                        comment_count = (article["comment_count"] as? Number)?.toInt() ?: 0
                                    )
                                }
                                else -> null
                            }
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        myPosts = posts
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Tải bài viết thất bại"
                    )
                }
            }
        }
    }
    
    fun deletePost(articleId: Int) {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = true,
            postToDelete = articleId
        )
    }
    
    fun confirmDelete() {
        val articleId = _uiState.value.postToDelete ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                showDeleteDialog = false,
                error = null
            )
            
            postRepository.deletePost(articleId).collect { result ->
                result.onSuccess { message ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = message,
                        postToDelete = null
                    )
                    // Reload posts
                    loadMyPosts()
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Xóa bài viết thất bại",
                        postToDelete = null
                    )
                }
            }
        }
    }
    
    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = false,
            postToDelete = null
        )
    }
    
    fun uploadImage(imageFile: File, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingImage = true, error = null)
            
            postRepository.uploadImage(imageFile).collect { result ->
                result.onSuccess { imageUrl ->
                    _uiState.value = _uiState.value.copy(isUploadingImage = false)
                    onSuccess(imageUrl)
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isUploadingImage = false,
                        error = error.message ?: "Upload ảnh thất bại"
                    )
                    onError(error.message ?: "Upload ảnh thất bại")
                }
            }
        }
    }
    
    fun setEditorReady(ready: Boolean) {
        _uiState.value = _uiState.value.copy(editorReady = ready)
    }
}

