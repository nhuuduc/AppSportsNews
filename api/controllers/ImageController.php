<?php
/**
 * Image Controller - Xử lý upload ảnh cho bài viết
 */
class ImageController extends Controller {
    
    private $upload_dir;
    private $allowed_types = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    private $max_file_size = 5242880; // 5MB
    
    public function __construct() {
        parent::__construct();
        $this->upload_dir = __DIR__ . '/../../uploads/posts/';
        
        // Tạo thư mục nếu chưa tồn tại
        if (!file_exists($this->upload_dir)) {
            mkdir($this->upload_dir, 0755, true);
        }
    }
    
    /**
     * POST /posts/upload-image
     * Upload ảnh cho bài viết (dùng trong CKEditor)
     */
    public function uploadImage() {
        $user = $this->getAuthUser();
        
        // Kiểm tra xem user đã xác thực email chưa
        try {
            $stmt = $this->db->prepare("SELECT email_verified_at FROM users WHERE user_id = ?");
            $stmt->execute([$user['user_id']]);
            $userInfo = $stmt->fetch();
            
            if (!$userInfo || !$userInfo['email_verified_at']) {
                $this->error('Bạn cần xác thực email trước khi upload ảnh', 403);
            }
        } catch (Exception $e) {
            $this->error('Lỗi kiểm tra thông tin user: ' . $e->getMessage(), 500);
        }
        
        // Kiểm tra file upload
        if (!isset($_FILES['image']) || $_FILES['image']['error'] !== UPLOAD_ERR_OK) {
            $this->error('Không có file ảnh hoặc có lỗi khi upload', 400);
        }
        
        $file = $_FILES['image'];
        
        // Validate file type
        $finfo = finfo_open(FILEINFO_MIME_TYPE);
        $mime_type = finfo_file($finfo, $file['tmp_name']);
        finfo_close($finfo);
        
        if (!in_array($mime_type, $this->allowed_types)) {
            $this->error('Chỉ chấp nhận file ảnh (JPG, PNG, GIF, WEBP)', 400);
        }
        
        // Validate file size
        if ($file['size'] > $this->max_file_size) {
            $this->error('Kích thước file không được vượt quá 5MB', 400);
        }
        
        // Generate unique filename
        $extension = pathinfo($file['name'], PATHINFO_EXTENSION);
        $filename = 'post_' . $user['user_id'] . '_' . time() . '_' . bin2hex(random_bytes(8)) . '.' . $extension;
        $filepath = $this->upload_dir . $filename;
        
        // Move uploaded file
        if (!move_uploaded_file($file['tmp_name'], $filepath)) {
            $this->error('Lỗi khi lưu file ảnh', 500);
        }
        
        // Tạo URL cho ảnh
        $image_url = '/uploads/posts/' . $filename;
        
        // Lưu thông tin vào database (optional - để tracking)
        try {
            $stmt = $this->db->prepare(
                "INSERT INTO uploaded_images (user_id, filename, file_path, file_size, mime_type, created_at) 
                 VALUES (?, ?, ?, ?, ?, NOW())"
            );
            $stmt->execute([
                $user['user_id'],
                $filename,
                $image_url,
                $file['size'],
                $mime_type
            ]);
        } catch (Exception $e) {
            // Không cần fail nếu không lưu được vào database
            error_log('Warning: Could not save image info to database: ' . $e->getMessage());
        }
        
        // Trả về URL theo format CKEditor expects
        $this->success([
            'url' => $image_url,
            'filename' => $filename
        ], 'Upload ảnh thành công', 201);
    }
    
    /**
     * DELETE /posts/delete-image
     * Xóa ảnh (chỉ xóa ảnh của chính mình)
     */
    public function deleteImage() {
        $user = $this->getAuthUser();
        $data = $this->getRequestData();
        
        $filename = trim($data['filename'] ?? '');
        
        if (empty($filename)) {
            $this->error('Tên file không được để trống', 400);
        }
        
        // Validate filename to prevent directory traversal
        if (strpos($filename, '..') !== false || strpos($filename, '/') !== false) {
            $this->error('Tên file không hợp lệ', 400);
        }
        
        $filepath = $this->upload_dir . $filename;
        
        // Kiểm tra file có tồn tại không
        if (!file_exists($filepath)) {
            $this->error('File không tồn tại', 404);
        }
        
        // Kiểm tra quyền sở hữu
        try {
            $stmt = $this->db->prepare(
                "SELECT user_id FROM uploaded_images WHERE filename = ?"
            );
            $stmt->execute([$filename]);
            $image = $stmt->fetch();
            
            if ($image && $image['user_id'] != $user['user_id'] && $user['role'] !== 'admin') {
                $this->error('Bạn không có quyền xóa ảnh này', 403);
            }
        } catch (Exception $e) {
            // Nếu không có trong database, cho phép xóa (backward compatibility)
        }
        
        // Xóa file
        if (@unlink($filepath)) {
            // Xóa record trong database
            try {
                $stmt = $this->db->prepare("DELETE FROM uploaded_images WHERE filename = ?");
                $stmt->execute([$filename]);
            } catch (Exception $e) {
                error_log('Warning: Could not delete image record from database: ' . $e->getMessage());
            }
            
            $this->success([
                'message' => 'Xóa ảnh thành công'
            ]);
        } else {
            $this->error('Lỗi khi xóa file ảnh', 500);
        }
    }
}
?>

