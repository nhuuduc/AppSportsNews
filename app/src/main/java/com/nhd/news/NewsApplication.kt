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
        
        // Xóa cache đã hết hạn (quá 7 ngày)
        // cacheManager.clearOldCache()
    }
}
