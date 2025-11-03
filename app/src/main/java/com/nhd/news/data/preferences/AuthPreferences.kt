package com.nhd.news.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.nhd.news.data.models.User
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )
    
    private val gson = Gson()
    
    companion object {
        private const val PREF_NAME = "auth_prefs"
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_USER = "user"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    /**
     * Save session token
     */
    fun saveSessionToken(token: String) {
        prefs.edit().putString(KEY_SESSION_TOKEN, token).apply()
    }
    
    /**
     * Get session token
     */
    fun getSessionToken(): String? {
        return prefs.getString(KEY_SESSION_TOKEN, null)
    }
    
    /**
     * Save user info
     */
    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        prefs.edit()
            .putString(KEY_USER, userJson)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }
    
    /**
     * Get user info
     */
    fun getUser(): User? {
        val userJson = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && 
               getSessionToken() != null && 
               getUser() != null
    }
    
    /**
     * Check if email is verified
     */
    fun isEmailVerified(): Boolean {
        return getUser()?.emailVerified == true
    }
    
    /**
     * Clear all auth data
     */
    fun clearAuth() {
        prefs.edit().clear().apply()
    }
    
    /**
     * Update user info (e.g., after email verification)
     */
    fun updateUser(user: User) {
        saveUser(user)
    }
}

