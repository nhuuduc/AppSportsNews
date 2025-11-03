<?php
/**
 * Simple Rate Limiter to prevent API abuse
 */
class RateLimiter {
    private static $storage_dir;
    
    public static function init() {
        self::$storage_dir = sys_get_temp_dir() . '/rate_limit/';
        if (!is_dir(self::$storage_dir)) {
            @mkdir(self::$storage_dir, 0777, true);
        }
    }
    
    /**
     * Check rate limit
     * @param string $identifier - IP or user ID
     * @param int $max_requests - Maximum requests allowed
     * @param int $window - Time window in seconds
     * @return bool - True if allowed, False if exceeded
     */
    public static function check($identifier = null, $max_requests = 60, $window = 60) {
        self::init();
        
        if ($identifier === null) {
            $identifier = self::getClientIdentifier();
        }
        
        $file = self::$storage_dir . md5($identifier);
        $now = time();
        
        // Load existing requests
        $requests = [];
        if (file_exists($file)) {
            $requests = unserialize(file_get_contents($file));
        }
        
        // Remove old requests outside the window
        $requests = array_filter($requests, function($timestamp) use ($now, $window) {
            return ($now - $timestamp) < $window;
        });
        
        // Check if limit exceeded
        if (count($requests) >= $max_requests) {
            $oldest = min($requests);
            $retry_after = $window - ($now - $oldest);
            
            header("X-RateLimit-Limit: $max_requests");
            header("X-RateLimit-Remaining: 0");
            header("X-RateLimit-Reset: " . ($oldest + $window));
            header("Retry-After: $retry_after");
            
            http_response_code(429);
            die(json_encode([
                "error" => true,
                "message" => "Rate limit exceeded. Too many requests.",
                "retry_after" => $retry_after
            ]));
        }
        
        // Add current request
        $requests[] = $now;
        file_put_contents($file, serialize($requests), LOCK_EX);
        
        // Set rate limit headers
        header("X-RateLimit-Limit: $max_requests");
        header("X-RateLimit-Remaining: " . ($max_requests - count($requests)));
        header("X-RateLimit-Reset: " . ($now + $window));
        
        return true;
    }
    
    /**
     * Get client identifier (IP address with proxy support)
     */
    private static function getClientIdentifier() {
        $ip = $_SERVER['REMOTE_ADDR'];
        
        // Check for proxy headers
        if (!empty($_SERVER['HTTP_CLIENT_IP'])) {
            $ip = $_SERVER['HTTP_CLIENT_IP'];
        } elseif (!empty($_SERVER['HTTP_X_FORWARDED_FOR'])) {
            $ip = explode(',', $_SERVER['HTTP_X_FORWARDED_FOR'])[0];
        }
        
        return $ip;
    }
    
    /**
     * Clean old rate limit files (cron job)
     */
    public static function cleanup() {
        self::init();
        $files = glob(self::$storage_dir . '*');
        $now = time();
        
        foreach ($files as $file) {
            if (($now - filemtime($file)) > 3600) { // Older than 1 hour
                @unlink($file);
            }
        }
    }
}
?>

