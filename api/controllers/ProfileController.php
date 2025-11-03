<?php
/**
 * Profile Controller - Xử lý thông tin người dùng
 */
class ProfileController extends Controller {
    
    /**
     * GET /profile
     */
    public function show() {
        $user = $this->getAuthUser();
        
        // Get full user profile
        $stmt = $this->db->prepare(
            "SELECT user_id, username, email, full_name, avatar_url, phone, 
                    date_of_birth, gender, role, email_verified_at, created_at 
             FROM users WHERE user_id = ?"
        );
        $stmt->execute([$user['user_id']]);
        $profile = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if ($profile) {
            $profile['user_id'] = (int)$profile['user_id'];
            $profile['email_verified'] = $profile['email_verified_at'] !== null;
            unset($profile['email_verified_at']);
        }
        
        $this->success(['profile' => $profile]);
    }
    
    /**
     * PUT /profile
     */
    public function update() {
        $user = $this->getAuthUser();
        $data = $this->getRequestData();
        
        $allowed_fields = ['full_name', 'phone', 'date_of_birth', 'gender', 'avatar_url'];
        $update_fields = [];
        $update_values = [];
        
        foreach ($allowed_fields as $field) {
            if (isset($data[$field])) {
                $update_fields[] = "$field = ?";
                $update_values[] = $data[$field];
            }
        }
        
        if (empty($update_fields)) {
            $this->error('Không có thông tin nào để cập nhật', 400);
        }
        
        try {
            $update_values[] = $user['user_id'];
            $sql = "UPDATE users SET " . implode(', ', $update_fields) . " WHERE user_id = ?";
            $stmt = $this->db->prepare($sql);
            $stmt->execute($update_values);
            
            // Xóa cache profile và articles liên quan
            ResponseHelper::clearCacheByKey("profile_" . $user['user_id']);
            ResponseHelper::clearCacheByPattern(['articles_']);
            
            // Lấy lại thông tin đã cập nhật từ database
            $stmt = $this->db->prepare(
                "SELECT user_id, username, email, full_name, avatar_url, phone, 
                        date_of_birth, gender, role, email_verified_at, created_at 
                 FROM users WHERE user_id = ?"
            );
            $stmt->execute([$user['user_id']]);
            $profile = $stmt->fetch(PDO::FETCH_ASSOC);
            
            if ($profile) {
                $profile['user_id'] = (int)$profile['user_id'];
                $profile['email_verified'] = $profile['email_verified_at'] !== null;
                unset($profile['email_verified_at']);
            }
            
            $this->success(['profile' => $profile], 'Cập nhật thông tin thành công');
        } catch (Exception $e) {
            $this->error('Lỗi khi cập nhật thông tin: ' . $e->getMessage(), 500);
        }
    }
    
    /**
     * PUT /profile/password
     */
    public function updatePassword() {
        $user = $this->getAuthUser();
        $data = $this->getRequestData();
        
        $current_password = $data['current_password'] ?? '';
        $new_password = $data['new_password'] ?? '';
        
        if (empty($current_password) || empty($new_password)) {
            $this->error('Vui lòng nhập đầy đủ thông tin', 400);
        }
        
        if (strlen($new_password) < 6) {
            $this->error('Mật khẩu mới phải có ít nhất 6 ký tự', 400);
        }
        
        try {
            // Verify current password
            $stmt = $this->db->prepare("SELECT password_hash FROM users WHERE user_id = ?");
            $stmt->execute([$user['user_id']]);
            $result = $stmt->fetch(PDO::FETCH_ASSOC);
            
            if (!$result || !password_verify($current_password, $result['password_hash'])) {
                $this->error('Mật khẩu hiện tại không đúng', 400);
            }
            
            // Update password
            $new_password_hash = password_hash($new_password, PASSWORD_BCRYPT);
            $stmt = $this->db->prepare("UPDATE users SET password_hash = ? WHERE user_id = ?");
            $stmt->execute([$new_password_hash, $user['user_id']]);
            
            $this->success([], 'Đổi mật khẩu thành công');
        } catch (Exception $e) {
            $this->error('Lỗi khi đổi mật khẩu: ' . $e->getMessage(), 500);
        }
    }
    
