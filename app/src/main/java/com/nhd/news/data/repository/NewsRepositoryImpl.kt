package com.nhd.news.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.nhd.news.data.api.ApiConfig
import com.nhd.news.data.api.ApiService
import com.nhd.news.data.local.ArticleDao
import com.nhd.news.data.local.toArticle
import com.nhd.news.data.local.toEntity
import com.nhd.news.data.models.Article
import com.nhd.news.data.models.Match
import com.nhd.news.data.models.Video
import com.nhd.news.data.paging.ArticlesPagingSource
import com.nhd.news.data.paging.SearchArticlesPagingSource
import com.nhd.news.utils.ImageUrlHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val articleDao: ArticleDao,
    private val imageUrlHelper: ImageUrlHelper
) : NewsRepository {

    override fun getArticles(category: String, page: Int): Flow<Result<List<Article>>> = flow {
        try {
            // 先尝试从网络获取
            val response = apiService.getArticles(page = page, limit = ApiConfig.DEFAULT_PAGE_SIZE)
            if (response.isSuccessful) {
                val articleResponse = response.body()
                if (articleResponse != null) {
                    // Use backend Article model directly, just update image URLs
                    // Sort by published date (newest first)
                    val articles = articleResponse.articles
                        .map { article ->
                            article.copy(
                                thumbnailUrl = imageUrlHelper.getAbsoluteImageUrl(article.thumbnailUrl)
                            )
                        }
                        .sortedByDescending { it.publishedAt ?: it.createdAt ?: "" }
                    
                    // 保存到缓存
                    if (page == 1) {
                        articleDao.insertArticles(articles.map { it.toEntity() })
                    }
                    
                    emit(Result.success(articles))
                } else {
                    emit(Result.failure(Exception("Empty response")))
                }
            } else {
                emit(Result.failure(Exception("API Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            // 网络失败时从缓存读取
            try {
                articleDao.getAllArticles().collect { cachedArticles ->
                    if (cachedArticles.isNotEmpty()) {
                        emit(Result.success(cachedArticles.map { it.toArticle() }))
                    } else {
                        emit(Result.failure(e))
                    }
                }
            } catch (cacheError: Exception) {
                emit(Result.failure(e))
            }
        }
    }

    override fun getArticleById(id: Int): Flow<Result<Article>> = flow {
        try {
            // 先尝试从网络获取
            val response = apiService.getArticleById(id)
            if (response.isSuccessful) {
                val article = response.body()
                if (article != null) {
                    android.util.Log.d("NewsRepositoryImpl", "Article $id from API: isLiked=${article.isLiked}, likeCount=${article.likeCount}")
                    // Update image URLs
                    val updatedArticle = article.copy(
                        thumbnailUrl = imageUrlHelper.getAbsoluteImageUrl(article.thumbnailUrl)
                    )
                    
                    // 保存到缓存
                    articleDao.insertArticle(updatedArticle.toEntity())
                    
                    emit(Result.success(updatedArticle))
                } else {
                    emit(Result.failure(Exception("Article not found")))
                }
            } else {
                emit(Result.failure(Exception("API Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            // 网络失败时从缓存读取
            android.util.Log.w("NewsRepositoryImpl", "Failed to load article $id from network, trying cache: ${e.message}")
            try {
                val cachedArticle = articleDao.getArticleById(id)
                if (cachedArticle != null) {
                    emit(Result.success(cachedArticle.toArticle()))
                } else {
                    emit(Result.failure(e))
                }
            } catch (cacheError: Exception) {
                emit(Result.failure(e))
            }
        }
    }

    override fun searchArticles(query: String, page: Int): Flow<Result<List<Article>>> = flow {
        try {
            // 先尝试从网络搜索
            val response = apiService.searchContent(keyword = query, type = "articles", limit = 20)
            if (response.isSuccessful) {
                val searchResponse = response.body()
                if (searchResponse != null) {
                    // Use backend Article model directly, just update image URLs
                    // Sort by published date (newest first)
                    val articles = searchResponse.articles
                        .map { article ->
                            article.copy(
                                thumbnailUrl = imageUrlHelper.getAbsoluteImageUrl(article.thumbnailUrl)
                            )
                        }
                        .sortedByDescending { it.publishedAt ?: it.createdAt ?: "" }
                    emit(Result.success(articles))
                } else {
                    emit(Result.failure(Exception("Empty search response")))
                }
            } else {
                emit(Result.failure(Exception("Search API Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            // 网络失败时从缓存搜索
            try {
                articleDao.searchArticles(query).collect { cachedArticles ->
                    if (cachedArticles.isNotEmpty()) {
                        emit(Result.success(cachedArticles.map { it.toArticle() }))
                    } else {
                        emit(Result.failure(e))
                    }
                }
            } catch (cacheError: Exception) {
                emit(Result.failure(e))
            }
        }
    }

    override fun getTrendingArticles(): Flow<Result<List<Article>>> = flow {
        try {
            val response = apiService.getTrendingArticles(limit = 10)
            if (response.isSuccessful) {
                val trendingResponse = response.body()
                if (trendingResponse != null && trendingResponse.articles.isNotEmpty()) {
                    // Use backend Article model directly, just update image URLs
                    // Sort by published date (newest first)
                    val convertedArticles = trendingResponse.articles
                        .map { article ->
                            article.copy(
                                thumbnailUrl = imageUrlHelper.getAbsoluteImageUrl(article.thumbnailUrl)
                            )
                        }
                        .sortedByDescending { it.publishedAt ?: it.createdAt ?: "" }
                    emit(Result.success(convertedArticles))
                } else {
                    emit(Result.failure(Exception("Empty trending articles response")))
                }
            } else {
                emit(Result.failure(Exception("Trending API Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getMatches(date: String?, status: String?): Flow<Result<List<Match>>> = flow {
        try {
            val response = apiService.getMatches(status = status, date = date, limit = 20)
            if (response.isSuccessful) {
                val matches = response.body()
                if (matches != null) {
                    emit(Result.success(matches))
                } else {
                    emit(Result.failure(Exception("Empty matches response")))
                }
            } else {
                emit(Result.failure(Exception("Matches API Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getLiveMatches(): Flow<Result<List<Match>>> = flow {
        try {
            val response = apiService.getLiveMatches()
            if (response.isSuccessful) {
                val matches = response.body()
                if (matches != null) {
                    emit(Result.success(matches))
                } else {
                    emit(Result.failure(Exception("Empty live matches response")))
                }
            } else {
                emit(Result.failure(Exception("Live Matches API Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getTodayMatches(): Flow<Result<List<Match>>> = flow {
        try {
            val today = java.time.LocalDate.now().toString()
            val response = apiService.getMatches(date = today, limit = 20)
            if (response.isSuccessful) {
                val matches = response.body()
                if (matches != null) {
                    emit(Result.success(matches))
                } else {
                    emit(Result.failure(Exception("Empty today matches response")))
                }
            } else {
                emit(Result.failure(Exception("Today Matches API Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getVideos(category: String, page: Int): Flow<Result<List<Video>>> = flow {
        try {
            val response = apiService.getVideos(limit = 20)
            if (response.isSuccessful) {
                val videos = response.body()
                if (videos != null) {
                    emit(Result.success(videos))
                } else {
                    emit(Result.failure(Exception("Empty videos response")))
                }
            } else {
                emit(Result.failure(Exception("Videos API Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun searchVideos(query: String, page: Int): Flow<Result<List<Video>>> = flow {
        try {
            val response = apiService.searchContent(keyword = query, type = "videos", limit = 20)
            if (response.isSuccessful) {
                val searchResponse = response.body()
                if (searchResponse != null) {
                    emit(Result.success(searchResponse.videos))
                } else {
                    emit(Result.failure(Exception("Empty video search response")))
                }
            } else {
                emit(Result.failure(Exception("Video Search API Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getTrendingVideos(): Flow<Result<List<Video>>> = flow {
        try {
            val response = apiService.getHighlightVideos(limit = 10)
            if (response.isSuccessful) {
                val videos = response.body()
                if (videos != null) {
                    emit(Result.success(videos))
                } else {
                    emit(Result.failure(Exception("Empty trending videos response")))
                }
            } else {
                emit(Result.failure(Exception("Trending Videos API Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    // ============= Paging 3 Implementation =============
    
    /**
     * Lấy danh sách bài viết với Paging 3
     * @param categoryId ID của category, null nếu lấy tất cả
     */
    override fun getArticlesPaged(categoryId: Int?): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = ApiConfig.DEFAULT_PAGE_SIZE,
                enablePlaceholders = false,
                initialLoadSize = ApiConfig.DEFAULT_PAGE_SIZE
            ),
            pagingSourceFactory = {
                ArticlesPagingSource(
                    apiService = apiService,
                    imageUrlHelper = imageUrlHelper,
                    categoryId = categoryId
                )
            }
        ).flow
    }
    
    /**
     * Tìm kiếm bài viết với Paging 3
     * @param query Từ khóa tìm kiếm
     */
    override fun searchArticlesPaged(query: String): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                pageSize = 50,
                enablePlaceholders = false,
                initialLoadSize = 50
            ),
            pagingSourceFactory = {
                SearchArticlesPagingSource(
                    apiService = apiService,
                    imageUrlHelper = imageUrlHelper,
                    query = query
                )
            }
        ).flow
    }
    
    /**
     * Tăng lượt xem bài viết
     * @param articleId ID của bài viết
     */
    override fun incrementArticleView(articleId: Int): Flow<Result<Unit>> = flow {
        try {
            val response = apiService.trackArticleView(articleId)
            if (response.isSuccessful) {
                emit(Result.success(Unit))
            } else {
                emit(Result.failure(Exception("Failed to track view: ${response.code()}")))
            }
        } catch (e: Exception) {
            // Không cần hiển thị lỗi cho user, chỉ log
            android.util.Log.w("NewsRepositoryImpl", "Failed to track article view: ${e.message}")
            emit(Result.failure(e))
        }
    }
}
