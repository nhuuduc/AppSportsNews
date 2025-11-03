package com.nhd.news.utils

import android.util.Log
import com.nhd.news.data.api.ApiConfig
import com.nhd.news.data.api.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class để test API connection
 * Inject ApiService qua Hilt
 */
@Singleton
class ApiTestHelper @Inject constructor(
    private val apiService: ApiService
) {
    
    companion object {
        private const val TAG = "ApiTestHelper"
    }
    
    /**
     * Test kết nối với API backend
     * Gọi hàm này trong MainActivity để kiểm tra kết nối
     */
    fun testApiConnection() {
        Log.d(TAG, "Testing API connection to: ${ApiConfig.BASE_URL}")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Test Categories API
                val categoriesResponse = apiService.getCategories()
                if (categoriesResponse.isSuccessful) {
                    val categories = categoriesResponse.body()
                    Log.d(TAG, "✅ Categories API: Success - ${categories?.size} categories found")
                } else {
                    Log.e(TAG, "❌ Categories API: Error ${categoriesResponse.code()}")
                }
                
                // Test Articles API
                val articlesResponse = apiService.getArticles(page = 1, limit = 5)
                if (articlesResponse.isSuccessful) {
                    val articleResponse = articlesResponse.body()
                    Log.d(TAG, "✅ Articles API: Success - ${articleResponse?.articles?.size} articles found")
                    Log.d(TAG, "📄 Total articles: ${articleResponse?.totalCount}")
                } else {
                    Log.e(TAG, "❌ Articles API: Error ${articlesResponse.code()}")
                }
                
                // Test Trending Articles API
                val trendingResponse = apiService.getTrendingArticles(limit = 3)
                if (trendingResponse.isSuccessful) {
                    val trending = trendingResponse.body()
                    Log.d(TAG, "✅ Trending Articles API: Success - ${trending?.articles?.size} articles found")
                } else {
                    Log.e(TAG, "❌ Trending Articles API: Error ${trendingResponse.code()}")
                }
                
                // Test Live Matches API
                val matchesResponse = apiService.getLiveMatches()
                if (matchesResponse.isSuccessful) {
                    val matches = matchesResponse.body()
                    Log.d(TAG, "✅ Live Matches API: Success - ${matches?.size} matches found")
                } else {
                    Log.e(TAG, "❌ Live Matches API: Error ${matchesResponse.code()}")
                }
                
                // Test Videos API
                val videosResponse = apiService.getVideos(limit = 3)
                if (videosResponse.isSuccessful) {
                    val videos = videosResponse.body()
                    Log.d(TAG, "✅ Videos API: Success - ${videos?.size} videos found")
                } else {
                    Log.e(TAG, "❌ Videos API: Error ${videosResponse.code()}")
                }
                
                Log.d(TAG, "🎉 API connection test completed!")
                
            } catch (e: Exception) {
                Log.e(TAG, "💥 API connection test failed: ${e.message}", e)
                Log.e(TAG, "🔧 Check your API configuration in ApiConfig.kt")
                Log.e(TAG, "🔧 Make sure your server is running at: ${ApiConfig.BASE_URL}")
            }
        }
    }
    
    /**
     * Test một endpoint cụ thể
     */
    fun testSpecificEndpoint(endpointName: String, test: suspend () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Testing $endpointName...")
                test()
                Log.d(TAG, "✅ $endpointName: Success")
            } catch (e: Exception) {
                Log.e(TAG, "❌ $endpointName: Failed - ${e.message}")
            }
        }
    }
}