    /**
     * GET /profile/favorites
     * Return liked articles in ArticlesResponse format
     */
    public function favorites() {
        $user = $this->getAuthUser();
        $params = $this->getQueryParams();
        
        $page = isset($params['page']) ? intval($params['page']) : 1;
        $limit = isset($params['limit']) ? min(intval($params['limit']), 50) : 20;
        $offset = ($page - 1) * $limit;
        
        try {
            // Get liked articles from article_views table
            $sql = "SELECT a.article_id, a.title, a.slug, a.thumbnail_url, 
                           a.summary, a.content, a.category_id, a.author_id,
                           a.status, a.published_at, a.created_at, a.updated_at,
                           a.is_featured, a.is_breaking_news,
                           av.view_count, av.like_count, av.comment_count,
                           c.category_name, c.category_slug,
                           u.username as author_name, u.full_name as author_full_name
                    FROM article_views av
                    JOIN articles a ON av.article_id = a.article_id
                    LEFT JOIN categories c ON a.category_id = c.category_id
                    LEFT JOIN users u ON a.author_id = u.user_id
                    WHERE (
                        CONCAT(',', av.liked_user_ids, ',') LIKE CONCAT('%,', ?, ',%')
                        OR av.liked_user_ids = ?
                    )
                    AND a.status = 'published'
                    ORDER BY av.updated_at DESC
                    LIMIT ? OFFSET ?";
            
            $stmt = $this->db->prepare($sql);
            $stmt->execute([$user['user_id'], $user['user_id'], $limit, $offset]);
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
                $article['is_liked'] = true; // All articles in this list are liked
                
                // Use full_name if available, otherwise username
                $article['author_name'] = $article['author_full_name'] ?: $article['author_name'];
                unset($article['author_full_name']);
            }
            
            // Get total count
            $count_sql = "SELECT COUNT(DISTINCT av.article_id) as total
                         FROM article_views av
                         JOIN articles a ON av.article_id = a.article_id
                         WHERE (
                             CONCAT(',', av.liked_user_ids, ',') LIKE CONCAT('%,', ?, ',%')
                             OR av.liked_user_ids = ?
                         )
                         AND a.status = 'published'";
            $count_stmt = $this->db->prepare($count_sql);
            $count_stmt->execute([$user['user_id'], $user['user_id']]);
            $total = $count_stmt->fetch(PDO::FETCH_ASSOC)['total'];
            
            // Return in ArticlesResponse format
            $this->success([
                'error' => false,
                'articles' => $articles,
                'total' => (int)$total,
                'page' => $page,
                'page_size' => $limit
            ]);
        } catch (Exception $e) {
            $this->error('Lỗi khi lấy danh sách bài viết đã thích: ' . $e->getMessage(), 500);
        }
    }
    
    /**
     * GET /profile/avatar
     * Get current user's avatar information
     */
    public function getAvatar() {
        $user = $this->getAuthUser();
        
        try {
            $stmt = $this->db->prepare(
                "SELECT avatar_url FROM users WHERE user_id = ?"
            );
            $stmt->execute([$user['user_id']]);
            $result = $stmt->fetch(PDO::FETCH_ASSOC);
            
            $this->success([
                'avatar_url' => $result['avatar_url'] ?? null
            ]);
        } catch (Exception $e) {
            $this->error('Lỗi khi lấy thông tin avatar: ' . $e->getMessage(), 500);
        }
    }
    
    /**
     * POST /profile/avatar
     * Upload avatar
     */
    public function uploadAvatar() {
        $user = $this->getAuthUser();
        
        try {
            // Check if file was uploaded
            if (!isset($_FILES['avatar'])) {
                $this->error('Không có file trong request', 400);
                return;
            }
            
            if ($_FILES['avatar']['error'] !== UPLOAD_ERR_OK) {
                $error_messages = [
                    UPLOAD_ERR_INI_SIZE => 'File vượt quá kích thước tối đa cho phép',
                    UPLOAD_ERR_FORM_SIZE => 'File vượt quá kích thước MAX_FILE_SIZE',
                    UPLOAD_ERR_PARTIAL => 'File chỉ được tải lên một phần',
                    UPLOAD_ERR_NO_FILE => 'Không có file nào được tải lên',
                    UPLOAD_ERR_NO_TMP_DIR => 'Thiếu thư mục tạm thời',
                    UPLOAD_ERR_CANT_WRITE => 'Không thể ghi file vào ổ đĩa',
                    UPLOAD_ERR_EXTENSION => 'Upload bị dừng bởi extension'
                ];
                $error_code = $_FILES['avatar']['error'];
                $error_msg = $error_messages[$error_code] ?? 'Lỗi upload không xác định';
                $this->error($error_msg, 400);
                return;
            }
            
            $file = $_FILES['avatar'];
            $userId = $user['user_id'];
            
            // Validate file type
            $allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
            $fileType = mime_content_type($file['tmp_name']);
            
            if (!in_array($fileType, $allowedTypes)) {
                $this->error('Định dạng file không hợp lệ. Chỉ chấp nhận JPG, PNG, GIF, WEBP', 400);
                return;
            }
            
            // Validate file size (max 5MB)
            $maxSize = 5 * 1024 * 1024; // 5MB in bytes
            if ($file['size'] > $maxSize) {
                $this->error('File quá lớn. Kích thước tối đa là 5MB', 400);
                return;
            }
            
            // Generate unique filename
            $extension = pathinfo($file['name'], PATHINFO_EXTENSION);
            $filename = 'avatar_' . $userId . '_' . time() . '.' . $extension;
            
            // Upload directory
            $uploadDir = __DIR__ . '/../../uploads/avatars/';
            if (!file_exists($uploadDir)) {
                mkdir($uploadDir, 0755, true);
            }
            
            $uploadPath = $uploadDir . $filename;
            
            // Delete old avatar if exists
            $stmt = $this->db->prepare("SELECT avatar_url FROM users WHERE user_id = ?");
            $stmt->execute([$userId]);
            $oldAvatar = $stmt->fetchColumn();
            
            if ($oldAvatar && file_exists(__DIR__ . '/../..' . $oldAvatar)) {
                unlink(__DIR__ . '/../..' . $oldAvatar);
            }
            
            // Move uploaded file
            if (!move_uploaded_file($file['tmp_name'], $uploadPath)) {
                $this->error('Không thể lưu file', 500);
                return;
            }
            
            // Update database
            $avatarUrl = '/uploads/avatars/' . $filename;
            $stmt = $this->db->prepare("UPDATE users SET avatar_url = ? WHERE user_id = ?");
            $stmt->execute([$avatarUrl, $userId]);
            
            // Clear profile cache
            ResponseHelper::clearCacheByKey("profile_" . $userId);
            
            // Lấy lại thông tin đã cập nhật từ database
            $stmt = $this->db->prepare(
                "SELECT user_id, username, email, full_name, avatar_url, phone, 
                        date_of_birth, gender, role, email_verified_at, created_at 
                 FROM users WHERE user_id = ?"
            );
            $stmt->execute([$userId]);
            $profile = $stmt->fetch(PDO::FETCH_ASSOC);
            
            if ($profile) {
                $profile['user_id'] = (int)$profile['user_id'];
                $profile['email_verified'] = $profile['email_verified_at'] !== null;
                unset($profile['email_verified_at']);
            }
            
            $this->success([
                'avatar_url' => $avatarUrl,
                'profile' => $profile
            ], 'Upload avatar thành công');
            
        } catch (Exception $e) {
            $this->error('Lỗi khi upload avatar: ' . $e->getMessage(), 500);
        }
    }
    
    /**
     * DELETE /profile/avatar
     * Delete current user's avatar
     */
    public function deleteAvatar() {
        $user = $this->getAuthUser();
        
        try {
            // Get current avatar
            $stmt = $this->db->prepare("SELECT avatar_url FROM users WHERE user_id = ?");
            $stmt->execute([$user['user_id']]);
            $result = $stmt->fetch(PDO::FETCH_ASSOC);
            
            if ($result && $result['avatar_url']) {
                $avatarPath = __DIR__ . '/../..' . $result['avatar_url'];
                
                // Delete file if exists
                if (file_exists($avatarPath)) {
                    @unlink($avatarPath);
                }
                
                // Update database to remove avatar
                $stmt = $this->db->prepare("UPDATE users SET avatar_url = NULL WHERE user_id = ?");
                $stmt->execute([$user['user_id']]);
                
                // Clear profile cache
                ResponseHelper::clearCacheByKey("profile_" . $user['user_id']);
            }
            
            $this->success([], 'Đã xóa avatar thành công');
            
        } catch (Exception $e) {
            $this->error('Lỗi khi xóa avatar: ' . $e->getMessage(), 500);
        }
    }
}
?>

