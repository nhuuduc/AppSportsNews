package com.nhd.news.utils

/**
 * Sealed class đại diện cho các loại lỗi trong ứng dụng
 * Giúp xử lý lỗi một cách có tổ chức và hiển thị thông báo phù hợp
 */
sealed class AppError(
    open val message: String,
    open val cause: Throwable? = null
) {
    /**
     * Lỗi liên quan đến mạng
     */
    sealed class NetworkError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        
        /** Không có kết nối internet */
        data class NoConnection(
            override val cause: Throwable? = null
        ) : NetworkError("Không có kết nối internet. Vui lòng kiểm tra kết nối của bạn.", cause)
        
        /** Timeout khi gọi API */
        data class Timeout(
            override val cause: Throwable? = null
        ) : NetworkError("Kết nối quá chậm. Vui lòng thử lại.", cause)
        
        /** Không thể kết nối đến server */
        data class ConnectionFailed(
            override val cause: Throwable? = null
        ) : NetworkError("Không thể kết nối đến server. Vui lòng thử lại sau.", cause)
        
        /** Lỗi mạng không xác định */
        data class Unknown(
            override val message: String = "Lỗi mạng không xác định",
            override val cause: Throwable? = null
        ) : NetworkError(message, cause)
    }
    
    /**
     * Lỗi từ server (HTTP errors)
     */
    sealed class ServerError(
        override val message: String,
        val code: Int,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        
        /** 400 - Bad Request */
        data class BadRequest(
            override val message: String = "Yêu cầu không hợp lệ",
            override val cause: Throwable? = null
        ) : ServerError(message, 400, cause)
        
        /** 401 - Unauthorized */
        data class Unauthorized(
            override val message: String = "Vui lòng đăng nhập để tiếp tục",
            override val cause: Throwable? = null
        ) : ServerError(message, 401, cause)
        
        /** 403 - Forbidden */
        data class Forbidden(
            override val message: String = "Bạn không có quyền truy cập",
            override val cause: Throwable? = null
        ) : ServerError(message, 403, cause)
        
        /** 404 - Not Found */
        data class NotFound(
            override val message: String = "Không tìm thấy dữ liệu",
            override val cause: Throwable? = null
        ) : ServerError(message, 404, cause)
        
        /** 500 - Internal Server Error */
        data class InternalServerError(
            override val message: String = "Lỗi server. Vui lòng thử lại sau",
            override val cause: Throwable? = null
        ) : ServerError(message, 500, cause)
        
        /** 503 - Service Unavailable */
        data class ServiceUnavailable(
            override val message: String = "Dịch vụ tạm thời không khả dụng",
            override val cause: Throwable? = null
        ) : ServerError(message, 503, cause)
        
        /** Lỗi server khác */
        data class Unknown(
            override val message: String,
            val httpCode: Int,
            override val cause: Throwable? = null
        ) : ServerError(message, httpCode, cause)
    }
    
    /**
     * Lỗi xác thực (authentication)
     */
    sealed class AuthError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        
        /** Token hết hạn */
        data class TokenExpired(
            override val message: String = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại",
            override val cause: Throwable? = null
        ) : AuthError(message, cause)
        
        /** Token không hợp lệ */
        data class InvalidToken(
            override val message: String = "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại",
            override val cause: Throwable? = null
        ) : AuthError(message, cause)
        
        /** Chưa đăng nhập */
        data class NotAuthenticated(
            override val message: String = "Vui lòng đăng nhập để tiếp tục",
            override val cause: Throwable? = null
        ) : AuthError(message, cause)
        
        /** Sai mật khẩu hoặc username */
        data class InvalidCredentials(
            override val message: String = "Tên đăng nhập hoặc mật khẩu không đúng",
            override val cause: Throwable? = null
        ) : AuthError(message, cause)
    }
    
    /**
     * Lỗi validate dữ liệu
     */
    sealed class ValidationError(
        override val message: String,
        override val cause: Throwable? = null
    ) : AppError(message, cause) {
        
        /** Dữ liệu trống */
        data class EmptyData(
            override val message: String = "Dữ liệu không được để trống",
            override val cause: Throwable? = null
        ) : ValidationError(message, cause)
        
        /** Dữ liệu không hợp lệ */
        data class InvalidData(
            override val message: String = "Dữ liệu không hợp lệ",
            override val cause: Throwable? = null
        ) : ValidationError(message, cause)
        
        /** Email không hợp lệ */
        data class InvalidEmail(
            override val message: String = "Email không hợp lệ",
            override val cause: Throwable? = null
        ) : ValidationError(message, cause)
        
        /** Mật khẩu quá ngắn */
        data class PasswordTooShort(
            override val message: String = "Mật khẩu phải có ít nhất 6 ký tự",
            override val cause: Throwable? = null
        ) : ValidationError(message, cause)
        
        /** Mật khẩu không khớp */
        data class PasswordMismatch(
            override val message: String = "Mật khẩu không khớp",
            override val cause: Throwable? = null
        ) : ValidationError(message, cause)
    }
    
    /**
     * Lỗi không xác định
     */
    data class Unknown(
        override val message: String = "Đã xảy ra lỗi không xác định",
        override val cause: Throwable? = null
    ) : AppError(message, cause)
}

/**
 * Extension function để chuyển Exception thành AppError
 */
fun Throwable.toAppError(): AppError {
    return when (this) {
        is java.net.UnknownHostException -> AppError.NetworkError.NoConnection(this)
        is java.net.SocketTimeoutException -> AppError.NetworkError.Timeout(this)
        is java.net.ConnectException -> AppError.NetworkError.ConnectionFailed(this)
        is java.io.IOException -> AppError.NetworkError.Unknown(cause = this)
        else -> AppError.Unknown(message = this.message ?: "Lỗi không xác định", cause = this)
    }
}

/**
 * Extension function để chuyển HTTP code thành ServerError
 */
fun Int.toServerError(message: String? = null, cause: Throwable? = null): AppError.ServerError {
    return when (this) {
        400 -> AppError.ServerError.BadRequest(message ?: "Yêu cầu không hợp lệ", cause)
        401 -> AppError.ServerError.Unauthorized(message ?: "Vui lòng đăng nhập để tiếp tục", cause)
        403 -> AppError.ServerError.Forbidden(message ?: "Bạn không có quyền truy cập", cause)
        404 -> AppError.ServerError.NotFound(message ?: "Không tìm thấy dữ liệu", cause)
        500 -> AppError.ServerError.InternalServerError(message ?: "Lỗi server", cause)
        503 -> AppError.ServerError.ServiceUnavailable(message ?: "Dịch vụ không khả dụng", cause)
        else -> AppError.ServerError.Unknown(message ?: "Lỗi server ($this)", this, cause)
    }
}

