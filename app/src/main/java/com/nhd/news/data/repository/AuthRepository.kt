package com.nhd.news.data.repository

import android.os.Build
import com.nhd.news.data.api.AuthApiService
import com.nhd.news.data.models.*
import com.nhd.news.data.preferences.AuthPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val authPreferences: AuthPreferences
) {
    
    /**
     * Get device info
     */
    private fun getDeviceInfo(): String {
        return "${Build.MANUFACTURER} ${Build.MODEL} - Android ${Build.VERSION.RELEASE}"
    }
    
    /**
     * Register a new user
     */
    fun register(
        email: String,
        password: String,
        username: String,
        fullName: String? = null
    ): Flow<Result<RegisterResponse>> = flow {
        try {
            val request = RegisterRequest(email, password, username, fullName)
            val response = authApiService.register(request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    emit(Result.success(body))
                } else {
                    emit(Result.failure(Exception(body.message)))
                }
            } else {
                emit(Result.failure(Exception("Đăng ký thất bại")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Login user
     */
    fun login(email: String, password: String): Flow<Result<User>> = flow {
        try {
            val request = LoginRequest(email, password, getDeviceInfo())
            val response = authApiService.login(request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.sessionToken != null && body.user != null) {
                    // Save to preferences
                    authPreferences.saveSessionToken(body.sessionToken)
                    authPreferences.saveUser(body.user)
                    
                    emit(Result.success(body.user))
                } else {
                    emit(Result.failure(Exception(body.message)))
                }
            } else {
                emit(Result.failure(Exception("Đăng nhập thất bại")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Verify email with token
     */
    fun verifyEmail(token: String): Flow<Result<String>> = flow {
        try {
            val request = com.nhd.news.data.api.VerifyEmailRequest(token)
            val response = authApiService.verifyEmail(request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    // Update user email_verified status
                    authPreferences.getUser()?.let { user ->
                        val updatedUser = user.copy(emailVerified = true)
                        authPreferences.updateUser(updatedUser)
                    }
                    emit(Result.success(body.message))
                } else {
                    emit(Result.failure(Exception(body.message)))
                }
            } else {
                emit(Result.failure(Exception("Xác thực thất bại")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Resend verification email
     */
    fun resendVerification(email: String): Flow<Result<String>> = flow<Result<String>> {
        try {
            val request = com.nhd.news.data.api.ResendVerificationRequest(email)
            val response = authApiService.resendVerification(request)
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val msg = body.message ?: "Thành công"
                if (body.success) {
                    emit(Result.success(msg))
                } else {
                    emit(Result.failure(Exception(msg)))
                }
            } else {
                emit(Result.failure(Exception("Gửi lại email thất bại")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Logout user
     */
    fun logout(): Flow<Result<String>> = flow<Result<String>> {
        try {
            val token = authPreferences.getSessionToken()
            if (token != null) {
                val response = authApiService.logout()
                authPreferences.clearAuth()
                
                if (response.isSuccessful && response.body() != null) {
                    val msg = response.body()!!.message ?: "Đã đăng xuất"
                    emit(Result.success(msg))
                } else {
                    emit(Result.success("Đã đăng xuất"))
                }
            } else {
                authPreferences.clearAuth()
                emit(Result.success("Đã đăng xuất"))
            }
        } catch (e: Exception) {
            authPreferences.clearAuth()
            emit(Result.success("Đã đăng xuất"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get current user from API
     */
    fun getCurrentUser(): Flow<Result<User>> = flow {
        try {
            val token = authPreferences.getSessionToken()
            if (token == null) {
                emit(Result.failure(Exception("Không có session token")))
                return@flow
            }
            
            val response = authApiService.getCurrentUser()
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.user != null) {
                    // Update cached user
                    authPreferences.saveUser(body.user)
                    emit(Result.success(body.user))
                } else {
                    emit(Result.failure(Exception(body.message ?: "Lỗi")))
                }
            } else {
                emit(Result.failure(Exception("Lấy thông tin user thất bại")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean = authPreferences.isLoggedIn()
    
    /**
     * Check if email is verified
     */
    fun isEmailVerified(): Boolean = authPreferences.isEmailVerified()
    
    /**
     * Get cached user
     */
    fun getCachedUser(): User? = authPreferences.getUser()
    
    /**
     * Get session token
     */
    fun getSessionToken(): String? = authPreferences.getSessionToken()
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): Int? = authPreferences.getUser()?.userId
}

