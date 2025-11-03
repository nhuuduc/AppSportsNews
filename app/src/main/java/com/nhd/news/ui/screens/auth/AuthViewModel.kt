package com.nhd.news.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhd.news.data.models.User
import com.nhd.news.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        checkLoginStatus()
    }
    
    /**
     * Check if user is logged in
     */
    private fun checkLoginStatus() {
        val isLoggedIn = authRepository.isLoggedIn()
        val user = authRepository.getCachedUser()
        
        _uiState.value = _uiState.value.copy(
            isLoggedIn = isLoggedIn,
            user = user
        )
    }
    
    /**
     * Register a new user
     */
    fun register(
        email: String,
        password: String,
        username: String,
        fullName: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.register(email, password, username, fullName).collect { result ->
                result.onSuccess { response ->
                    // Auto-login after successful registration
                    authRepository.login(email, password).collect { loginResult ->
                        loginResult.onSuccess { user ->
                            // Ensure state is synced with repository
                            checkLoginStatus()
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = response.message
                            )
                        }.onFailure { loginError ->
                            // Registration succeeded but auto-login failed
                            // Still show success message, user can login manually
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                successMessage = "${response.message}\nVui lòng đăng nhập để tiếp tục."
                            )
                        }
                    }
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Đăng ký thất bại"
                    )
                }
            }
        }
    }
    
    /**
     * Login user
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.login(email, password).collect { result ->
                result.onSuccess { user ->
                    // Ensure state is synced with repository
                    checkLoginStatus()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Đăng nhập thành công"
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Đăng nhập thất bại"
                    )
                }
            }
        }
    }
    
    /**
     * Verify email with token
     */
    fun verifyEmail(token: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.verifyEmail(token).collect { result ->
                result.onSuccess { message ->
                    // Update user
                    val updatedUser = _uiState.value.user?.copy(emailVerified = true)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = updatedUser,
                        successMessage = message
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Xác thực thất bại"
                    )
                }
            }
        }
    }
    
    /**
     * Resend verification email
     */
    fun resendVerification(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.resendVerification(email).collect { result ->
                result.onSuccess { message ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = message
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Gửi lại email thất bại"
                    )
                }
            }
        }
    }
    
    /**
     * Logout user
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout().collect { result ->
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = false,
                    user = null
                )
            }
        }
    }
    
    /**
     * Get current user from API
     */
    fun refreshUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            authRepository.getCurrentUser().collect { result ->
                result.onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        user = user,
                        isLoading = false
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
    
    /**
     * Check if email is verified
     */
    fun isEmailVerified(): Boolean {
        return authRepository.isEmailVerified()
    }
}

