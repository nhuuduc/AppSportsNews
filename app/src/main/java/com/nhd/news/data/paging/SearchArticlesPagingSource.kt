package com.nhd.news.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.nhd.news.data.api.ApiService
import com.nhd.news.data.models.Article
import com.nhd.news.utils.ImageUrlHelper
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * PagingSource cho tìm kiếm bài viết
 */
class SearchArticlesPagingSource @Inject constructor(
    private val apiService: ApiService,
    private val imageUrlHelper: ImageUrlHelper,
    private val query: String
) : PagingSource<Int, Article>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        // Search API thường không hỗ trợ paging, nên load tất cả 1 lần
        val page = params.key ?: 1
        
        // Chỉ load ở page 1, các page khác return empty
        if (page != 1) {
            return LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null
            )
        }
        
        return try {
            val response = apiService.searchContent(
                keyword = query,
                type = "articles",
                limit = 50 // Load nhiều kết quả hơn cho search
            )
            
            if (response.isSuccessful) {
                val searchResponse = response.body()
                
                if (searchResponse != null) {
                    val articles = searchResponse.articles
                        .map { article ->
                            article.copy(
                                thumbnailUrl = imageUrlHelper.getAbsoluteImageUrl(article.thumbnailUrl)
                            )
                        }
                        .sortedByDescending { it.publishedAt ?: it.createdAt ?: "" }
                    
                    LoadResult.Page(
                        data = articles,
                        prevKey = null,
                        nextKey = null // Không có page tiếp theo
                    )
                } else {
                    LoadResult.Error(Exception("Empty search response"))
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
        return null // Luôn refresh từ đầu
    }
}

