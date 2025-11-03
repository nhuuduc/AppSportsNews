<?php
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../config/EmailService.php';

class UserModel {
    private $conn;
    private $emailService;
    
    public function __construct() {
        $database = Database::getInstance();
        $this->conn = $database->getConnection();
        
        // Initialize email service with error handling
        try {
            $this->emailService = new EmailService();
        } catch (Exception $e) {
            error_log("Failed to initialize EmailService: " . $e->getMessage());
            // Continue without email service - emails will fail gracefully
            $this->emailService = null;
        }
    }
    
    /**
     * Register a new user
     */
    public function register($email, $password, $username, $full_name = null) {
        // Validate email
        if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            return ['success' => false, 'message' => 'Email không hợp lệ'];
        }
        
        // Validate password length
        if (strlen($password) < 6) {
            return ['success' => false, 'message' => 'Mật khẩu phải có ít nhất 6 ký tự'];
        }
        
        // Check if email already exists
        $stmt = $this->conn->prepare("SELECT user_id FROM users WHERE email = ?");
        $stmt->execute([$email]);
        if ($stmt->fetch()) {
            return ['success' => false, 'message' => 'Email đã được sử dụng'];
        }
        
        // Check if username already exists
        $stmt = $this->conn->prepare("SELECT user_id FROM users WHERE username = ?");
        $stmt->execute([$username]);
        if ($stmt->fetch()) {
            return ['success' => false, 'message' => 'Tên đăng nhập đã được sử dụng'];
        }
        
        // Hash password
        $password_hash = password_hash($password, PASSWORD_BCRYPT);
        
