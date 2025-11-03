package com.nhd.news.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RecentSearchHelper(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "recent_searches", 
        Context.MODE_PRIVATE
    )
    
    private val gson = Gson()
    
    companion object {
        private const val KEY_SEARCHES = "searches"
        private const val MAX_RECENT_SEARCHES = 10
    }
    
    /**
     * Get all recent searches
     */
    fun getRecentSearches(): List<String> {
        val json = prefs.getString(KEY_SEARCHES, null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    /**
     * Add a search to recent searches
     */
    fun addRecentSearch(keyword: String) {
        if (keyword.isBlank()) return
        
        val searches = getRecentSearches().toMutableList()
        
        // Remove if already exists
        searches.remove(keyword)
        
        // Add to beginning
        searches.add(0, keyword)
        
        // Keep only MAX_RECENT_SEARCHES items
        if (searches.size > MAX_RECENT_SEARCHES) {
            searches.subList(MAX_RECENT_SEARCHES, searches.size).clear()
        }
        
        // Save
        saveSearches(searches)
    }
    
    /**
     * Remove a search from recent searches
     */
    fun removeRecentSearch(keyword: String) {
        val searches = getRecentSearches().toMutableList()
        searches.remove(keyword)
        saveSearches(searches)
    }
    
    /**
     * Clear all recent searches
     */
    fun clearRecentSearches() {
        prefs.edit().remove(KEY_SEARCHES).apply()
    }
    
    /**
     * Save searches to SharedPreferences
     */
    private fun saveSearches(searches: List<String>) {
        val json = gson.toJson(searches)
        prefs.edit().putString(KEY_SEARCHES, json).apply()
    }
}

