<?php
/**
 * Post Controller - Xử lý user-generated posts/articles
 */
class PostController extends Controller {
    
    /**
     * POST /posts/create
     * Create a new user post (article)
     */
    public function create() {
        $user = $this->getAuthUser();
        $data = $this->getRequestData();
        
        $title = trim($data['title'] ?? '');
        $summary = trim($data['summary'] ?? '');
        $content = trim($data['content'] ?? '');
        $category_id = isset($data['category_id']) ? intval($data['category_id']) : null;
        $thumbnail_url = trim($data['thumbnail_url'] ?? '');
        
        // Validation
        if (empty($title)) {
            $this->error('Tiêu đề không được để trống', 400);
        }
        
        if (empty($content)) {
            $this->error('Nội dung không được để trống', 400);
        }
        
        if (strlen($title) > 255) {
            $this->error('Tiêu đề không được vượt quá 255 ký tự', 400);
        }
        
        try {
            // Generate slug from title
            $slug = $this->generateSlug($title);
            
            // Check if user's email is verified
            $stmt = $this->db->prepare("SELECT email_verified_at FROM users WHERE user_id = ?");
            $stmt->execute([$user['user_id']]);
            $userInfo = $stmt->fetch();
            
            if (!$userInfo || !$userInfo['email_verified_at']) {
                $this->error('Bạn cần xác thực email trước khi đăng bài', 403);
            }
            
            // Insert article
            $stmt = $this->db->prepare(
                "INSERT INTO articles (title, slug, summary, content, thumbnail_url, 
                 category_id, author_id, status, published_at) 
                 VALUES (?, ?, ?, ?, ?, ?, ?, 'published', NOW())"
            );
            
            $stmt->execute([
                $title,
                $slug,
                $summary,
                $content,
                $thumbnail_url,
                $category_id,
                $user['user_id']
            ]);
            
            $article_id = $this->db->lastInsertId();
            
            $this->success([
                'article_id' => (int)$article_id,
                'message' => 'Đăng bài viết thành công'
            ], null, 201);
            
        } catch (Exception $e) {
            $this->error('Lỗi khi tạo bài viết: ' . $e->getMessage(), 500);
        }
    }
    
    /**
     * POST /posts/update
     * Update user's own post
     */
    public function update() {
        $user = $this->getAuthUser();
        $data = $this->getRequestData();
        
        $article_id = isset($data['article_id']) ? intval($data['article_id']) : 0;
        
        if ($article_id <= 0) {
            $this->error('ID bài viết không hợp lệ', 400);
        }
        
        try {
            // Check ownership
            $stmt = $this->db->prepare(
                "SELECT author_id FROM articles WHERE article_id = ?"
            );
            $stmt->execute([$article_id]);
            $article = $stmt->fetch();
            
            if (!$article) {
                $this->error('Bài viết không tồn tại', 404);
            }
            
            if ($article['author_id'] != $user['user_id']) {
                $this->error('Bạn không có quyền chỉnh sửa bài viết này', 403);
            }
            
            // Build update query
            $update_fields = [];
            $update_values = [];
            
            if (isset($data['title'])) {
                $title = trim($data['title']);
                if (empty($title)) {
                    $this->error('Tiêu đề không được để trống', 400);
                }
                $update_fields[] = "title = ?";
                $update_values[] = $title;
                
                // Update slug too
                $update_fields[] = "slug = ?";
                $update_values[] = $this->generateSlug($title);
            }
            
            if (isset($data['summary'])) {
                $update_fields[] = "summary = ?";
                $update_values[] = trim($data['summary']);
            }
            
            if (isset($data['content'])) {
                $content = trim($data['content']);
                if (empty($content)) {
                    $this->error('Nội dung không được để trống', 400);
                }
                $update_fields[] = "content = ?";
                $update_values[] = $content;
            }
            
            if (isset($data['thumbnail_url'])) {
                $update_fields[] = "thumbnail_url = ?";
                $update_values[] = trim($data['thumbnail_url']);
            }
            
            if (isset($data['category_id'])) {
                $update_fields[] = "category_id = ?";
                $update_values[] = intval($data['category_id']);
            }
            
            if (empty($update_fields)) {
                $this->error('Không có thông tin nào để cập nhật', 400);
            }
            
            $update_fields[] = "updated_at = NOW()";
            $update_values[] = $article_id;
            
            $sql = "UPDATE articles SET " . implode(', ', $update_fields) . " WHERE article_id = ?";
            $stmt = $this->db->prepare($sql);
            $stmt->execute($update_values);
            
            $this->success([
                'article_id' => $article_id,
                'message' => 'Cập nhật bài viết thành công'
            ]);
            
        } catch (Exception $e) {
            $this->error('Lỗi khi cập nhật bài viết: ' . $e->getMessage(), 500);
        }
    }
    
    /**
     * DELETE /posts/delete
     * Delete user's own post
     */
    public function delete() {
        $user = $this->getAuthUser();
        $query = $this->getQueryParams();
        
        $article_id = isset($query['article_id']) ? intval($query['article_id']) : 0;
        
        if ($article_id <= 0) {
            $this->error('ID bài viết không hợp lệ', 400);
        }
        
        try {
            // Check ownership
            $stmt = $this->db->prepare(
                "SELECT author_id FROM articles WHERE article_id = ?"
            );
            $stmt->execute([$article_id]);
            $article = $stmt->fetch();
            
            if (!$article) {
                $this->error('Bài viết không tồn tại', 404);
            }
            
            if ($article['author_id'] != $user['user_id'] && $user['role'] !== 'admin') {
                $this->error('Bạn không có quyền xóa bài viết này', 403);
            }
            
            // Soft delete - change status to 'draft' or 'deleted'
            $stmt = $this->db->prepare(
                "UPDATE articles SET status = 'draft', updated_at = NOW() WHERE article_id = ?"
            );
            $stmt->execute([$article_id]);
            
            // Clear cache
            $cache_file = sys_get_temp_dir() . '/api_cache_' . md5("article_" . $article_id);
            @unlink($cache_file);
            
            $this->success([
                'message' => 'Đã xóa bài viết thành công'
            ]);
            
        } catch (Exception $e) {
            $this->error('Lỗi khi xóa bài viết: ' . $e->getMessage(), 500);
        }
    }
    
    /**
     * GET /posts/my-posts
     * Get current user's posts
     */
    public function myPosts() {
        $user = $this->getAuthUser();
        $params = $this->getQueryParams();
        
        $page = isset($params['page']) ? intval($params['page']) : 1;
        $limit = isset($params['limit']) ? min(intval($params['limit']), 50) : 20;
        $offset = ($page - 1) * $limit;
        
        try {
            // Get user's articles
            $sql = "SELECT 
                        a.article_id, a.title, a.slug, a.summary, a.content,
                        a.thumbnail_url, a.category_id, a.author_id,
                        COALESCE(v.view_count, 0) as view_count,
                        COALESCE(v.like_count, 0) as like_count,
                        COALESCE(cm.comment_count, 0) as comment_count,
                        a.is_featured, a.is_breaking_news,
                        a.status, a.published_at, a.created_at, a.updated_at,
                        c.category_name
                    FROM articles a
                    LEFT JOIN categories c ON a.category_id = c.category_id
                    LEFT JOIN article_views v ON a.article_id = v.article_id
                    LEFT JOIN (
                        SELECT article_id, COUNT(*) as comment_count 
                        FROM comments 
                        WHERE is_active = 1 
                        GROUP BY article_id
                    ) cm ON a.article_id = cm.article_id
                    WHERE a.author_id = ?
                    ORDER BY a.created_at DESC
                    LIMIT ? OFFSET ?";
            
            $stmt = $this->db->prepare($sql);
            $stmt->execute([$user['user_id'], $limit, $offset]);
            $articles = $stmt->fetchAll(PDO::FETCH_ASSOC);
            
            // Format articles
            foreach ($articles as &$article) {
                $article['article_id'] = (int)$article['article_id'];
                $article['category_id'] = (int)$article['category_id'];
                $article['author_id'] = (int)$article['author_id'];
                $article['view_count'] = (int)$article['view_count'];
                $article['like_count'] = (int)$article['like_count'];
                $article['comment_count'] = (int)$article['comment_count'];
                $article['is_featured'] = (bool)$article['is_featured'];
                $article['is_breaking_news'] = (bool)$article['is_breaking_news'];
            }
            
            // Get total count
            $count_sql = "SELECT COUNT(*) as total FROM articles WHERE author_id = ?";
            $count_stmt = $this->db->prepare($count_sql);
            $count_stmt->execute([$user['user_id']]);
            $total = $count_stmt->fetch(PDO::FETCH_ASSOC)['total'];
            
            $this->success([
                'articles' => $articles,
                'total' => (int)$total,
                'page' => $page,
                'limit' => $limit
            ]);
            
        } catch (Exception $e) {
            $this->error('Lỗi khi lấy danh sách bài viết: ' . $e->getMessage(), 500);
        }
    }
    
    /**
     * Generate URL-friendly slug from title
     */
    private function generateSlug($title) {
        // Convert to lowercase
        $slug = strtolower($title);
        
        // Vietnamese characters mapping
        $vietnamese_map = [
            'á' => 'a', 'à' => 'a', 'ả' => 'a', 'ã' => 'a', 'ạ' => 'a',
            'ă' => 'a', 'ắ' => 'a', 'ằ' => 'a', 'ẳ' => 'a', 'ẵ' => 'a', 'ặ' => 'a',
            'â' => 'a', 'ấ' => 'a', 'ầ' => 'a', 'ẩ' => 'a', 'ẫ' => 'a', 'ậ' => 'a',
            'é' => 'e', 'è' => 'e', 'ẻ' => 'e', 'ẽ' => 'e', 'ẹ' => 'e',
            'ê' => 'e', 'ế' => 'e', 'ề' => 'e', 'ể' => 'e', 'ễ' => 'e', 'ệ' => 'e',
            'í' => 'i', 'ì' => 'i', 'ỉ' => 'i', 'ĩ' => 'i', 'ị' => 'i',
            'ó' => 'o', 'ò' => 'o', 'ỏ' => 'o', 'õ' => 'o', 'ọ' => 'o',
            'ô' => 'o', 'ố' => 'o', 'ồ' => 'o', 'ổ' => 'o', 'ỗ' => 'o', 'ộ' => 'o',
            'ơ' => 'o', 'ớ' => 'o', 'ờ' => 'o', 'ở' => 'o', 'ỡ' => 'o', 'ợ' => 'o',
            'ú' => 'u', 'ù' => 'u', 'ủ' => 'u', 'ũ' => 'u', 'ụ' => 'u',
            'ư' => 'u', 'ứ' => 'u', 'ừ' => 'u', 'ử' => 'u', 'ữ' => 'u', 'ự' => 'u',
            'ý' => 'y', 'ỳ' => 'y', 'ỷ' => 'y', 'ỹ' => 'y', 'ỵ' => 'y',
            'đ' => 'd'
        ];
        
        $slug = strtr($slug, $vietnamese_map);
        
        // Remove special characters
        $slug = preg_replace('/[^a-z0-9\s-]/', '', $slug);
        
        // Replace spaces and multiple hyphens with single hyphen
        $slug = preg_replace('/[\s-]+/', '-', $slug);
        
        // Remove leading/trailing hyphens
        $slug = trim($slug, '-');
        
        // Add timestamp to ensure uniqueness
        $slug .= '-' . time();
        
        return $slug;
    }
}
?>