        // Insert user
        try {
            $this->conn->beginTransaction();
            
            $stmt = $this->conn->prepare(
                "INSERT INTO users (email, username, password_hash, full_name, role) 
                 VALUES (?, ?, ?, ?, 'user')"
            );
            $stmt->execute([$email, $username, $password_hash, $full_name]);
            $user_id = $this->conn->lastInsertId();
            
            // Generate 6-digit verification code
            $verification_token = str_pad((string)random_int(100000, 999999), 6, '0', STR_PAD_LEFT);
            $expires_at = date('Y-m-d H:i:s', strtotime('+15 minutes'));
            
            $stmt = $this->conn->prepare(
                "INSERT INTO email_verifications (user_id, verification_token, expires_at, attempts, last_attempt_at) 
                 VALUES (?, ?, ?, 0, NULL)"
            );
            $stmt->execute([$user_id, $verification_token, $expires_at]);
            
            $this->conn->commit();
            
            // Send verification email if service is available
            if ($this->emailService !== null) {
                try {
                    $this->emailService->sendVerificationEmail($email, $full_name ?: $username, $verification_token);
                } catch (Exception $e) {
                    error_log("Failed to send verification email: " . $e->getMessage());
                    // Continue anyway - user is registered
                }
            }
            
            return [
                'success' => true,
                'message' => 'Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản.',
                'user_id' => $user_id,
                'email' => $email,
                'username' => $username
            ];
            
        } catch (Exception $e) {
            $this->conn->rollBack();
            return ['success' => false, 'message' => 'Lỗi: ' . $e->getMessage()];
        }
    }
    
    /**
     * Verify email with 6-digit code
     */
    public function verifyEmail($token) {
        try {
            // Validate 6-digit code format
            if (!preg_match('/^\d{6}$/', $token)) {
                return ['success' => false, 'message' => 'Mã xác thực phải là 6 chữ số'];
            }
            
            $stmt = $this->conn->prepare(
                "SELECT ev.user_id, ev.attempts, u.email, u.username 
                 FROM email_verifications ev 
                 JOIN users u ON ev.user_id = u.user_id 
                 WHERE ev.verification_token = ? AND ev.expires_at > NOW()"
            );
            $stmt->execute([$token]);
            $result = $stmt->fetch();
            
            if (!$result) {
                return ['success' => false, 'message' => 'Mã xác thực không hợp lệ hoặc đã hết hạn'];
            }
            
            // Check if too many failed attempts (rate limiting)
            if ($result['attempts'] >= 5) {
                return ['success' => false, 'message' => 'Bạn đã nhập sai quá nhiều lần. Vui lòng yêu cầu mã mới.'];
            }
            
            $this->conn->beginTransaction();
            
            // Update user
            $stmt = $this->conn->prepare(
                "UPDATE users SET email_verified_at = NOW() WHERE user_id = ?"
            );
            $stmt->execute([$result['user_id']]);
            
            // Delete verification token
            $stmt = $this->conn->prepare(
                "DELETE FROM email_verifications WHERE user_id = ?"
            );
            $stmt->execute([$result['user_id']]);
            
            $this->conn->commit();
            
            return [
                'success' => true,
                'message' => 'Email đã được xác thực thành công',
                'user_id' => $result['user_id'],
                'email' => $result['email'],
                'username' => $result['username']
            ];
            
        } catch (Exception $e) {
            $this->conn->rollBack();
            return ['success' => false, 'message' => 'Lỗi: ' . $e->getMessage()];
        }
    }
    
    /**
     * Record failed verification attempt
     */
    public function recordFailedVerification($token) {
        try {
            $stmt = $this->conn->prepare(
                "UPDATE email_verifications 
                 SET attempts = attempts + 1, last_attempt_at = NOW() 
                 WHERE verification_token = ? AND expires_at > NOW()"
            );
            $stmt->execute([$token]);
            return true;
        } catch (Exception $e) {
            return false;
        }
    }
    
    /**
     * Resend verification email
     */
    public function resendVerification($email) {
        try {
            // Get user info
            $stmt = $this->conn->prepare(
                "SELECT user_id, username, full_name, email_verified_at 
                 FROM users WHERE email = ?"
            );
            $stmt->execute([$email]);
            $user = $stmt->fetch();
            
            if (!$user) {
                return ['success' => false, 'message' => 'Email không tồn tại'];
            }
            
            if ($user['email_verified_at']) {
                return ['success' => false, 'message' => 'Email đã được xác thực'];
            }
            
            $this->conn->beginTransaction();
            
            // Delete old tokens
            $stmt = $this->conn->prepare(
                "DELETE FROM email_verifications WHERE user_id = ?"
            );
            $stmt->execute([$user['user_id']]);
            
            // Generate new 6-digit code
            $verification_token = str_pad((string)random_int(100000, 999999), 6, '0', STR_PAD_LEFT);
            $expires_at = date('Y-m-d H:i:s', strtotime('+15 minutes'));
            
            $stmt = $this->conn->prepare(
                "INSERT INTO email_verifications (user_id, verification_token, expires_at, attempts, last_attempt_at) 
                 VALUES (?, ?, ?, 0, NULL)"
            );
            $stmt->execute([$user['user_id'], $verification_token, $expires_at]);
            
            $this->conn->commit();
            
            // Send email if service is available
            if ($this->emailService !== null) {
                try {
                    $name = $user['full_name'] ?: $user['username'];
                    $this->emailService->sendVerificationEmail($email, $name, $verification_token);
                } catch (Exception $e) {
                    error_log("Failed to send verification email: " . $e->getMessage());
                    // Continue anyway
                }
            }
            
            return [
                'success' => true,
                'message' => 'Email xác thực đã được gửi lại'
            ];
            
        } catch (Exception $e) {
            $this->conn->rollBack();
            return ['success' => false, 'message' => 'Lỗi: ' . $e->getMessage()];
        }
    }
    
    /**
     * Login user
     */
    public function login($email, $password, $device_info = null, $ip_address = null) {
        try {
            // Get user
            $stmt = $this->conn->prepare(
                "SELECT user_id, email, username, password_hash, full_name, avatar_url, 
                        role, email_verified_at, is_active 
                 FROM users WHERE email = ?"
            );
            $stmt->execute([$email]);
            $user = $stmt->fetch();
            
            if (!$user) {
                return ['success' => false, 'message' => 'Email hoặc mật khẩu không đúng'];
            }
            
            // Verify password
            if (!password_verify($password, $user['password_hash'])) {
                return ['success' => false, 'message' => 'Email hoặc mật khẩu không đúng'];
            }
            
            // Check if account is active
            if (!$user['is_active']) {
                return ['success' => false, 'message' => 'Tài khoản đã bị khóa'];
            }
            
            // Create session token
            $session_token = bin2hex(random_bytes(32));
            $expires_at = date('Y-m-d H:i:s', strtotime('+30 days'));
            
            $stmt = $this->conn->prepare(
                "INSERT INTO user_sessions (user_id, session_token, device_info, ip_address, expires_at) 
                 VALUES (?, ?, ?, ?, ?)"
            );
            $stmt->execute([
                $user['user_id'],
                $session_token,
                $device_info,
                $ip_address,
                $expires_at
            ]);
            
            return [
                'success' => true,
                'message' => 'Đăng nhập thành công',
                'session_token' => $session_token,
                'user' => [
                    'user_id' => $user['user_id'],
                    'email' => $user['email'],
                    'username' => $user['username'],
                    'full_name' => $user['full_name'],
                    'avatar_url' => $user['avatar_url'],
                    'role' => $user['role'],
                    'email_verified' => $user['email_verified_at'] !== null
                ]
            ];
            
        } catch (Exception $e) {
            return ['success' => false, 'message' => 'Lỗi: ' . $e->getMessage()];
        }
    }
    
    /**
     * Verify session token and get user info
     */
    public function verifySession($session_token) {
        try {
            $stmt = $this->conn->prepare(
                "SELECT u.user_id, u.email, u.username, u.full_name, u.avatar_url, 
                        u.role, u.email_verified_at, u.is_active, s.expires_at
                 FROM user_sessions s
                 JOIN users u ON s.user_id = u.user_id
                 WHERE s.session_token = ? AND s.expires_at > NOW()"
            );
            $stmt->execute([$session_token]);
            $result = $stmt->fetch();
            
            if (!$result) {
                return ['success' => false, 'message' => 'Session không hợp lệ hoặc đã hết hạn'];
            }
            
            if (!$result['is_active']) {
                return ['success' => false, 'message' => 'Tài khoản đã bị khóa'];
            }
            
            return [
                'success' => true,
                'user' => [
                    'user_id' => $result['user_id'],
                    'email' => $result['email'],
                    'username' => $result['username'],
                    'full_name' => $result['full_name'],
                    'avatar_url' => $result['avatar_url'],
                    'role' => $result['role'],
                    'email_verified' => $result['email_verified_at'] !== null
                ]
            ];
            
        } catch (Exception $e) {
            return ['success' => false, 'message' => 'Lỗi: ' . $e->getMessage()];
        }
    }
    
    /**
     * Logout user
     */
    public function logout($session_token) {
        try {
            $stmt = $this->conn->prepare(
                "DELETE FROM user_sessions WHERE session_token = ?"
            );
            $stmt->execute([$session_token]);
            
            return [
                'success' => true,
                'message' => 'Đăng xuất thành công'
            ];
            
        } catch (Exception $e) {
            return ['success' => false, 'message' => 'Lỗi: ' . $e->getMessage()];
        }
    }
    
    /**
     * Clean up expired sessions and verification tokens
     */
    public function cleanup() {
        try {
            // Delete expired sessions
            $stmt = $this->conn->prepare(
                "DELETE FROM user_sessions WHERE expires_at < NOW()"
            );
            $stmt->execute();
            $deleted_sessions = $stmt->rowCount();
            
            // Delete expired verifications
            $stmt = $this->conn->prepare(
                "DELETE FROM email_verifications WHERE expires_at < NOW()"
            );
            $stmt->execute();
            $deleted_verifications = $stmt->rowCount();
            
            return [
                'success' => true,
                'deleted_sessions' => $deleted_sessions,
                'deleted_verifications' => $deleted_verifications
            ];
            
        } catch (Exception $e) {
            return ['success' => false, 'message' => 'Lỗi: ' . $e->getMessage()];
        }
    }
}
?>

