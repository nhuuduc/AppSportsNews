package com.nhd.news.utils

/**
 * Sealed class đại diện cho trạng thái của data loading
 * Hỗ trợ Loading, Success, và Error states với AppError
 */
sealed class Resource<out T> {
    /**
     * Đang loading dữ liệu
     */
    object Loading : Resource<Nothing>()
    
    /**
     * Load dữ liệu thành công
     */
    data class Success<T>(val data: T) : Resource<T>()
    
    /**
     * Load dữ liệu thất bại
     */
    data class Error(val error: AppError) : Resource<Nothing>()
    
    /**
     * Kiểm tra xem resource có đang loading không
     */
    val isLoading: Boolean
        get() = this is Loading
    
    /**
     * Kiểm tra xem resource có thành công không
     */
    val isSuccess: Boolean
        get() = this is Success
    
    /**
     * Kiểm tra xem resource có lỗi không
     */
    val isError: Boolean
        get() = this is Error
    
    /**
     * Lấy data nếu success, null nếu không
     */
    fun getDataOrNull(): T? {
        return if (this is Success) data else null
    }
    
    /**
     * Lấy error nếu error, null nếu không
     */
    fun getErrorOrNull(): AppError? {
        return if (this is Error) error else null
    }
}

/**
 * Extension function để chuyển Result<T> thành Resource<T>
 */
fun <T> Result<T>.toResource(): Resource<T> {
    return fold(
        onSuccess = { Resource.Success(it) },
        onFailure = { Resource.Error(it.toAppError()) }
    )
}

/**
 * Extension function để chuyển Resource thành Result
 */
fun <T> Resource<T>.toResult(): Result<T>? {
    return when (this) {
        is Resource.Success -> Result.success(data)
        is Resource.Error -> Result.failure(Exception(error.message, error.cause))
        is Resource.Loading -> null
    }
}

