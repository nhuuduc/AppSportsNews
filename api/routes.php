<?php
/**
 * API Routes Definition
 */

// ============================================
// Public Routes (không cần xác thực)
// ============================================

// Auth routes
$router->post('/auth/register', 'AuthController@register');
$router->post('/auth/login', 'AuthController@login');
$router->get('/auth/verify', 'AuthController@verify');
$router->post('/auth/verify', 'AuthController@verify');
$router->post('/auth/resend-verification', 'AuthController@resendVerification');
$router->get('/auth/verify-session', 'AuthController@verifySession');

// Articles routes
$router->get('/articles', 'ArticleController@index');
$router->get('/articles/featured', 'ArticleController@featured');
$router->get('/articles/breaking', 'ArticleController@breaking');
$router->get('/articles/trending', 'ArticleController@trending');
$router->get('/articles/:id', 'ArticleController@show');
$router->post('/articles/:id/view', 'ArticleController@incrementView');

// Categories routes
$router->get('/categories', 'CategoryController@index');
$router->get('/categories/:id', 'CategoryController@show');

// Search route
$router->get('/search', 'SearchController@search');

// Teams routes
$router->get('/teams', 'TeamController@index');
$router->get('/teams/:id', 'TeamController@show');

// Matches routes
$router->get('/matches', 'MatchController@index');
$router->get('/matches/live', 'MatchController@live');
$router->get('/matches/upcoming', 'MatchController@upcoming');
$router->get('/matches/:id', 'MatchController@show');

// Videos routes
$router->get('/videos', 'VideoController@index');
$router->get('/videos/highlights', 'VideoController@highlights');
$router->get('/videos/:id', 'VideoController@show');

// Tags routes
$router->get('/tags', 'TagController@index');
$router->get('/tags/:id', 'TagController@show');

// Comments routes (public read)
$router->get('/articles/:id/comments', 'CommentController@getByArticle');

// ============================================
// Protected Routes (cần xác thực)
// ============================================

// Auth protected
$router->post('/auth/logout', 'AuthController@logout', ['AuthMiddleware::authenticate']);
$router->get('/auth/me', 'AuthController@me', ['AuthMiddleware::authenticate']);

// Profile routes (thứ tự quan trọng: routes cụ thể phải đứng trước routes chung)
$router->get('/profile/favorites', 'ProfileController@favorites', ['AuthMiddleware::authenticate']);
$router->get('/profile/avatar', 'ProfileController@getAvatar', ['AuthMiddleware::authenticate']);
$router->post('/profile/avatar', 'ProfileController@uploadAvatar', ['AuthMiddleware::authenticate']);
$router->delete('/profile/avatar', 'ProfileController@deleteAvatar', ['AuthMiddleware::authenticate']);
$router->put('/profile/password', 'ProfileController@updatePassword', ['AuthMiddleware::authenticate']);
$router->get('/profile', 'ProfileController@show', ['AuthMiddleware::authenticate']);
$router->put('/profile', 'ProfileController@update', ['AuthMiddleware::authenticate']);

// Likes routes
$router->post('/articles/:id/like', 'LikeController@toggleArticleLike', ['AuthMiddleware::authenticate']);
$router->get('/articles/:id/like-status', 'LikeController@getArticleLikeStatus', ['AuthMiddleware::authenticate']);

// Comments routes (write operations need auth)
$router->post('/articles/:id/comments', 'CommentController@create', ['AuthMiddleware::authenticate']);
$router->put('/comments/:id', 'CommentController@update', ['AuthMiddleware::authenticate']);
$router->delete('/comments/:id', 'CommentController@delete', ['AuthMiddleware::authenticate']);
$router->post('/comments/:id/like', 'CommentController@toggleLike', ['AuthMiddleware::authenticate']);

// Favorites routes
$router->post('/favorites', 'FavoriteController@add', ['AuthMiddleware::authenticate']);
$router->delete('/favorites/:id', 'FavoriteController@remove', ['AuthMiddleware::authenticate']);

// Posts routes (user-generated articles)
$router->post('/posts/create', 'PostController@create', ['AuthMiddleware::authenticate']);
$router->post('/posts/update', 'PostController@update', ['AuthMiddleware::authenticate']);
$router->delete('/posts/delete', 'PostController@delete', ['AuthMiddleware::authenticate']);
$router->get('/posts/my-posts', 'PostController@myPosts', ['AuthMiddleware::authenticate']);

// Image upload routes
$router->post('/posts/upload-image', 'ImageController@uploadImage', ['AuthMiddleware::authenticate']);
$router->delete('/posts/delete-image', 'ImageController@deleteImage', ['AuthMiddleware::authenticate']);

// ============================================
// API Info
// ============================================
$router->get('/', function() {
    ResponseHelper::sendJson([
        'name' => 'Sports News API',
        'version' => '2.0',
        'status' => 'active',
        'message' => 'API đang hoạt động bình thường',
        'endpoints' => [
            'auth' => '/auth/*',
            'articles' => '/articles',
            'categories' => '/categories',
            'search' => '/search',
            'teams' => '/teams',
            'matches' => '/matches',
            'videos' => '/videos',
            'profile' => '/profile'
        ],
    ]);
});

// Health check
$router->get('/health', function() {
    $database = Database::getInstance();
    $db = $database->getConnection();
    
    $db_status = 'connected';
    try {
        $db->query('SELECT 1');
    } catch (Exception $e) {
        $db_status = 'error';
    }
    
    ResponseHelper::sendJson([
        'status' => 'healthy',
        'database' => $db_status,
        'timestamp' => date('Y-m-d H:i:s'),
        'timezone' => date_default_timezone_get()
    ]);
});
?>

