package com.nhd.news.ui.screens.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nhd.news.data.models.User
import com.nhd.news.data.repository.UserRepository
import com.nhd.news.utils.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: User? = null,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    application: Application,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadProfile()
    }
    
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            userRepository.getProfile().collect { result ->
                result.onSuccess { profile ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        profile = profile
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Lỗi khi tải thông tin"
                    )
                }
            }
        }
    }
    
    fun updateProfile(
        fullName: String,
        phone: String,
        gender: String?
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            userRepository.updateProfile(fullName, phone, gender).collect { result ->
                result.onSuccess { message ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = message
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Cập nhật thất bại"
                    )
                }
            }
        }
    }
    
    fun changePassword(
        oldPassword: String,
        newPassword: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            userRepository.changePassword(oldPassword, newPassword).collect { result ->
                result.onSuccess { message ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = message
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Đổi mật khẩu thất bại"
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
    
    fun uploadAvatar(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Xử lý ảnh trước khi upload (crop + compress)
                val processedUri = withContext(Dispatchers.IO) {
                    ImageUtils.prepareAvatarForUpload(getApplication(), imageUri)
                }
                
                if (processedUri == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Không thể xử lý ảnh"
                    )
                    return@launch
                }
                
                // Upload ảnh đã xử lý
                userRepository.uploadAvatar(processedUri).collect { result ->
                    result.onSuccess { avatarUrl ->
                        // Cleanup temp files
                        withContext(Dispatchers.IO) {
                            ImageUtils.cleanupTempFiles(getApplication())
                        }
                        // Reload profile to get updated avatar
                        loadProfile()
                    }.onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Tải ảnh thất bại"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Lỗi khi xử lý ảnh: ${e.message}"
                )
            }
        }
    }
}

