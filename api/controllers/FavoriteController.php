<?php
/**
 * Favorite Controller - Xử lý các thao tác yêu thích
 */
class FavoriteController extends Controller {
    
    /**
     * POST /favorites
     */
    public function add() {
        $user = $this->getAuthUser();
        $data = $this->getRequestData();
        
        $type = $data['type'] ?? ''; // article or team
        $article_id = isset($data['article_id']) ? intval($data['article_id']) : null;
        $team_id = isset($data['team_id']) ? intval($data['team_id']) : null;
        
        if (!in_array($type, ['article', 'team'])) {
            $this->error('Loại yêu thích không hợp lệ', 400);
        }
        
        if ($type === 'article' && !$article_id) {
            $this->error('ID bài viết không hợp lệ', 400);
        }
        
        if ($type === 'team' && !$team_id) {
            $this->error('ID đội bóng không hợp lệ', 400);
        }
        
        try {
            // Check if already favorited
            $check_sql = "SELECT favorite_id FROM favorites 
                          WHERE user_id = ? AND type = ? AND 
                          " . ($type === 'article' ? "article_id = ?" : "team_id = ?");
            $check_stmt = $this->db->prepare($check_sql);
            $check_stmt->execute([$user['user_id'], $type, $type === 'article' ? $article_id : $team_id]);
            
            if ($check_stmt->fetch()) {
                $this->error('Đã thêm vào yêu thích trước đó', 400);
            }
            
            // Add to favorites
            $sql = "INSERT INTO favorites (user_id, article_id, team_id, type) VALUES (?, ?, ?, ?)";
            $stmt = $this->db->prepare($sql);
            $stmt->execute([$user['user_id'], $article_id, $team_id, $type]);
            
            $favorite_id = $this->db->lastInsertId();
            
            $this->success([
                'favorite_id' => (int)$favorite_id,
                'message' => 'Đã thêm vào yêu thích'
            ], null, 201);
            
        } catch (Exception $e) {
            $this->error('Lỗi khi thêm vào yêu thích: ' . $e->getMessage(), 500);
        }
    }
    
    /**
     * DELETE /favorites/:id
     */
    public function remove($params) {
        $user = $this->getAuthUser();
        $favorite_id = intval($params['id'] ?? 0);
        
        if ($favorite_id <= 0) {
            $this->error('ID yêu thích không hợp lệ', 400);
        }
        
        try {
            // Check ownership
            $stmt = $this->db->prepare("SELECT user_id FROM favorites WHERE favorite_id = ?");
            $stmt->execute([$favorite_id]);
            $favorite = $stmt->fetch(PDO::FETCH_ASSOC);
            
            if (!$favorite) {
                $this->error('Mục yêu thích không tồn tại', 404);
            }
            
            if ($favorite['user_id'] != $user['user_id']) {
                $this->error('Bạn không có quyền xóa mục này', 403);
            }
            
            // Delete favorite
            $stmt = $this->db->prepare("DELETE FROM favorites WHERE favorite_id = ?");
            $stmt->execute([$favorite_id]);
            
            $this->success(['message' => 'Đã xóa khỏi yêu thích']);
            
        } catch (Exception $e) {
            $this->error('Lỗi khi xóa khỏi yêu thích: ' . $e->getMessage(), 500);
        }
    }
}
?>

