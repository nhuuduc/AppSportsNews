<?php
/**
 * Router - Simple and fast routing system
 */
class Router {
    private $routes = [];
    private $middlewares = [];
    
    /**
     * Add a GET route
     */
    public function get($path, $handler, $middleware = []) {
        $this->addRoute('GET', $path, $handler, $middleware);
    }
    
    /**
     * Add a POST route
     */
    public function post($path, $handler, $middleware = []) {
        $this->addRoute('POST', $path, $handler, $middleware);
    }
    
    /**
     * Add a PUT route
     */
    public function put($path, $handler, $middleware = []) {
        $this->addRoute('PUT', $path, $handler, $middleware);
    }
    
    /**
     * Add a DELETE route
     */
    public function delete($path, $handler, $middleware = []) {
        $this->addRoute('DELETE', $path, $handler, $middleware);
    }
    
    /**
     * Add a route with any method
     */
    private function addRoute($method, $path, $handler, $middleware = []) {
        $this->routes[] = [
            'method' => $method,
            'path' => $path,
            'handler' => $handler,
            'middleware' => $middleware,
            'pattern' => $this->pathToRegex($path)
        ];
    }
    
    /**
     * Add global middleware
     */
    public function middleware($middleware) {
        $this->middlewares[] = $middleware;
    }
    
    /**
     * Convert path to regex pattern
     */
    private function pathToRegex($path) {
        // Replace :param with named capture group
        $pattern = preg_replace('/\/:([^\/]+)/', '/(?<$1>[^/]+)', $path);
        // Escape forward slashes
        $pattern = str_replace('/', '\/', $pattern);
        // Make trailing slash optional
        $pattern = rtrim($pattern, '\/') . '\/?';
        return '/^' . $pattern . '$/';
    }
    
    /**
     * Dispatch the request
     */
    public function dispatch() {
        $method = $_SERVER['REQUEST_METHOD'];
        $uri = $_SERVER['REQUEST_URI'];
        
        // Remove query string
        $uri = strtok($uri, '?');
        
        // Remove base path (e.g., /api) if present
        $scriptName = dirname($_SERVER['SCRIPT_NAME']);
        if ($scriptName !== '/' && strpos($uri, $scriptName) === 0) {
            $uri = substr($uri, strlen($scriptName));
        }
        
        // Normalize: ensure starts with / and remove trailing /
        $uri = '/' . trim($uri, '/');
        if ($uri !== '/') {
            $uri = rtrim($uri, '/');
        }
        
        // Match route
        foreach ($this->routes as $route) {
            if ($route['method'] !== $method) {
                continue;
            }
            
            if (preg_match($route['pattern'], $uri, $matches)) {
                // Extract named parameters
                $params = array_filter($matches, 'is_string', ARRAY_FILTER_USE_KEY);
                
                // Execute global middlewares
                foreach ($this->middlewares as $middleware) {
                    $this->executeMiddleware($middleware);
                }
                
                // Execute route middlewares
                foreach ($route['middleware'] as $middleware) {
                    $this->executeMiddleware($middleware);
                }
                
                // Execute handler
                $this->executeHandler($route['handler'], $params);
                return;
            }
        }
        
        // No route found - Log for debugging
        error_log("Route not found: $method $uri");
        ResponseHelper::error('Endpoint không tồn tại', 404, [
            'requested_method' => $method,
            'requested_path' => $uri
        ]);
    }
    
    /**
     * Execute middleware
     */
    private function executeMiddleware($middleware) {
        if (is_callable($middleware)) {
            call_user_func($middleware);
        } elseif (is_string($middleware)) {
            // Call static method like 'AuthMiddleware::authenticate'
            if (strpos($middleware, '::') !== false) {
                list($class, $method) = explode('::', $middleware);
                call_user_func([$class, $method]);
            }
        } elseif (is_array($middleware)) {
            // Array format: ['Class', 'method'] or ['Class', 'method', 'param']
            if (count($middleware) == 3) {
                call_user_func([$middleware[0], $middleware[1]], $middleware[2]);
            } else {
                call_user_func($middleware);
            }
        }
    }
    
    /**
     * Execute handler
     */
    private function executeHandler($handler, $params = []) {
        if (is_callable($handler)) {
            call_user_func($handler, $params);
        } elseif (is_string($handler)) {
            // Format: 'ControllerName@method'
            if (strpos($handler, '@') !== false) {
                list($controller, $method) = explode('@', $handler);
                $controllerInstance = new $controller();
                call_user_func([$controllerInstance, $method], $params);
            }
        } elseif (is_array($handler)) {
            // Array format: ['Controller', 'method']
            $controllerInstance = new $handler[0]();
            call_user_func([$controllerInstance, $handler[1]], $params);
        }
    }
}
?>

