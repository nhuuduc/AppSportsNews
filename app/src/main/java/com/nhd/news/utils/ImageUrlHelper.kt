package com.nhd.news.utils

import android.util.Log
import com.nhd.news.data.api.ApiConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class để xử lý URL hình ảnh
 * Chuyển đổi URL tương đối thành URL tuyệt đối và kiểm tra fake domains
 */
@Singleton
class ImageUrlHelper @Inject constructor() {
    
    companion object {
        private const val TAG = "ImageUrlHelper"
        
        /**
         * Lấy base URL từ API config (không có /api/)
         */
        fun getBaseUrl(): String = ApiConfig.BASE_URL.substringBeforeLast("api/")
    }
    
    /**
     * Chuyển đổi URL tương đối thành URL tuyệt đối
     * @param imageUrl URL gốc (có thể là tương đối hoặc tuyệt đối)
     * @return URL tuyệt đối hoặc null nếu imageUrl null/blank
     */
    fun getAbsoluteImageUrl(imageUrl: String?): String? {
        if (imageUrl.isNullOrBlank()) return null

        // Đã là absolute URL (bắt đầu với http:// hoặc https://)
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl
        }

        // URL tương đối - chuyển thành absolute
        val baseUrl = getBaseUrl() // Kết thúc bằng "/"
        val cleanPath = imageUrl.trimStart('/')  // Xóa dấu / ở đầu
        val absoluteUrl = "$baseUrl$cleanPath"

        // Debug log
        Log.d(TAG, "Converting image URL: '$imageUrl' -> '$absoluteUrl'")

        return absoluteUrl
    }

    /**
     * Kiểm tra xem URL có phải từ fake/placeholder domain không
     * @param url URL cần kiểm tra
     * @return true nếu là fake domain, false nếu không
     */
    fun isFakeDomainUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false

        return url.contains("cdn.example.com") ||
               url.contains("example.com") ||
               url.contains("placeholder.com") ||
               url.contains("via.placeholder.com")
    }
    
    /**
     * Lấy URL hình ảnh hợp lệ (absolute và không phải fake domain)
     * @param imageUrl URL gốc
     * @return URL hợp lệ hoặc null nếu không hợp lệ
     */
    fun getValidImageUrl(imageUrl: String?): String? {
        val absoluteUrl = getAbsoluteImageUrl(imageUrl)
        return if (absoluteUrl != null && !isFakeDomainUrl(absoluteUrl)) {
            absoluteUrl
        } else {
            null
        }
    }
}

