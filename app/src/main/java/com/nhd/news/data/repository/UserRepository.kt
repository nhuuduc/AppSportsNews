package com.nhd.news.data.repository

import android.content.Context
import android.net.Uri
import com.nhd.news.data.api.UserApiService
import com.nhd.news.data.models.User
import com.nhd.news.data.preferences.AuthPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApiService: UserApiService,
    private val authPreferences: AuthPreferences,
    @ApplicationContext private val context: Context
) {
    
    /**
     * Get user profile
     */
    fun getProfile(): Flow<Result<User>> = flow {
        try {
            val response = userApiService.getProfile()
            
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.profile != null) {
                    // Update cached user
                    authPreferences.saveUser(body.profile)
                    emit(Result.success(body.profile))
                } else {
                    emit(Result.failure(Exception(body.message)))
                }
            } else {
                emit(Result.failure(Exception("Không thể tải thông tin")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Update user profile
     */
    fun updateProfile(
        fullName: String,
        phone: String,
        gender: String?
    ): Flow<Result<String>> = flow {
        try {
            val json = JSONObject().apply {
                put("action", "update")
                put("full_name", fullName)
                put("phone", phone)
                if (gender != null) put("gender", gender)
            }
            
            val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val response = userApiService.updateProfile(body)
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                if (responseBody.success) {
                    emit(Result.success(responseBody.message))
                } else {
                    emit(Result.failure(Exception(responseBody.message)))
                }
            } else {
                emit(Result.failure(Exception("Cập nhật thất bại")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Change password
     */
    fun changePassword(
        oldPassword: String,
        newPassword: String
    ): Flow<Result<String>> = flow {
        try {
            val json = JSONObject().apply {
                put("old_password", oldPassword)
                put("new_password", newPassword)
            }
            
            val body = json.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val response = userApiService.changePassword(body)
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                if (responseBody.success) {
                    emit(Result.success(responseBody.message))
                } else {
                    emit(Result.failure(Exception(responseBody.message)))
                }
            } else {
                emit(Result.failure(Exception("Đổi mật khẩu thất bại")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Upload avatar
     */
    fun uploadAvatar(imageUri: Uri): Flow<Result<String>> = flow {
        try {
            // Copy URI to temp file
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val tempFile = File(context.cacheDir, "temp_avatar_${System.currentTimeMillis()}.jpg")
            
            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            // Create multipart request
            val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("avatar", tempFile.name, requestFile)
            
            val response = userApiService.updateAvatar(body)
            
            // Clean up temp file
            tempFile.delete()
            
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                if (responseBody.success && responseBody.avatar_url != null) {
                    emit(Result.success(responseBody.avatar_url))
                } else {
                    emit(Result.failure(Exception(responseBody.message)))
                }
            } else {
                emit(Result.failure(Exception("Tải ảnh thất bại")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)
}

