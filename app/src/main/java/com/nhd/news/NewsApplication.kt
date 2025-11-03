package com.nhd.news

import android.app.Application
import com.nhd.news.data.local.CacheManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NewsApplication : Application() {
    
    // TODO: Re-enable when CacheManager is properly provided
    // @Inject
    // lateinit var cacheManager: CacheManager
    
    override fun onCreate() {
        super.onCreate()
        
        // 清理过期缓存（超过7天）
        // cacheManager.clearOldCache()
    }
}
