<?php
// Email service for sending verification emails using pure PHP SMTP
class EmailService {
    private $from_email;
    private $from_name;
    private $base_url;
    
    // SMTP Configuration
    private $smtp_host;
    private $smtp_port;
    private $smtp_username;
    private $smtp_password;
    private $smtp_secure; // 'tls' or 'ssl'
    private $smtp_timeout = 30;
    
    public function __construct() {
        // Email configuration
        $this->from_email = "MS_yJNemv@nhd6.site"; // Change this to your email
        $this->from_name = "Sp News";
        
        // Get base URL safely
        try {
            $this->base_url = $this->getBaseUrl();
        } catch (Exception $e) {
            $this->base_url = "http://localhost";
            error_log("Failed to get base URL: " . $e->getMessage());
        }
        
        // SMTP Configuration - Update these with your SMTP credentials
        $this->smtp_host = "smtp.mailersend.net"; // e.g., smtp.gmail.com, smtp.mailtrap.io
        $this->smtp_port = 587; // 587 for TLS, 465 for SSL
        $this->smtp_username = "MS_yJNemv@nhd6.site"; // Your SMTP username/email
        $this->smtp_password = "mssp.9v2UNPy.z86org8jd7elew13.OQu4I3K"; // Your SMTP password or app password
        $this->smtp_secure = "tls"; // 'tls' or 'ssl'
    }
    
