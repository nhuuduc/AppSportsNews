<?php
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../config/ResponseHelper.php';

class AuthMiddleware {
    /**
     * Authenticate user from session token
     * Returns user data if valid, otherwise exits with error
     */
    public static function authenticate($debug = false) {
        // Get token from Authorization header (support multiple formats)
        $auth_header = '';
        $source = 'none';
        
        // Try different ways to get Authorization header
        if (isset($_SERVER['HTTP_AUTHORIZATION'])) {
            $auth_header = $_SERVER['HTTP_AUTHORIZATION'];
            $source = 'HTTP_AUTHORIZATION';
        } elseif (isset($_SERVER['REDIRECT_HTTP_AUTHORIZATION'])) {
            $auth_header = $_SERVER['REDIRECT_HTTP_AUTHORIZATION'];
            $source = 'REDIRECT_HTTP_AUTHORIZATION';
        } elseif (isset($_SERVER['Authorization'])) {
            $auth_header = $_SERVER['Authorization'];
            $source = 'Authorization';
        } elseif (function_exists('apache_request_headers')) {
            $headers = apache_request_headers();
            $auth_header = $headers['Authorization'] ?? $headers['authorization'] ?? '';
            if ($auth_header) {
                $source = 'apache_request_headers';
            }
        }
        
        // Debug mode (only for development)
        if ($debug) {
            error_log("Auth Debug - Source: $source, Header: $auth_header");
        }
        
        $token = '';
        
        if (preg_match('/Bearer\s+(.*)$/i', $auth_header, $matches)) {
            $token = trim($matches[1]);
        }
        
        if (empty($token)) {
            // Enhanced error message for debugging
            $error_msg = 'Vui lòng đăng nhập';
            if ($debug) {
                $error_msg .= " (No token found from: $source)";
            }
            ResponseHelper::error($error_msg, 401);
            exit();
        }
        
        // Verify session
        try {
            $database = Database::getInstance();
            $conn = $database->getConnection();
            
            $stmt = $conn->prepare(
                "SELECT u.user_id, u.email, u.username, u.full_name, u.avatar_url, 
                        u.role, u.email_verified_at, u.is_active
                 FROM user_sessions s
                 JOIN users u ON s.user_id = u.user_id
                 WHERE s.session_token = ? AND s.expires_at > NOW()"
            );
            $stmt->execute([$token]);
            $user = $stmt->fetch();
            
            if (!$user) {
                ResponseHelper::error('Session không hợp lệ hoặc đã hết hạn', 401);
                exit();
            }
            
            if (!$user['is_active']) {
                ResponseHelper::error('Tài khoản đã bị khóa', 403);
                exit();
            }
            
            return $user;
            
        } catch (Exception $e) {
            ResponseHelper::error('Lỗi xác thực: ' . $e->getMessage(), 500);
            exit();
        }
    }
    
    /**
     * Check if user has required role
     */
    public static function requireRole($required_role) {
        $user = self::authenticate();
        
        $role_hierarchy = [
            'user' => 1,
            'editor' => 2,
            'admin' => 3
        ];
        
        $user_level = $role_hierarchy[$user['role']] ?? 0;
        $required_level = $role_hierarchy[$required_role] ?? 99;
        
        if ($user_level < $required_level) {
            ResponseHelper::error('Bạn không có quyền thực hiện thao tác này', 403);
            exit();
        }
        
        return $user;
    }
}
?>

