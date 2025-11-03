<?php
/**
 * Like Controller - Xử lý các thao tác like bài viết
 */
class LikeController extends Controller {
    
    /**
     * POST /articles/:id/like
     */
    public function toggleArticleLike($params) {
        $user = $this->getAuthUser();
        $article_id = intval($params['id'] ?? 0);
        
        if ($article_id <= 0) {
            $this->error('ID bài viết không hợp lệ', 400);
        }
        
        try {
            // Check if article exists
            $stmt = $this->db->prepare("SELECT article_id FROM articles WHERE article_id = ? AND status = 'published'");
            $stmt->execute([$article_id]);
            if (!$stmt->fetch()) {
                $this->error('Bài viết không tồn tại', 404);
            }
            
            // Get current liked users
            $stmt = $this->db->prepare("SELECT liked_user_ids FROM article_views WHERE article_id = ?");
            $stmt->execute([$article_id]);
            $result = $stmt->fetch(PDO::FETCH_ASSOC);
            
            $liked_users = [];
            if ($result && !empty($result['liked_user_ids'])) {
                $liked_users = explode(',', $result['liked_user_ids']);
                $liked_users = array_map('intval', $liked_users); // Convert to int
            }
            
            $is_liked = in_array((int)$user['user_id'], $liked_users, true);
            
            if ($is_liked) {
                // Unlike: Remove user from liked list
                $liked_users = array_diff($liked_users, [(int)$user['user_id']]);
                $liked_users_str = implode(',', array_filter($liked_users));
                
                $sql = "UPDATE article_views 
                        SET like_count = GREATEST(like_count - 1, 0), 
                            liked_user_ids = ? 
                        WHERE article_id = ?";
                $stmt = $this->db->prepare($sql);
                $stmt->execute([$liked_users_str, $article_id]);
                
                $message = 'Đã bỏ thích bài viết';
                $liked = false;
            } else {
                // Like: Add user to liked list
                $liked_users[] = (int)$user['user_id'];
                $liked_users_str = implode(',', $liked_users);
                
                $sql = "INSERT INTO article_views (article_id, like_count, liked_user_ids) 
                        VALUES (?, 1, ?) 
                        ON DUPLICATE KEY UPDATE 
                            like_count = like_count + 1, 
                            liked_user_ids = ?";
                $stmt = $this->db->prepare($sql);
                $stmt->execute([$article_id, $liked_users_str, $liked_users_str]);
                
                $message = 'Đã thích bài viết';
                $liked = true;
            }
            
            // Get updated like_count from database
            $stmt = $this->db->prepare("SELECT like_count FROM article_views WHERE article_id = ?");
            $stmt->execute([$article_id]);
            $result = $stmt->fetch(PDO::FETCH_ASSOC);
            $like_count = $result ? (int)$result['like_count'] : 0;
            
            // Xóa cache bài viết và các danh sách liên quan
            ResponseHelper::clearCacheByKey("article_" . $article_id);
            ResponseHelper::clearCacheByPattern(['articles_', 'favorites_', 'trending', 'featured']);
            
            $this->success([
                'message' => $message,
                'liked' => $liked,
                'like_count' => $like_count
            ]);
            
        } catch (Exception $e) {
            $this->error('Lỗi khi thực hiện thao tác: ' . $e->getMessage(), 500);
        }
    }
    
    /**
     * GET /articles/:id/like-status
     */
    public function getArticleLikeStatus($params) {
        $user = $this->getAuthUser();
        $article_id = intval($params['id'] ?? 0);
        
        if ($article_id <= 0) {
            $this->error('ID bài viết không hợp lệ', 400);
        }
        
        try {
            $stmt = $this->db->prepare("SELECT liked_user_ids, like_count FROM article_views WHERE article_id = ?");
            $stmt->execute([$article_id]);
            $result = $stmt->fetch(PDO::FETCH_ASSOC);
            
            $liked = false;
            $like_count = 0;
            
            if ($result) {
                $like_count = (int)$result['like_count'];
                if (!empty($result['liked_user_ids'])) {
                    $liked_users = explode(',', $result['liked_user_ids']);
                    $liked_users = array_map('intval', $liked_users); // Convert to int
                    $liked = in_array((int)$user['user_id'], $liked_users, true);
                }
            }
            
            $this->success([
                'liked' => $liked,
                'like_count' => $like_count
            ]);
            
        } catch (Exception $e) {
            $this->error('Lỗi khi lấy trạng thái like: ' . $e->getMessage(), 500);
        }
    }
}
?>

