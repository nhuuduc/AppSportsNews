<?php
/**
 * Base Controller
 */
abstract class Controller {
    protected $db;
    
    public function __construct() {
        $database = Database::getInstance();
        $this->db = $database->getConnection();
    }
    
    /**
     * Get request data (JSON or form)
     */
    protected function getRequestData() {
        $input = file_get_contents('php://input');
        $data = json_decode($input, true) ?: [];
        return array_merge($_POST, $data);
    }
    
    /**
     * Get query parameters
     */
    protected function getQueryParams() {
        return $_GET;
    }
    
    /**
     * Get authenticated user
     */
    protected function getAuthUser() {
        return AuthMiddleware::authenticate();
    }
    
    /**
     * Get authenticated user (optional - returns null if not authenticated)
     */
    protected function getOptionalAuthUser() {
        try {
            // Try different ways to get Authorization header
            $auth_header = $_SERVER['HTTP_AUTHORIZATION'] 
                ?? $_SERVER['REDIRECT_HTTP_AUTHORIZATION'] 
                ?? $_SERVER['Authorization'] 
                ?? '';
            
            if (function_exists('apache_request_headers') && empty($auth_header)) {
                $headers = apache_request_headers();
                $auth_header = $headers['Authorization'] ?? $headers['authorization'] ?? '';
            }
            
            $token = '';
            if (preg_match('/Bearer\s+(.*)$/i', $auth_header, $matches)) {
                $token = trim($matches[1]);
            }
            
            if (empty($token)) {
                return null;
            }
            
            // Verify session
            $stmt = $this->db->prepare(
                "SELECT u.user_id, u.email, u.username, u.full_name, u.avatar_url, 
                        u.role, u.email_verified_at, u.is_active
                 FROM user_sessions s
                 JOIN users u ON s.user_id = u.user_id
                 WHERE s.session_token = ? AND s.expires_at > NOW()"
            );
            $stmt->execute([$token]);
            $user = $stmt->fetch();
            
            return $user ?: null;
        } catch (Exception $e) {
            return null;
        }
    }
    
    /**
     * Check if article is liked by user
     */
    protected function checkArticleLiked($article_id, $user_id) {
        if (!$user_id) return false;
        
        try {
            $stmt = $this->db->prepare(
                "SELECT liked_user_ids FROM article_views WHERE article_id = ?"
            );
            $stmt->execute([$article_id]);
            $result = $stmt->fetch(PDO::FETCH_ASSOC);
            
            if ($result && !empty($result['liked_user_ids'])) {
                $liked_users = explode(',', $result['liked_user_ids']);
                // Convert to int for proper comparison
                $liked_users = array_map('intval', $liked_users);
                return in_array((int)$user_id, $liked_users, true);
            }
            
            return false;
        } catch (Exception $e) {
            return false;
        }
    }
    
    /**
     * Success response
     */
    protected function success($data = [], $message = null, $status = 200) {
        ResponseHelper::success($data, $message, $status);
    }
    
    /**
     * Error response
     */
    protected function error($message, $status = 400, $details = null) {
        ResponseHelper::error($message, $status, $details);
    }
    
    /**
     * JSON response
     */
    protected function json($data, $status = 200, $cache_time = 0) {
        ResponseHelper::sendJson($data, $status, $cache_time);
    }
}
?>

