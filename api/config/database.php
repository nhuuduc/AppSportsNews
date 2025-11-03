<?php
// Database configuration with optimizations
class Database {
    private $host = "localhost";
    private $db_name = "sportsnews";
    private $username = "sportsnews";
    private $password = "NaJzYEKbr2y8WEbD";
    private static $instance = null;
    public $conn;


    // Singleton pattern for connection reuse
    public static function getInstance() {
        if (self::$instance === null) {
            self::$instance = new self();
        }
        return self::$instance;
    }

    // Get database connection with optimizations
    public function getConnection() {
        if ($this->conn !== null) {
            return $this->conn;
        }
        
        try {
            $options = [
                PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
                PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
                PDO::ATTR_EMULATE_PREPARES => false,
                PDO::ATTR_PERSISTENT => true, // Persistent connections
                PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci",
                PDO::MYSQL_ATTR_USE_BUFFERED_QUERY => true,
                PDO::ATTR_STRINGIFY_FETCHES => false // Return native types
            ];
            
            $this->conn = new PDO(
                "mysql:host=" . $this->host . ";dbname=" . $this->db_name . ";charset=utf8mb4",
                $this->username,
                $this->password,
                $options
            );
            
        } catch(PDOException $exception) {
            http_response_code(500);
            die(json_encode(["error" => "Database connection failed", "message" => $exception->getMessage()]));
        }
        
        return $this->conn;
    }
}
?>
