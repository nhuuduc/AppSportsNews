<?php
class Team {
    private $conn;
    private $table_name = "teams";

    public $team_id;
    public $team_name;
    public $team_code;
    public $logo_url;
    public $category_id;
    public $country;
    public $stadium;
    public $founded_year;
    public $description;
    public $is_active;
    public $created_at;

    public function __construct($db) {
        $this->conn = $db;
    }

    // Get all active teams with pagination
    public function getAll($page = 1, $limit = 50, $category_id = null) {
        $offset = ($page - 1) * $limit;
        
        $query = "SELECT 
                    t.team_id, t.team_name, t.team_code, t.logo_url, 
                    t.category_id, t.country, t.stadium, t.founded_year,
                    t.description, t.is_active, t.created_at,
                    c.category_name
                  FROM " . $this->table_name . " t
                  LEFT JOIN categories c ON t.category_id = c.category_id
                  WHERE t.is_active = 1";
        
        if ($category_id) {
            $query .= " AND t.category_id = :category_id";
        }
        
        $query .= " ORDER BY t.team_name ASC LIMIT :limit OFFSET :offset";

        $stmt = $this->conn->prepare($query);
        
        if ($category_id) {
            $stmt->bindParam(':category_id', $category_id, PDO::PARAM_INT);
        }
        
        $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
        $stmt->bindParam(':offset', $offset, PDO::PARAM_INT);
        $stmt->execute();
        return $stmt;
    }

    // Get total count for pagination
    public function getTotalCount($category_id = null) {
        $query = "SELECT COUNT(*) as total FROM " . $this->table_name . " WHERE is_active = 1";
        
        if ($category_id) {
            $query .= " AND category_id = :category_id";
        }

        $stmt = $this->conn->prepare($query);
        
        if ($category_id) {
            $stmt->bindParam(':category_id', $category_id, PDO::PARAM_INT);
        }
        
        $stmt->execute();
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        return $row['total'];
    }

    // Get team by ID
    public function getById() {
        $query = "SELECT 
                    team_id, team_name, team_code, logo_url, 
                    category_id, country, stadium, founded_year,
                    description, is_active, created_at
                  FROM " . $this->table_name . " 
                  WHERE team_id = ? AND is_active = 1";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(1, $this->team_id);
        $stmt->execute();
        return $stmt;
    }

    // Get team by ID with complete data
    public function getByIdComplete() {
        $query = "SELECT 
                    t.team_id, t.team_name, t.team_code, t.logo_url, 
                    t.category_id, t.country, t.stadium, t.founded_year,
                    t.description, t.is_active, t.created_at,
                    c.category_name
                  FROM " . $this->table_name . " t
                  LEFT JOIN categories c ON t.category_id = c.category_id
                  WHERE t.team_id = ? AND t.is_active = 1";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(1, $this->team_id);
        $stmt->execute();
        
        if ($stmt->rowCount() > 0) {
            return $stmt->fetch(PDO::FETCH_ASSOC);
        }
        
        return null;
    }

    // Create sample teams if table is empty
    public function createSampleData() {
        // Check if teams exist
        $check_query = "SELECT COUNT(*) as count FROM " . $this->table_name;
        $stmt = $this->conn->prepare($check_query);
        $stmt->execute();
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if ($row['count'] == 0) {
            $sample_teams = [
                ['Manchester United', 'MAN', null, 1, 'England', 'Old Trafford', 1878, 'Manchester United Football Club'],
                ['Manchester City', 'MCI', null, 1, 'England', 'Etihad Stadium', 1880, 'Manchester City Football Club'],
                ['Liverpool', 'LIV', null, 1, 'England', 'Anfield', 1892, 'Liverpool Football Club'],
                ['Chelsea', 'CHE', null, 1, 'England', 'Stamford Bridge', 1905, 'Chelsea Football Club'],
                ['Arsenal', 'ARS', null, 1, 'England', 'Emirates Stadium', 1886, 'Arsenal Football Club'],
                ['Los Angeles Lakers', 'LAL', null, 2, 'USA', 'Crypto.com Arena', 1947, 'Los Angeles Lakers Basketball Team'],
                ['Golden State Warriors', 'GSW', null, 2, 'USA', 'Chase Center', 1946, 'Golden State Warriors Basketball Team'],
                ['Boston Celtics', 'BOS', null, 2, 'USA', 'TD Garden', 1946, 'Boston Celtics Basketball Team']
            ];

            $insert_query = "INSERT INTO " . $this->table_name . " 
                           (team_name, team_code, logo_url, category_id, country, stadium, founded_year, description, is_active) 
                           VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)";
            
            $stmt = $this->conn->prepare($insert_query);
            
            foreach ($sample_teams as $team) {
                $stmt->execute($team);
            }
            
            return true;
        }
        
        return false;
    }
}
?>