    private function getBaseUrl() {
        $protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off' || 
                     (!empty($_SERVER['SERVER_PORT']) && $_SERVER['SERVER_PORT'] == 443)) ? "https://" : "http://";
        $host = isset($_SERVER['HTTP_HOST']) ? $_SERVER['HTTP_HOST'] : 'localhost';
        return $protocol . $host;
    }
    
    /**
     * Send verification email
     * @param string $to_email Recipient email
     * @param string $to_name Recipient name
     * @param string $verification_token Verification token
     * @return bool Success status
     */
    public function sendVerificationEmail($to_email, $to_name, $verification_token) {
        $subject = "Xác thực email của bạn - NHD News";
        
        // Verification URL
        $verification_url = $this->base_url . "/api/auth.php?action=verify&token=" . urlencode($verification_token);
        
        // HTML email body
        $html_body = $this->getVerificationEmailTemplate($to_name, $verification_url, $verification_token);
        
        // Plain text version
        $text_body = "Xin chào $to_name,\n\n" .
                    "Cảm ơn bạn đã đăng ký tài khoản tại NHD News.\n\n" .
                    "Vui lòng nhập mã xác thực 6 số này trong ứng dụng:\n\n" .
                    "MÃ XÁC THỰC: $verification_token\n\n" .
                    "Mã này sẽ hết hạn sau 15 phút.\n\n" .
                    "Nếu bạn không tạo tài khoản này, vui lòng bỏ qua email này.\n\n" .
                    "Trân trọng,\n" .
                    "Đội ngũ NHD News";
        
        return $this->sendEmail($to_email, $to_name, $subject, $html_body, $text_body);
    }
    
    /**
     * Send password reset email
     * @param string $to_email Recipient email
     * @param string $to_name Recipient name
     * @param string $reset_token Reset token
     * @return bool Success status
     */
    public function sendPasswordResetEmail($to_email, $to_name, $reset_token) {
        $subject = "Đặt lại mật khẩu - NHD News";
        
        // Reset URL
        $reset_url = $this->base_url . "/api/auth.php?action=reset-password&token=" . urlencode($reset_token);
        
        // HTML email body
        $html_body = $this->getPasswordResetEmailTemplate($to_name, $reset_url, $reset_token);
        
        // Plain text version
        $text_body = "Xin chào $to_name,\n\n" .
                    "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.\n\n" .
                    "Vui lòng click vào link sau để đặt lại mật khẩu:\n" .
                    "$reset_url\n\n" .
                    "Hoặc nhập mã này trong ứng dụng:\n" .
                    "$reset_token\n\n" .
                    "Link này sẽ hết hạn sau 1 giờ.\n\n" .
                    "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" .
                    "Trân trọng,\n" .
                    "Đội ngũ NHD News";
        
        return $this->sendEmail($to_email, $to_name, $subject, $html_body, $text_body);
    }
    
    /**
     * Send email using pure PHP SMTP
     */
    private function sendEmail($to_email, $to_name, $subject, $html_body, $text_body) {
        // For development on localhost, log emails instead of sending
        $server_name = isset($_SERVER['SERVER_NAME']) ? $_SERVER['SERVER_NAME'] : '';
        if ($server_name === 'localhost' || strpos($server_name, '127.0.0.1') !== false) {
            $log_file = __DIR__ . '/../logs/emails.log';
            $log_dir = dirname($log_file);
            if (!file_exists($log_dir)) {
                mkdir($log_dir, 0755, true);
            }
            
            $log_content = date('Y-m-d H:i:s') . " - Email to: $to_email ($to_name)\n" .
                          "Subject: $subject\n" .
                          "Body:\n$text_body\n" .
                          "HTML:\n$html_body\n" .
                          str_repeat("=", 80) . "\n\n";
            
            file_put_contents($log_file, $log_content, FILE_APPEND);
            return true;
        }
        
        try {
            // Connect to SMTP server
            $socket = $this->connectToSMTP();
            if (!$socket) {
                error_log("Failed to connect to SMTP server");
                return false;
            }
            
            // Perform SMTP handshake
            if (!$this->smtpHandshake($socket)) {
                fclose($socket);
                return false;
            }
            
            // Send email data
            $result = $this->sendEmailData($socket, $to_email, $to_name, $subject, $html_body, $text_body);
            
            // Close connection
            $this->smtpCommand($socket, "QUIT\r\n", 221);
            fclose($socket);
            
            if ($result) {
                error_log("Email sent successfully to: $to_email");
            }
            
            return $result;
            
        } catch (Exception $e) {
            error_log("Email sending failed: " . $e->getMessage());
            return false;
        }
    }
    
    /**
     * Connect to SMTP server
     */
    private function connectToSMTP() {
        $errno = 0;
        $errstr = '';
        
        // Connect based on security type
        if ($this->smtp_secure === 'ssl') {
            $host = 'ssl://' . $this->smtp_host;
        } else {
            $host = $this->smtp_host;
        }
        
        $socket = @fsockopen($host, $this->smtp_port, $errno, $errstr, $this->smtp_timeout);
        
        if (!$socket) {
            error_log("SMTP connection failed: $errstr ($errno)");
            return false;
        }
        
        // Set timeout for socket operations
        stream_set_timeout($socket, $this->smtp_timeout);
        
        // Read greeting
        $response = fgets($socket, 515);
        if (substr($response, 0, 3) != '220') {
            error_log("SMTP greeting failed: $response");
            fclose($socket);
            return false;
        }
        
        return $socket;
    }
    
    /**
     * Perform SMTP handshake and authentication
     */
    private function smtpHandshake($socket) {
        // Send EHLO
        if (!$this->smtpCommand($socket, "EHLO " . $this->smtp_host . "\r\n", 250)) {
            return false;
        }
        
        // Start TLS if needed
        if ($this->smtp_secure === 'tls') {
            if (!$this->smtpCommand($socket, "STARTTLS\r\n", 220)) {
                return false;
            }
            
            // Enable crypto
            if (!stream_socket_enable_crypto($socket, true, STREAM_CRYPTO_METHOD_TLS_CLIENT)) {
                error_log("Failed to enable TLS");
                return false;
            }
            
            // Send EHLO again after STARTTLS
            if (!$this->smtpCommand($socket, "EHLO " . $this->smtp_host . "\r\n", 250)) {
                return false;
            }
        }
        
        // Authenticate
        if (!$this->smtpCommand($socket, "AUTH LOGIN\r\n", 334)) {
            return false;
        }
        
        if (!$this->smtpCommand($socket, base64_encode($this->smtp_username) . "\r\n", 334)) {
            return false;
        }
        
        if (!$this->smtpCommand($socket, base64_encode($this->smtp_password) . "\r\n", 235)) {
            error_log("SMTP authentication failed");
            return false;
        }
        
        return true;
    }
    
    /**
     * Send email data via SMTP
     */
    private function sendEmailData($socket, $to_email, $to_name, $subject, $html_body, $text_body) {
        // MAIL FROM
        if (!$this->smtpCommand($socket, "MAIL FROM: <{$this->from_email}>\r\n", 250)) {
            return false;
        }
        
        // RCPT TO
        if (!$this->smtpCommand($socket, "RCPT TO: <{$to_email}>\r\n", 250)) {
            return false;
        }
        
        // DATA
        if (!$this->smtpCommand($socket, "DATA\r\n", 354)) {
            return false;
        }
        
        // Build email headers and body
        $boundary = "----=_Part_" . md5(time());
        
        $headers = "From: {$this->from_name} <{$this->from_email}>\r\n";
        $headers .= "To: {$to_name} <{$to_email}>\r\n";
        $headers .= "Subject: =?UTF-8?B?" . base64_encode($subject) . "?=\r\n";
        $headers .= "MIME-Version: 1.0\r\n";
        $headers .= "Content-Type: multipart/alternative; boundary=\"{$boundary}\"\r\n";
        $headers .= "Date: " . date('r') . "\r\n";
        $headers .= "Message-ID: <" . time() . "." . md5($to_email) . "@{$this->smtp_host}>\r\n";
        $headers .= "\r\n";
        
        // Plain text part
        $body = "--{$boundary}\r\n";
        $body .= "Content-Type: text/plain; charset=UTF-8\r\n";
        $body .= "Content-Transfer-Encoding: base64\r\n\r\n";
        $body .= chunk_split(base64_encode($text_body)) . "\r\n";
        
        // HTML part
        $body .= "--{$boundary}\r\n";
        $body .= "Content-Type: text/html; charset=UTF-8\r\n";
        $body .= "Content-Transfer-Encoding: base64\r\n\r\n";
        $body .= chunk_split(base64_encode($html_body)) . "\r\n";
        
        $body .= "--{$boundary}--\r\n";
        
        // Send headers and body
        fputs($socket, $headers . $body);
        
        // End DATA
        if (!$this->smtpCommand($socket, "\r\n.\r\n", 250)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Send SMTP command and check response
     */
    private function smtpCommand($socket, $command, $expected_code) {
        fputs($socket, $command);
        $response = '';
        
        // Read full response (may be multi-line)
        while ($line = fgets($socket, 515)) {
            $response .= $line;
            // Check if this is the last line (has space after code)
            if (preg_match('/^(\d{3}) /', $line, $matches)) {
                break;
            }
        }
        
        $code = (int)substr($response, 0, 3);
        
        if ($code !== $expected_code) {
            error_log("SMTP command failed. Expected: $expected_code, Got: $code, Response: $response");
            return false;
        }
        
        return true;
    }
    
    /**
     * Get HTML template for verification email
     */
    private function getVerificationEmailTemplate($name, $verification_url, $token) {
        return <<<HTML
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Xác thực Email</title>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: #1976d2; color: white; padding: 20px; text-align: center; }
        .content { background: #f9f9f9; padding: 30px; }
        .button { display: inline-block; background: #1976d2; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
        .token { background: #e3f2fd; padding: 15px; margin: 15px 0; border-left: 4px solid #1976d2; font-family: monospace; font-size: 18px; letter-spacing: 2px; }
        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>NHD News</h1>
        </div>
        <div class="content">
            <h2>Xin chào $name,</h2>
            <p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>NHD News</strong>.</p>
            <p>Để hoàn tất đăng ký và có thể thích, bình luận bài viết, vui lòng nhập mã xác thực 6 số sau trong ứng dụng:</p>
            
            <div style="text-align: center;">
                <div class="token" style="font-size: 32px; font-weight: bold; letter-spacing: 8px; margin: 30px 0;">$token</div>
            </div>
            
            <p style="text-align: center; color: #666;"><small>Mã xác thực sẽ hết hạn sau 15 phút.</small></p>
            
            <hr style="margin: 30px 0; border: none; border-top: 1px solid #ddd;">
            
            <p style="color: #666; font-size: 14px;">
                Nếu bạn không tạo tài khoản này, vui lòng bỏ qua email này.
            </p>
        </div>
        <div class="footer">
            <p>&copy; 2025 NHD News. All rights reserved.</p>
        </div>
    </div>
</body>
</html>
HTML;
    }
    
    /**
     * Get HTML template for password reset email
     */
    private function getPasswordResetEmailTemplate($name, $reset_url, $token) {
        return <<<HTML
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt lại mật khẩu</title>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: #d32f2f; color: white; padding: 20px; text-align: center; }
        .content { background: #f9f9f9; padding: 30px; }
        .button { display: inline-block; background: #d32f2f; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
        .token { background: #ffebee; padding: 15px; margin: 15px 0; border-left: 4px solid #d32f2f; font-family: monospace; font-size: 18px; letter-spacing: 2px; }
        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>NHD News</h1>
        </div>
        <div class="content">
            <h2>Xin chào $name,</h2>
            <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>
            
            <div style="text-align: center;">
                <a href="$reset_url" class="button">Đặt lại mật khẩu</a>
            </div>
            
            <p>Hoặc bạn có thể nhập mã này trong ứng dụng:</p>
            <div class="token">$token</div>
            
            <p><small>Link này sẽ hết hạn sau 1 giờ.</small></p>
            
            <hr style="margin: 30px 0; border: none; border-top: 1px solid #ddd;">
            
            <p style="color: #666; font-size: 14px;">
                Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này. Tài khoản của bạn vẫn an toàn.
            </p>
        </div>
        <div class="footer">
            <p>&copy; 2025 NHD News. All rights reserved.</p>
        </div>
    </div>
</body>
</html>
HTML;
    }
}
?>

