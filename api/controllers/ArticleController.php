<?php
/**
 * Article Controller - Xử lý các thao tác với bài viết
 */
class ArticleController extends Controller {
    private $articleModel;
    
    public function __construct() {
        parent::__construct();
        $this->articleModel = new Article($this->db);
    }
    
    /**
     * GET /articles
     */
    public function index() {
        $params = $this->getQueryParams();
        
        $page = isset($params['page']) ? intval($params['page']) : 1;
        $limit = isset($params['limit']) ? min(intval($params['limit']), 100) : 20;
        $category_id = isset($params['category_id']) ? intval($params['category_id']) : null;
        
        $cache_key = "articles_p{$page}_l{$limit}_c" . ($category_id ?? 'all');
        
        $response = ResponseHelper::cache($cache_key, function() use ($page, $limit, $category_id) {
            $stmt = $this->articleModel->getAll($page, $limit, $category_id);
            $total_count = $this->articleModel->getTotalCount($category_id);
            $total_pages = ceil($total_count / $limit);
            
            $articles_arr = [];
            
            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                $articles_arr[] = [
                    "article_id" => (int)$row['article_id'],
                    "title" => $row['title'],
                    "slug" => $row['slug'],
                    "summary" => $row['summary'],
                    "thumbnail_url" => $row['thumbnail_url'],
                    "category_id" => (int)$row['category_id'],
                    "category_name" => $row['category_name'],
                    "author_id" => (int)$row['author_id'],
                    "author_name" => $row['author_name'],
                    "view_count" => (int)$row['view_count'],
                    "like_count" => (int)$row['like_count'],
                    "comment_count" => (int)$row['comment_count'],
                    "is_featured" => (bool)$row['is_featured'],
                    "is_breaking_news" => (bool)$row['is_breaking_news'],
                    "published_at" => $row['published_at'],
                    "created_at" => $row['created_at'],
                    "updated_at" => $row['updated_at']
                ];
            }
            
            return [
                "articles" => $articles_arr,
                "totalCount" => (int)$total_count,
                "currentPage" => (int)$page,
                "totalPages" => (int)$total_pages
            ];
        }, 120);
        
        // Add is_liked field if user is authenticated
        $user = $this->getOptionalAuthUser();
        if ($user && isset($response['articles'])) {
            foreach ($response['articles'] as &$article) {
                $article['is_liked'] = $this->checkArticleLiked($article['article_id'], $user['user_id']);
            }
        } else if (isset($response['articles'])) {
            foreach ($response['articles'] as &$article) {
                $article['is_liked'] = false;
            }
        }
        
