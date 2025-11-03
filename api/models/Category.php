<?php
class Category {
    private $conn;
    private $table_name = "categories";

    public $category_id;
    public $category_name;
    public $category_slug;
    public $description;
    public $icon_url;
    public $parent_id;
    public $display_order;
    public $is_active;
    public $created_at;

    public function __construct($db) {
        $this->conn = $db;
    }

    // Get all active categories
    public function getAll() {
        $query = "SELECT 
                    category_id, category_name, category_slug, 
                    description, icon_url, parent_id, 
                    display_order, is_active, created_at
                  FROM " . $this->table_name . " 
                  WHERE is_active = 1 
                  ORDER BY display_order ASC, category_name ASC";

        $stmt = $this->conn->prepare($query);
        $stmt->execute();
        return $stmt;
    }

    // Get category by ID
    public function getById() {
        $query = "SELECT 
                    category_id, category_name, category_slug, 
                    description, icon_url, parent_id, 
                    display_order, is_active, created_at
                  FROM " . $this->table_name . " 
                  WHERE category_id = ? AND is_active = 1";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(1, $this->category_id);
        $stmt->execute();
        return $stmt;
    }

    // Create sample categories if table is empty
    public function createSampleData() {
        // Check if categories exist
        $check_query = "SELECT COUNT(*) as count FROM " . $this->table_name;
        $stmt = $this->conn->prepare($check_query);
        $stmt->execute();
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if ($row['count'] == 0) {
            $sample_categories = [
                ['Bóng đá', 'bong-da', 'Tin tức bóng đá trong nước và quốc tế', 1],
                ['Bóng rổ', 'bong-ro', 'Tin tức bóng rổ NBA, VBA và các giải đấu khác', 2],
                ['Tennis', 'tennis', 'Tin tức tennis Grand Slam và các giải đấu', 3],
                ['Bóng chuyền', 'bong-chuyen', 'Tin tức bóng chuyền trong nước và quốc tế', 4],
                ['Thể thao điện tử', 'the-thao-dien-tu', 'Tin tức Esports và game thể thao', 5],
                ['Đua xe', 'dua-xe', 'Tin tức F1, MotoGP và các giải đua xe', 6],
                ['Bơi lội', 'boi-loi', 'Tin tức bơi lội và thể thao dưới nước', 7],
                ['Cầu lông', 'cau-long', 'Tin tức cầu lông trong nước và quốc tế', 8]
            ];

            $insert_query = "INSERT INTO " . $this->table_name . " 
                           (category_name, category_slug, description, display_order, is_active) 
                           VALUES (?, ?, ?, ?, 1)";
            
            $stmt = $this->conn->prepare($insert_query);
            
            foreach ($sample_categories as $category) {
                $stmt->execute($category);
            }
            
            return true;
        }
        
        return false;
    }
}
?>
