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
     * Lấy tất cả category
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
                    emit(Result.failure(Exception("Không thể lấy danh sách category")))
                }
            } else {
                emit(Result.failure(Exception("Lỗi API: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Lấy category theo ID
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
                        emit(Result.failure(Exception("Không tìm thấy category")))
                    }
                } else {
                    emit(Result.failure(Exception(categoryResponse?.message ?: "Không thể lấy category")))
                }
            } else {
                emit(Result.failure(Exception("Lỗi API: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}

