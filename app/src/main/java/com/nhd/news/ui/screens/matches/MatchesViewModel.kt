package com.nhd.news.ui.screens.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhd.news.data.models.Match
import com.nhd.news.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchesViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchesUiState())
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

    init {
        loadMatches()
    }

    fun selectStatus(status: String) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
        loadMatches()
    }

    fun loadMatches() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val status = when (_uiState.value.selectedStatus) {
                "Đang diễn ra" -> "live"
                "Đã kết thúc" -> "finished"
                "Sắp diễn ra" -> "scheduled"
                else -> null // "Tất cả"
            }

            repository.getMatches(status = status).collect { result ->
                result.fold(
                    onSuccess = { matches ->
                        _uiState.value = _uiState.value.copy(
                            matches = matches,
                            isLoading = false,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Có lỗi xảy ra khi tải trận đấu"
                        )
                    }
                )
            }
        }
    }

    fun refresh() {
        loadMatches()
    }
}

data class MatchesUiState(
    val matches: List<Match> = emptyList(),
    val selectedStatus: String = "Tất cả",
    val isLoading: Boolean = false,
    val error: String? = null
)
