<?php
/**
 * Auth Controller - Xử lý đăng ký, đăng nhập, xác thực
 */
class AuthController extends Controller {
    private $userModel;
    
    public function __construct() {
        parent::__construct();
        $this->userModel = new UserModel();
    }
    
    /**
     * POST /auth/register
     */
    public function register() {
        $data = $this->getRequestData();
        
        $email = trim($data['email'] ?? '');
        $password = $data['password'] ?? '';
        $username = trim($data['username'] ?? '');
        $full_name = trim($data['full_name'] ?? '');
        
        if (empty($email) || empty($password) || empty($username)) {
            $this->error('Email, mật khẩu và tên đăng nhập là bắt buộc', 400);
        }
        
        $result = $this->userModel->register($email, $password, $username, $full_name);
        
        if ($result['success']) {
            $this->success($result, 'Đăng ký thành công', 201);
        } else {
            $this->error($result['message'], 400);
        }
    }
    
    /**
     * POST /auth/login
     * Supports login with email or username
     */
    public function login() {
        $data = $this->getRequestData();
        
        $identifier = trim($data['email'] ?? ''); // Can be email or username
        $password = $data['password'] ?? '';
        $device_info = $data['device_info'] ?? null;
        $ip_address = $_SERVER['REMOTE_ADDR'] ?? null;
        
        if (empty($identifier) || empty($password)) {
            $this->error('Tên đăng nhập/Email và mật khẩu là bắt buộc', 400);
        }
        
        $result = $this->userModel->login($identifier, $password, $device_info, $ip_address);
        
        if ($result['success']) {
            $this->success($result);
        } else {
            $this->error($result['message'], 401);
        }
    }
    
    /**
     * POST /auth/logout
     */
    public function logout() {
        $user = $this->getAuthUser();
        $data = $this->getRequestData();
        
        $auth_header = $_SERVER['HTTP_AUTHORIZATION'] ?? '';
        $token = '';
        
        if (preg_match('/Bearer\s+(.*)$/i', $auth_header, $matches)) {
            $token = $matches[1];
        } else {
            $token = $data['token'] ?? '';
        }
        
        if (empty($token)) {
            $this->error('Session token là bắt buộc', 400);
        }
        
        $result = $this->userModel->logout($token);
        
        if ($result['success']) {
            $this->success($result);
        } else {
            $this->error($result['message'], 400);
        }
    }
    
    /**
     * GET /auth/me
     */
    public function me() {
        $user = $this->getAuthUser();
        
        // Lấy thông tin mới nhất từ database
        try {
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
                
                $this->success(['user' => $profile]);
            } else {
                $this->error('Không tìm thấy thông tin người dùng', 404);
            }
        } catch (Exception $e) {
            $this->error('Lỗi khi lấy thông tin người dùng: ' . $e->getMessage(), 500);
        }
    }
    
    /**
     * POST /auth/verify
     */
    public function verify() {
        $data = $this->getRequestData();
        $query = $this->getQueryParams();
        
        $token = $query['token'] ?? $data['token'] ?? '';
        
        if (empty($token)) {
            $this->error('Mã xác thực là bắt buộc', 400);
        }
        
        $result = $this->userModel->verifyEmail($token);
        
        if ($result['success']) {
            // Check if this is a web browser request
            if (isset($query['token'])) {
                // Return HTML page
                echo $this->getVerificationSuccessPage();
            } else {
                $this->success($result);
            }
        } else {
            if (isset($query['token'])) {
                echo $this->getVerificationErrorPage($result['message']);
            } else {
                $this->error($result['message'], 400);
            }
        }
    }
    
    /**
     * POST /auth/resend-verification
     */
    public function resendVerification() {
        $data = $this->getRequestData();
        
        $email = trim($data['email'] ?? '');
        
        if (empty($email)) {
            $this->error('Email là bắt buộc', 400);
        }
        
        $result = $this->userModel->resendVerification($email);
        
        if ($result['success']) {
            $this->success($result);
        } else {
            $this->error($result['message'], 400);
        }
    }
    
    /**
     * GET /auth/verify-session
     */
    public function verifySession() {
        $query = $this->getQueryParams();
        $auth_header = $_SERVER['HTTP_AUTHORIZATION'] ?? '';
        $token = '';
        
        if (preg_match('/Bearer\s+(.*)$/i', $auth_header, $matches)) {
            $token = $matches[1];
        } else {
            $token = $query['token'] ?? '';
        }
        
        if (empty($token)) {
            $this->error('Session token là bắt buộc', 400);
        }
        
        $result = $this->userModel->verifySession($token);
        
        if ($result['success']) {
            $this->success($result);
        } else {
            $this->error($result['message'], 401);
        }
    }
    
    /**
     * HTML page for successful verification
     */
    private function getVerificationSuccessPage() {
        return '<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Xác thực thành công</title>
    <style>
        body { font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background: #f5f5f5; }
        .container { text-align: center; background: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .success { color: #4caf50; font-size: 48px; margin-bottom: 20px; }
        h1 { color: #333; margin-bottom: 10px; }
        p { color: #666; margin-bottom: 30px; }
        .button { display: inline-block; background: #1976d2; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="success">✓</div>
        <h1>Xác thực thành công!</h1>
        <p>Email của bạn đã được xác thực. Bạn có thể đóng trang này và quay lại ứng dụng.</p>
        <a href="#" class="button" onclick="window.close(); return false;">Đóng</a>
    </div>
</body>
</html>';
    }
    
    /**
     * HTML page for failed verification
     */
    private function getVerificationErrorPage($message) {
        return '<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Xác thực thất bại</title>
    <style>
        body { font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; background: #f5f5f5; }
        .container { text-align: center; background: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .error { color: #f44336; font-size: 48px; margin-bottom: 20px; }
        h1 { color: #333; margin-bottom: 10px; }
        p { color: #666; margin-bottom: 30px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="error">✗</div>
        <h1>Xác thực thất bại</h1>
        <p>' . htmlspecialchars($message) . '</p>
    </div>
</body>
</html>';
    }
}
?>

