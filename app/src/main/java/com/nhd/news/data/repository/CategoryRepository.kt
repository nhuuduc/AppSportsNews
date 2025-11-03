package com.nhd.news.data.repository

import com.nhd.news.data.api.CategoryApiService
import com.nhd.news.data.models.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryApiService: CategoryApiService
) {
    
    /**
     * 获取所有分类
     */
    fun getAllCategories(): Flow<Result<List<Category>>> = flow {
        try {
            val response = categoryApiService.getAllCategories()
            if (response.isSuccessful) {
                val categoriesResponse = response.body()
                if (categoriesResponse != null) {
                    val categories = categoriesResponse.categories
                    emit(Result.success(categories))
                } else {
                    emit(Result.failure(Exception("获取分类失败")))
                }
            } else {
                emit(Result.failure(Exception("API错误: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * 根据ID获取分类
     */
    fun getCategoryById(categoryId: Int): Flow<Result<Category>> = flow {
        try {
            val response = categoryApiService.getCategoryById(categoryId)
            if (response.isSuccessful) {
                val categoryResponse = response.body()
                if (categoryResponse != null && categoryResponse.success) {
                    val category = categoryResponse.data
                    if (category != null) {
                        emit(Result.success(category))
                    } else {
                        emit(Result.failure(Exception("未找到分类")))
                    }
                } else {
                    emit(Result.failure(Exception(categoryResponse?.message ?: "获取分类失败")))
                }
            } else {
                emit(Result.failure(Exception("API错误: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}

