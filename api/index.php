<?php
/**
 * Sports News API - Single Entry Point
 * Version: 2.0
 * 
 * Modern RESTful API với clean URLs (không có .php extension)
 */

// Error reporting (tắt trong production)
error_reporting(E_ALL);
ini_set('display_errors', 0); // Tắt hiển thị lỗi trực tiếp
ini_set('log_errors', 1);
ini_set('error_log', __DIR__ . '/logs/php_errors.log');

// Set timezone
date_default_timezone_set('Asia/Ho_Chi_Minh');

// CORS Headers
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');
header('Content-Type: application/json; charset=utf-8');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Autoloader
spl_autoload_register(function ($class) {
    $paths = [
        __DIR__ . '/core/' . $class . '.php',
        __DIR__ . '/controllers/' . $class . '.php',
        __DIR__ . '/models/' . $class . '.php',
        __DIR__ . '/middleware/' . $class . '.php',
        __DIR__ . '/config/' . $class . '.php',
    ];
    
    foreach ($paths as $path) {
        if (file_exists($path)) {
            require_once $path;
            return;
        }
    }
});

// Load required files
require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/config/ResponseHelper.php';
require_once __DIR__ . '/config/RateLimiter.php';
require_once __DIR__ . '/core/Router.php';
require_once __DIR__ . '/core/Controller.php';

// Global error handler
set_exception_handler(function($exception) {
    error_log("Uncaught Exception: " . $exception->getMessage() . " in " . $exception->getFile() . ":" . $exception->getLine());
    
    ResponseHelper::error(
        'Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.',
        500,
        [
            'error_code' => 'INTERNAL_ERROR',
            'timestamp' => date('Y-m-d H:i:s')
        ]
    );
});

// Global error handler for PHP errors
set_error_handler(function($severity, $message, $file, $line) {
    if (!(error_reporting() & $severity)) {
        return false;
    }
    
    error_log("PHP Error [$severity]: $message in $file:$line");
    
    // Don't show error to user, just log it
    return true;
});

try {
    // Apply global rate limiting (120 requests per minute)
    RateLimiter::check(null, 120, 60);
    
    // Initialize router
    $router = new Router();
    
    // Load routes
    require_once __DIR__ . '/routes.php';
    
    // Dispatch request
    $router->dispatch();
    
} catch (Exception $e) {
    error_log("Fatal Error: " . $e->getMessage() . " in " . $e->getFile() . ":" . $e->getLine());
    
    ResponseHelper::error(
        'Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.',
        500,
        [
            'error_code' => 'FATAL_ERROR',
            'timestamp' => date('Y-m-d H:i:s')
        ]
    );
}
?>