        $this->json($response, 200, 0); // No cache for personalized data
    }
    
    /**
     * GET /articles/:id
     */
    public function show($params) {
        $article_id = intval($params['id'] ?? 0);
        
        if ($article_id <= 0) {
            $this->error('ID bài viết không hợp lệ', 400);
        }
        
        $cache_key = "article_" . $article_id;
        
        $article_data = ResponseHelper::cache($cache_key, function() use ($article_id) {
            $this->articleModel->article_id = $article_id;
            $data = $this->articleModel->getByIdComplete();
            
            if ($data) {
                // Convert types
                $data['article_id'] = (int)$data['article_id'];
                $data['category_id'] = (int)$data['category_id'];
                $data['author_id'] = (int)$data['author_id'];
                $data['view_count'] = (int)$data['view_count'];
                $data['like_count'] = (int)$data['like_count'];
                $data['comment_count'] = (int)$data['comment_count'];
                $data['is_featured'] = (bool)$data['is_featured'];
                $data['is_breaking_news'] = (bool)$data['is_breaking_news'];
            }
            return $data;
        }, 300);
        
        if ($article_data) {
            // Add is_liked field if user is authenticated
            $user = $this->getOptionalAuthUser();
            if ($user) {
                $article_data['is_liked'] = $this->checkArticleLiked($article_id, $user['user_id']);
            } else {
                $article_data['is_liked'] = false;
            }
            
            $this->json($article_data, 200, 0); // No cache for personalized data
        } else {
            $this->error("Không tìm thấy bài viết", 404);
        }
    }
    
    /**
     * GET /articles/featured
     */
    public function featured() {
        $params = $this->getQueryParams();
        $limit = isset($params['limit']) ? min(intval($params['limit']), 20) : 5;
        
        $cache_key = "articles_featured_" . $limit;
        
        $response = ResponseHelper::cache($cache_key, function() use ($limit) {
            $stmt = $this->articleModel->getFeatured($limit);
            $articles_arr = [];
            
            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                $articles_arr[] = [
                    "article_id" => (int)$row['article_id'],
                    "title" => $row['title'],
                    "slug" => $row['slug'],
                    "summary" => $row['summary'],
                    "thumbnail_url" => $row['thumbnail_url'],
                    "category_id" => (int)$row['category_id'],
                    "category_name" => $row['category_name'],
                    "author_id" => (int)$row['author_id'],
                    "author_name" => $row['author_name'],
                    "view_count" => (int)$row['view_count'],
                    "like_count" => (int)$row['like_count'],
                    "comment_count" => (int)$row['comment_count'],
                    "is_featured" => true,
                    "is_breaking_news" => (bool)$row['is_breaking_news'],
                    "published_at" => $row['published_at']
                ];
            }
            
            return ["articles" => $articles_arr];
        }, 180);
        
        // Add is_liked field if user is authenticated
        $user = $this->getOptionalAuthUser();
        if ($user && isset($response['articles'])) {
            foreach ($response['articles'] as &$article) {
                $article['is_liked'] = $this->checkArticleLiked($article['article_id'], $user['user_id']);
            }
        } else if (isset($response['articles'])) {
            foreach ($response['articles'] as &$article) {
                $article['is_liked'] = false;
            }
        }
        
        $this->json($response, 200, 0); // No cache for personalized data
    }
    
    /**
     * GET /articles/breaking
     */
    public function breaking() {
        $params = $this->getQueryParams();
        $limit = isset($params['limit']) ? min(intval($params['limit']), 10) : 3;
        
        $cache_key = "articles_breaking_" . $limit;
        
        $response = ResponseHelper::cache($cache_key, function() use ($limit) {
            $stmt = $this->articleModel->getBreakingNews($limit);
            $articles_arr = [];
            
            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                $articles_arr[] = [
                    "article_id" => (int)$row['article_id'],
                    "title" => $row['title'],
                    "slug" => $row['slug'],
                    "summary" => $row['summary'],
                    "thumbnail_url" => $row['thumbnail_url'],
                    "category_id" => (int)$row['category_id'],
                    "category_name" => $row['category_name'],
                    "author_id" => (int)$row['author_id'],
                    "author_name" => $row['author_name'],
                    "view_count" => (int)$row['view_count'],
                    "like_count" => (int)$row['like_count'],
                    "comment_count" => (int)$row['comment_count'],
                    "is_featured" => (bool)$row['is_featured'],
                    "is_breaking_news" => true,
                    "published_at" => $row['published_at']
                ];
            }
            
            return ["articles" => $articles_arr];
        }, 60);
        
        // Add is_liked field if user is authenticated
        $user = $this->getOptionalAuthUser();
        if ($user && isset($response['articles'])) {
            foreach ($response['articles'] as &$article) {
                $article['is_liked'] = $this->checkArticleLiked($article['article_id'], $user['user_id']);
            }
        } else if (isset($response['articles'])) {
            foreach ($response['articles'] as &$article) {
                $article['is_liked'] = false;
            }
        }
        
        $this->json($response, 200, 0); // No cache for personalized data
    }
    
    /**
     * GET /articles/trending
     */
    public function trending() {
        $params = $this->getQueryParams();
        $limit = isset($params['limit']) ? min(intval($params['limit']), 20) : 10;
        
        $cache_key = "articles_trending_" . $limit;
        
        $response = ResponseHelper::cache($cache_key, function() use ($limit) {
            $stmt = $this->articleModel->getTrending($limit);
            $articles_arr = [];
            
            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                $articles_arr[] = [
                    "article_id" => (int)$row['article_id'],
                    "title" => $row['title'],
                    "slug" => $row['slug'],
                    "summary" => $row['summary'],
                    "thumbnail_url" => $row['thumbnail_url'],
                    "category_id" => (int)$row['category_id'],
                    "category_name" => $row['category_name'],
                    "author_id" => (int)$row['author_id'],
                    "author_name" => $row['author_name'],
                    "view_count" => (int)$row['view_count'],
                    "like_count" => (int)$row['like_count'],
                    "comment_count" => (int)$row['comment_count'],
                    "is_featured" => (bool)$row['is_featured'],
                    "is_breaking_news" => (bool)$row['is_breaking_news'],
                    "published_at" => $row['published_at'],
                    "trending_score" => isset($row['trending_score']) ? floatval($row['trending_score']) : 0
                ];
            }
            
            return ["articles" => $articles_arr];
        }, 300);
        
        // Add is_liked field if user is authenticated
        $user = $this->getOptionalAuthUser();
        if ($user && isset($response['articles'])) {
            foreach ($response['articles'] as &$article) {
                $article['is_liked'] = $this->checkArticleLiked($article['article_id'], $user['user_id']);
            }
        } else if (isset($response['articles'])) {
            foreach ($response['articles'] as &$article) {
                $article['is_liked'] = false;
            }
        }
        
        $this->json($response, 200, 0); // No cache for personalized data
    }
    
    /**
     * POST /articles/:id/view
     */
    public function incrementView($params) {
        $article_id = intval($params['id'] ?? 0);
        
        if ($article_id <= 0) {
            $this->error('ID bài viết không hợp lệ', 400);
        }
        
        try {
            // Increment view count
            $stmt = $this->db->prepare(
                "INSERT INTO article_views (article_id, view_count) 
                 VALUES (?, 1) 
                 ON DUPLICATE KEY UPDATE view_count = view_count + 1"
            );
            $stmt->execute([$article_id]);
            
            // Xóa cache bài viết và danh sách liên quan
            ResponseHelper::clearCacheByKey("article_" . $article_id);
            ResponseHelper::clearCacheByPattern(['articles_', 'trending']);
            
            $this->success(['message' => 'Đã tăng lượt xem']);
        } catch (Exception $e) {
            $this->error('Lỗi khi tăng lượt xem', 500);
        }
    }
}
?>

