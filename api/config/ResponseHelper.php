<?php
/**
 * Response Helper - Handle caching, compression, and JSON responses
 */
class ResponseHelper {
    
    /**
     * Send JSON response with optimizations
     */
    public static function sendJson($data, $status_code = 200, $cache_time = 0) {
        http_response_code($status_code);
        
        // Set cache headers
        if ($cache_time > 0) {
            header("Cache-Control: public, max-age=" . $cache_time);
            header("Expires: " . gmdate('D, d M Y H:i:s', time() + $cache_time) . ' GMT');
            
            // Generate ETag
            $etag = md5(json_encode($data));
            header("ETag: \"$etag\"");
            
            // Check if client has cached version
            if (isset($_SERVER['HTTP_IF_NONE_MATCH']) && $_SERVER['HTTP_IF_NONE_MATCH'] === "\"$etag\"") {
                http_response_code(304);
                exit;
            }
        } else {
            header("Cache-Control: no-cache, must-revalidate");
            header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
        }
        
        // Set content type header FIRST
        header("Content-Type: application/json; charset=utf-8");
        
        // DISABLE GZIP for now - send plain JSON to avoid Android parsing issues
        $json_output = json_encode($data, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
        echo $json_output;
        exit;
    }
    
    /**
     * Send error response
     */
    public static function sendError($message, $status_code = 400, $details = null) {
        $response = ["error" => true, "message" => $message];
        if ($details !== null) {
            $response["details"] = $details;
        }
        self::sendJson($response, $status_code, 0);
    }
    
    /**
     * Send error response (alias for sendError)
     */
    public static function error($message, $status_code = 400, $details = null) {
        self::sendError($message, $status_code, $details);
    }
    
    /**
     * Send success response
     */
    public static function success($data = [], $message = null, $status_code = 200) {
        $response = ["success" => true];
        
        if ($message !== null) {
            $response["message"] = $message;
        }
        
        // Merge data into response
        if (is_array($data)) {
            $response = array_merge($response, $data);
        } else {
            $response["data"] = $data;
        }
        
        self::sendJson($response, $status_code, 0);
    }
    
    /**
     * Simple in-memory cache (for single request)
     */
    private static $cache = [];
    
    public static function cache($key, $callback, $ttl = 300) {
        // Check in-memory cache first
        if (isset(self::$cache[$key])) {
            return self::$cache[$key];
        }
        
        // Try file cache (optional - can be replaced with Redis/Memcached)
        $cache_file = sys_get_temp_dir() . '/api_cache_' . md5($key);
        
        if (file_exists($cache_file) && (time() - filemtime($cache_file)) < $ttl) {
            $data = unserialize(file_get_contents($cache_file));
            self::$cache[$key] = $data;
            return $data;
        }
        
        // Generate fresh data
        $data = $callback();
        
        // Store in caches
        self::$cache[$key] = $data;
        @file_put_contents($cache_file, serialize($data), LOCK_EX);
        
        return $data;
    }
    
    /**
     * Clear cache
     */
    public static function clearCache($pattern = '*') {
        $cache_dir = sys_get_temp_dir();
        $files = glob($cache_dir . '/api_cache_*');
        foreach ($files as $file) {
            @unlink($file);
        }
    }
    
    /**
     * Clear cache by pattern
     * Xóa cache theo pattern cụ thể (articles, comments, etc.)
     */
    public static function clearCacheByPattern($patterns = []) {
        $cache_dir = sys_get_temp_dir();
        $files = glob($cache_dir . '/api_cache_*');
        
        foreach ($files as $file) {
            $filename = basename($file);
            
            foreach ($patterns as $pattern) {
                if (strpos($filename, $pattern) !== false) {
                    @unlink($file);
                    break; // Đã xóa rồi thì không cần check pattern khác
                }
            }
        }
    }
    
    /**
     * Clear specific cache by key
     */
    public static function clearCacheByKey($key) {
        $cache_file = sys_get_temp_dir() . '/api_cache_' . md5($key);
        @unlink($cache_file);
    }
}
?>

