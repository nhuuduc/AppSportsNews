package com.nhd.news.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nhd.news.data.api.ApiConfig
import com.nhd.news.data.api.ApiService
import com.nhd.news.data.models.Article
import com.nhd.news.utils.ImageUrlHelper
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * PagingSource cho danh sách bài viết
 * Hỗ trợ load dữ liệu theo trang từ API
 */
class ArticlesPagingSource @Inject constructor(
    private val apiService: ApiService,
    private val imageUrlHelper: ImageUrlHelper,
    private val categoryId: Int? = null
) : PagingSource<Int, Article>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: 1
        
        return try {
            val response = if (categoryId != null) {
                apiService.getArticlesByCategory(
                    categoryId = categoryId,
                    page = page,
                    limit = params.loadSize
                )
            } else {
                apiService.getArticles(
                    page = page,
                    limit = params.loadSize
                )
            }
            
            if (response.isSuccessful) {
                val articleResponse = response.body()
                
                if (articleResponse != null) {
                    // Cập nhật image URLs và sắp xếp theo ngày
                    val articles = articleResponse.articles
                        .map { article ->
                            article.copy(
                                thumbnailUrl = imageUrlHelper.getAbsoluteImageUrl(article.thumbnailUrl)
                            )
                        }
                        .sortedByDescending { it.publishedAt ?: it.createdAt ?: "" }
                    
                    LoadResult.Page(
                        data = articles,
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = if (page >= articleResponse.totalPages) null else page + 1
                    )
                } else {
                    LoadResult.Error(Exception("Empty response"))
                }
            } else {
                LoadResult.Error(HttpException(response))
            }
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}

