<?php
/**
 * Comment Controller - Xử lý các thao tác với bình luận
 */
class CommentController extends Controller {
    
    /**
     * GET /articles/:id/comments
     */
    public function getByArticle($params) {
        $article_id = intval($params['id'] ?? 0);
        
        if ($article_id <= 0) {
            $this->error('ID bài viết không hợp lệ', 400);
        }
        
        // Try to get authenticated user (optional for this endpoint)
        $current_user_id = null;
        try {
            $user = $this->getAuthUser();
            $current_user_id = $user['user_id'];
        } catch (Exception $e) {
            // User not authenticated, that's okay
        }
        
        $query_params = $this->getQueryParams();
        $page = isset($query_params['page']) ? intval($query_params['page']) : 1;
        $limit = isset($query_params['limit']) ? min(intval($query_params['limit']), 100) : 20;
        $offset = ($page - 1) * $limit;
        
        // Don't cache if user is authenticated (to show correct is_liked status)
        $cache_key = $current_user_id ? null : "comments_a{$article_id}_p{$page}_l{$limit}";
        
        $getData = function() use ($article_id, $page, $limit, $offset, $current_user_id) {
            // Get comments
            $sql = "SELECT c.comment_id, c.article_id, c.user_id, c.content, c.parent_comment_id, 
                           c.like_count, c.liked_user_ids, c.is_approved, c.created_at, c.updated_at,
                           u.username, u.full_name, u.avatar_url
                    FROM comments c
                    JOIN users u ON c.user_id = u.user_id
                    WHERE c.article_id = ? AND c.is_active = 1 AND c.is_approved = 1
                    ORDER BY c.created_at DESC
                    LIMIT ? OFFSET ?";
            
            $stmt = $this->db->prepare($sql);
            $stmt->execute([$article_id, $limit, $offset]);
            $comments = [];
            
            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                // Check if current user liked this comment
                $is_liked = false;
                if ($current_user_id && !empty($row['liked_user_ids'])) {
                    $liked_users = explode(',', $row['liked_user_ids']);
                    $liked_users = array_map('intval', $liked_users);
                    $is_liked = in_array((int)$current_user_id, $liked_users, true);
                }
                
                $comments[] = [
                    'comment_id' => (int)$row['comment_id'],
                    'article_id' => (int)$row['article_id'],
                    'user_id' => (int)$row['user_id'],
                    'parent_comment_id' => $row['parent_comment_id'] ? (int)$row['parent_comment_id'] : null,
                    'content' => $row['content'],
                    'is_approved' => (bool)$row['is_approved'],
                    'created_at' => $row['created_at'],
                    'updated_at' => $row['updated_at'],
                    'author_name' => $row['full_name'],
                    'author_avatar' => $row['avatar_url'],
                    'like_count' => (int)$row['like_count'],
                    'is_liked' => $is_liked
                ];
            }
            
            // Get total count
            $count_sql = "SELECT COUNT(*) as total FROM comments 
                          WHERE article_id = ? AND is_active = 1 AND is_approved = 1";
            $count_stmt = $this->db->prepare($count_sql);
            $count_stmt->execute([$article_id]);
            $total_count = (int)$count_stmt->fetch(PDO::FETCH_ASSOC)['total'];
            
            return [
                'success' => true,
                'data' => [
                    'comments' => $comments,
                    'pagination' => [
                        'total' => $total_count,
                        'page' => $page,
                        'limit' => $limit,
                        'totalPages' => (int)ceil($total_count / $limit)
                    ]
                ]
            ];
        };
        
