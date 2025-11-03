package com.nhd.news.data.api

import com.nhd.news.data.models.Category
import retrofit2.Response
import retrofit2.http.*

interface CategoryApiService {
    
    @GET("categories")
    suspend fun getAllCategories(): Response<CategoriesResponse>
    
    @GET("categories/{id}")
    suspend fun getCategoryById(
        @Path("id") categoryId: Int
    ): Response<CategoryDetailResponse>
}

data class CategoriesResponse(
    val categories: List<Category>
)

data class CategoryDetailResponse(
    val success: Boolean,
    val data: Category?,
    val message: String? = null
)

