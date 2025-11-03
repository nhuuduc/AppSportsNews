package com.nhd.news.ui.screens.videos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhd.news.data.models.Video
import com.nhd.news.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideosViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VideosUiState())
    val uiState: StateFlow<VideosUiState> = _uiState.asStateFlow()

    init {
        loadVideos()
    }

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        loadVideos()
    }

    fun loadVideos() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            repository.getVideos().collect { result ->
                result.fold(
                    onSuccess = { videos ->
                        // Filter by category if not "Tất cả"
                        val filteredVideos = if (_uiState.value.selectedCategory == "Tất cả") {
                            videos
                        } else {
                            videos.filter { 
                                it.category?.categoryName == _uiState.value.selectedCategory 
                            }
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            videos = filteredVideos,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Có lỗi xảy ra khi tải video"
                        )
                    }
                )
            }
        }
    }

    fun refresh() {
        loadVideos()
    }
}

data class VideosUiState(
    val videos: List<Video> = emptyList(),
    val selectedCategory: String = "Tất cả",
    val isLoading: Boolean = false,
    val error: String? = null
)