        if ($cache_key) {
            $response = ResponseHelper::cache($cache_key, $getData, 120);
            $this->json($response, 200, 120);
        } else {
            $response = $getData();
            $this->json($response);
        }
    }
    
    /**
     * POST /articles/:id/comments
     */
    public function create($params) {
        $user = $this->getAuthUser();
        $article_id = intval($params['id'] ?? 0);
        $data = $this->getRequestData();
        
        if ($article_id <= 0) {
            $this->error('ID bài viết không hợp lệ', 400);
        }
        
        $content = trim($data['content'] ?? '');
        $parent_comment_id = isset($data['parent_comment_id']) ? intval($data['parent_comment_id']) : null;
        
        if (empty($content)) {
            $this->error('Nội dung bình luận không được để trống', 400);
        }
        
        if (strlen($content) > 1000) {
            $this->error('Nội dung bình luận không được vượt quá 1000 ký tự', 400);
        }
        
        try {
            // Check if article exists
            $stmt = $this->db->prepare("SELECT article_id FROM articles WHERE article_id = ? AND status = 'published'");
            $stmt->execute([$article_id]);
            if (!$stmt->fetch()) {
                $this->error('Bài viết không tồn tại', 404);
            }
            
            // Insert comment
            $sql = "INSERT INTO comments (article_id, user_id, parent_comment_id, content, is_approved) 
                    VALUES (?, ?, ?, ?, 1)";
            $stmt = $this->db->prepare($sql);
            $stmt->execute([$article_id, $user['user_id'], $parent_comment_id, $content]);
            
            $comment_id = $this->db->lastInsertId();
            
            // Get the created comment with user info
            $sql = "SELECT c.comment_id, c.article_id, c.user_id, c.content, c.parent_comment_id, 
                           c.like_count, c.liked_user_ids, c.is_approved, c.created_at, c.updated_at,
                           u.username, u.full_name, u.avatar_url
                    FROM comments c
                    JOIN users u ON c.user_id = u.user_id
                    WHERE c.comment_id = ?";
            $stmt = $this->db->prepare($sql);
            $stmt->execute([$comment_id]);
            $row = $stmt->fetch(PDO::FETCH_ASSOC);
            
            // Check if current user liked this comment (new comment, so always false)
            $is_liked = false;
            
            $comment = [
                'comment_id' => (int)$row['comment_id'],
                'article_id' => (int)$row['article_id'],
                'user_id' => (int)$row['user_id'],
                'parent_comment_id' => $row['parent_comment_id'] ? (int)$row['parent_comment_id'] : null,
                'content' => $row['content'],
                'is_approved' => (bool)$row['is_approved'],
                'created_at' => $row['created_at'],
                'updated_at' => $row['updated_at'],
                'author_name' => $row['full_name'],
                'author_avatar' => $row['avatar_url'],
                'like_count' => (int)$row['like_count'],
                'is_liked' => $is_liked
            ];
            
            // Xóa cache comments và bài viết liên quan
            ResponseHelper::clearCacheByPattern(["comments_a{$article_id}_"]);
            ResponseHelper::clearCacheByKey("article_" . $article_id);
            ResponseHelper::clearCacheByPattern(['articles_']);
            
            $this->json([
                'message' => 'Đã đăng bình luận thành công',
                'comment' => $comment
            ], 201);
            
        } catch (Exception $e) {
            $this->error('Lỗi khi đăng bình luận: ' . $e->getMessage(), 500);
        }
    }
    
    /**
     * PUT /comments/:id
     */
    public function update($params) {
        $user = $this->getAuthUser();
        $comment_id = intval($params['id'] ?? 0);
        $data = $this->getRequestData();
        
        if ($comment_id <= 0) {
            $this->error('ID bình luận không hợp lệ', 400);
        }
        
        $content = trim($data['content'] ?? '');
        
        if (empty($content)) {
            $this->error('Nội dung bình luận không được để trống', 400);
        }
        
        try {
            // Check ownership
            $stmt = $this->db->prepare("SELECT user_id, article_id FROM comments WHERE comment_id = ?");
            $stmt->execute([$comment_id]);
            $comment = $stmt->fetch(PDO::FETCH_ASSOC);
            
            if (!$comment) {
                $this->error('Bình luận không tồn tại', 404);
            }
            
            if ($comment['user_id'] != $user['user_id']) {
                $this->error('Bạn không có quyền sửa bình luận này', 403);
            }
            
            // Update comment
            $stmt = $this->db->prepare("UPDATE comments SET content = ? WHERE comment_id = ?");
            $stmt->execute([$content, $comment_id]);
            
            // Clear cache
            $article_id = $comment['article_id'];
            $cache_pattern = sys_get_temp_dir() . '/api_cache_*comments_a' . $article_id . '*';
            foreach (glob($cache_pattern) as $file) {
                @unlink($file);
            }
            
            $this->success(['message' => 'Đã cập nhật bình luận']);
            
        } catch (Exception $e) {
            $this->error('Lỗi khi cập nhật bình luận: ' . $e->getMessage(), 500);
        }
    }
    
    /**
     * DELETE /comments/:id
     */
    public function delete($params) {
        $user = $this->getAuthUser();
        $comment_id = intval($params['id'] ?? 0);
        
        if ($comment_id <= 0) {
            $this->error('ID bình luận không hợp lệ', 400);
        }
        
        try {
            // Check ownership
            $stmt = $this->db->prepare("SELECT user_id, article_id FROM comments WHERE comment_id = ?");
            $stmt->execute([$comment_id]);
            $comment = $stmt->fetch(PDO::FETCH_ASSOC);
            
            if (!$comment) {
                $this->error('Bình luận không tồn tại', 404);
            }
            
            if ($comment['user_id'] != $user['user_id'] && $user['role'] !== 'admin') {
                $this->error('Bạn không có quyền xóa bình luận này', 403);
            }
            
            // Soft delete (set is_active = 0)
            $stmt = $this->db->prepare("UPDATE comments SET is_active = 0 WHERE comment_id = ?");
            $stmt->execute([$comment_id]);
            
            // Clear cache
            $article_id = $comment['article_id'];
            $cache_pattern = sys_get_temp_dir() . '/api_cache_*comments_a' . $article_id . '*';
            foreach (glob($cache_pattern) as $file) {
                @unlink($file);
            }
            
            $this->success(['message' => 'Đã xóa bình luận']);
            
        } catch (Exception $e) {
            $this->error('Lỗi khi xóa bình luận: ' . $e->getMessage(), 500);
        }
    }
    
    /**
     * POST /comments/:id/like
     */
    public function toggleLike($params) {
        $user = $this->getAuthUser();
        $comment_id = intval($params['id'] ?? 0);
        
        if ($comment_id <= 0) {
            $this->error('ID bình luận không hợp lệ', 400);
        }
        
        try {
            // Check if comment exists and get current liked users
            $stmt = $this->db->prepare("SELECT comment_id, article_id, liked_user_ids, like_count FROM comments WHERE comment_id = ?");
            $stmt->execute([$comment_id]);
            $comment = $stmt->fetch(PDO::FETCH_ASSOC);
            
            if (!$comment) {
                $this->error('Bình luận không tồn tại', 404);
            }
            
            // Get current liked users
            $liked_users = [];
            if (!empty($comment['liked_user_ids'])) {
                $liked_users = explode(',', $comment['liked_user_ids']);
                $liked_users = array_map('intval', $liked_users); // Convert to int
            }
            
            $is_liked = in_array((int)$user['user_id'], $liked_users, true);
            
            if ($is_liked) {
                // Unlike: Remove user from liked list
                $liked_users = array_diff($liked_users, [(int)$user['user_id']]);
                $liked_users_str = implode(',', array_filter($liked_users));
                
                $sql = "UPDATE comments 
                        SET like_count = GREATEST(like_count - 1, 0), 
                            liked_user_ids = ? 
                        WHERE comment_id = ?";
                $stmt = $this->db->prepare($sql);
                $stmt->execute([$liked_users_str, $comment_id]);
                
                $message = 'Đã bỏ thích bình luận';
                $liked = false;
            } else {
                // Like: Add user to liked list
                $liked_users[] = (int)$user['user_id'];
                $liked_users_str = implode(',', $liked_users);
                
                $sql = "UPDATE comments 
                        SET like_count = like_count + 1, 
                            liked_user_ids = ? 
                        WHERE comment_id = ?";
                $stmt = $this->db->prepare($sql);
                $stmt->execute([$liked_users_str, $comment_id]);
                
                $message = 'Đã thích bình luận';
                $liked = true;
            }
            
            // Get updated like_count from database
            $stmt = $this->db->prepare("SELECT like_count FROM comments WHERE comment_id = ?");
            $stmt->execute([$comment_id]);
            $result = $stmt->fetch(PDO::FETCH_ASSOC);
            $like_count = $result ? (int)$result['like_count'] : 0;
            
            // Clear cache comments
            $article_id = $comment['article_id'];
            ResponseHelper::clearCacheByPattern(["comments_a{$article_id}_"]);
            ResponseHelper::clearCacheByKey("article_" . $article_id);
            
            $this->success([
                'comment_id' => $comment_id,
                'like_count' => $like_count,
                'is_liked' => $liked,
                'message' => $message
            ]);
            
        } catch (Exception $e) {
            $this->error('Lỗi khi like bình luận: ' . $e->getMessage(), 500);
        }
    }
}
?>

