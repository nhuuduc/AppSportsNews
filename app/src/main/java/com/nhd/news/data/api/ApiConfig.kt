package com.nhd.news.data.api

object ApiConfig {
    
    // const val BASE_URL = "http://10.0.2.2/api/"
    
    // const val BASE_URL = "http://172.16.36.127/api/"
    
    const val BASE_URL = "https://nhd6.site/api/"
    
    const val CONNECT_TIMEOUT = 30L
    
    const val READ_TIMEOUT = 30L
    
    const val WRITE_TIMEOUT = 30L
    
    const val DEFAULT_PAGE_SIZE = 20
    
    const val MAX_PAGE_SIZE = 100
    
    fun getBaseUrl(): String = BASE_URL.substringBeforeLast("api/")
    
    fun getAbsoluteImageUrl(imageUrl: String?): String? {
        if (imageUrl.isNullOrBlank()) return null

        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl
        }

        val baseUrl = getBaseUrl()
        val cleanPath = imageUrl.trimStart('/')
        return "$baseUrl$cleanPath"
    }
    
    fun isFakeDomainUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        
        return url.contains("cdn.example.com") ||
               url.contains("example.com") ||
               url.contains("placeholder.com") ||
               url.contains("via.placeholder.com")
    }
}
