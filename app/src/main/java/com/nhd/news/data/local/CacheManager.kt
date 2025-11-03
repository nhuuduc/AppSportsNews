package com.nhd.news.data.local

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 缓存管理器 - 负责清理过期缓存
 */
@Singleton
class CacheManager @Inject constructor(
    private val articleDao: ArticleDao
) {
    companion object {
        // 缓存有效期：7天
        private val CACHE_VALIDITY_DAYS = 7L
    }
    
    /**
     * 清理超过7天的缓存
     */
    fun clearOldCache() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val expirationTime = System.currentTimeMillis() - 
                    TimeUnit.DAYS.toMillis(CACHE_VALIDITY_DAYS)
                articleDao.deleteOldCache(expirationTime)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 清除所有缓存
     */
    fun clearAllCache() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                articleDao.deleteAllArticles()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 获取缓存的文章数量
     */
    suspend fun getCachedArticlesCount(): Int {
        return try {
            articleDao.getCachedArticlesCount()
        } catch (e: Exception) {
            0
        }
    }
}

