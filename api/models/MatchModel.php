<?php
class MatchModel {
    private $conn;
    private $table_name = "matches";

    public $match_id;
    public $home_team_id;
    public $away_team_id;
    public $category_id;
    public $tournament_name;
    public $match_date;
    public $venue;
    public $home_score;
    public $away_score;
    public $status;
    public $highlight_url;
    public $created_at;
    public $updated_at;

    public function __construct($db) {
        $this->conn = $db;
    }

    // Get all matches with team details and pagination
    public function getAll($page = 1, $limit = 20, $status = 'all', $category_id = null) {
        $offset = ($page - 1) * $limit;
        
        $query = "SELECT 
                    m.match_id, m.home_team_id, m.away_team_id, m.category_id,
                    m.tournament_name, m.match_date, m.venue, m.home_score, m.away_score,
                    m.status, m.highlight_url, m.created_at, m.updated_at,
                    ht.team_name as home_team_name, ht.team_code as home_team_code, ht.logo_url as home_logo_url,
                    at.team_name as away_team_name, at.team_code as away_team_code, at.logo_url as away_logo_url,
                    c.category_name
                  FROM " . $this->table_name . " m
                  LEFT JOIN teams ht ON m.home_team_id = ht.team_id
                  LEFT JOIN teams at ON m.away_team_id = at.team_id  
                  LEFT JOIN categories c ON m.category_id = c.category_id
                  WHERE 1=1";
        
        if ($category_id) {
            $query .= " AND m.category_id = :category_id";
        }
        
        if ($status && $status !== 'all') {
            $query .= " AND m.status = :status";
        }
        
        $query .= " ORDER BY m.match_date DESC LIMIT :limit OFFSET :offset";

        $stmt = $this->conn->prepare($query);
        
        if ($category_id) {
            $stmt->bindParam(':category_id', $category_id, PDO::PARAM_INT);
        }
        if ($status && $status !== 'all') {
            $stmt->bindParam(':status', $status);
        }
        
        $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
        $stmt->bindParam(':offset', $offset, PDO::PARAM_INT);
        $stmt->execute();
        return $stmt;
    }

    // Get total count for pagination
    public function getTotalCount($status = 'all', $category_id = null) {
        $query = "SELECT COUNT(*) as total FROM " . $this->table_name . " WHERE 1=1";
        
        if ($category_id) {
            $query .= " AND category_id = :category_id";
        }
        
        if ($status && $status !== 'all') {
            $query .= " AND status = :status";
        }

        $stmt = $this->conn->prepare($query);
        
        if ($category_id) {
            $stmt->bindParam(':category_id', $category_id, PDO::PARAM_INT);
        }
        if ($status && $status !== 'all') {
            $stmt->bindParam(':status', $status);
        }
        
        $stmt->execute();
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        return $row['total'];
    }

    // Get match by ID with team details
    public function getById() {
        $query = "SELECT 
                    m.match_id, m.home_team_id, m.away_team_id, m.category_id,
                    m.tournament_name, m.match_date, m.venue, m.home_score, m.away_score,
                    m.status, m.highlight_url, m.created_at, m.updated_at,
                    ht.team_name as home_team_name, ht.team_code as home_team_code, ht.logo_url as home_team_logo,
                    at.team_name as away_team_name, at.team_code as away_team_code, at.logo_url as away_team_logo,
                    c.category_name
                  FROM " . $this->table_name . " m
                  LEFT JOIN teams ht ON m.home_team_id = ht.team_id
                  LEFT JOIN teams at ON m.away_team_id = at.team_id
                  LEFT JOIN categories c ON m.category_id = c.category_id
                  WHERE m.match_id = ?";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(1, $this->match_id);
        $stmt->execute();
        return $stmt;
    }

    // Get match by ID with complete data
    public function getByIdComplete() {
        $query = "SELECT 
                    m.match_id, m.home_team_id, m.away_team_id, m.category_id,
                    m.tournament_name, m.match_date, m.venue, m.home_score, m.away_score,
                    m.status, m.highlight_url, m.created_at, m.updated_at,
                    ht.team_name as home_team_name, ht.team_code as home_team_code, ht.logo_url as home_team_logo,
                    at.team_name as away_team_name, at.team_code as away_team_code, at.logo_url as away_team_logo,
                    c.category_name
                  FROM " . $this->table_name . " m
                  LEFT JOIN teams ht ON m.home_team_id = ht.team_id
                  LEFT JOIN teams at ON m.away_team_id = at.team_id
                  LEFT JOIN categories c ON m.category_id = c.category_id
                  WHERE m.match_id = ?";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(1, $this->match_id);
        $stmt->execute();
        
        if ($stmt->rowCount() > 0) {
            return $stmt->fetch(PDO::FETCH_ASSOC);
        }
        
        return null;
    }

    // Get live matches
    public function getLive($limit = 50) {
        $query = "SELECT 
                    m.match_id, m.home_team_id, m.away_team_id, m.category_id,
                    m.tournament_name, m.match_date, m.venue, m.home_score, m.away_score,
                    m.status, m.highlight_url, m.created_at, m.updated_at,
                    ht.team_name as home_team_name, ht.team_code as home_team_code, ht.logo_url as home_logo_url,
                    at.team_name as away_team_name, at.team_code as away_team_code, at.logo_url as away_logo_url,
                    c.category_name
                  FROM " . $this->table_name . " m
                  LEFT JOIN teams ht ON m.home_team_id = ht.team_id
                  LEFT JOIN teams at ON m.away_team_id = at.team_id
                  LEFT JOIN categories c ON m.category_id = c.category_id
                  WHERE m.status = 'live'
                  ORDER BY m.match_date DESC 
                  LIMIT :limit";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
        $stmt->execute();
        return $stmt;
    }

    // Get upcoming matches
    public function getUpcoming($limit = 20) {
        $query = "SELECT 
                    m.match_id, m.home_team_id, m.away_team_id, m.category_id,
                    m.tournament_name, m.match_date, m.venue, m.home_score, m.away_score,
                    m.status, m.highlight_url, m.created_at, m.updated_at,
                    ht.team_name as home_team_name, ht.team_code as home_team_code, ht.logo_url as home_logo_url,
                    at.team_name as away_team_name, at.team_code as away_team_code, at.logo_url as away_logo_url,
                    c.category_name
                  FROM " . $this->table_name . " m
                  LEFT JOIN teams ht ON m.home_team_id = ht.team_id
                  LEFT JOIN teams at ON m.away_team_id = at.team_id
                  LEFT JOIN categories c ON m.category_id = c.category_id
                  WHERE m.match_date > NOW() AND m.status = 'scheduled'
                  ORDER BY m.match_date ASC 
                  LIMIT :limit";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
        $stmt->execute();
        return $stmt;
    }

    // Create sample matches if table is empty
    public function createSampleData() {
        // Check if matches exist
        $check_query = "SELECT COUNT(*) as count FROM " . $this->table_name;
        $stmt = $this->conn->prepare($check_query);
        $stmt->execute();
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if ($row['count'] == 0) {
            // First check if we have teams
            $team_check = "SELECT COUNT(*) as count FROM teams";
            $team_stmt = $this->conn->prepare($team_check);
            $team_stmt->execute();
            $team_row = $team_stmt->fetch(PDO::FETCH_ASSOC);
            
            if ($team_row['count'] > 0) {
                $sample_matches = [
                    [1, 2, 1, 'Premier League', '2025-10-05 15:00:00', 'Old Trafford', 2, 1, 'finished'],
                    [3, 4, 1, 'Premier League', '2025-10-06 17:30:00', 'Anfield', null, null, 'scheduled'],
                    [5, 1, 1, 'Premier League', '2025-10-07 14:00:00', 'Emirates Stadium', null, null, 'scheduled'],
                    [6, 7, 2, 'NBA', '2025-10-05 20:00:00', 'Crypto.com Arena', 108, 95, 'finished'],
                    [8, 6, 2, 'NBA', '2025-10-08 19:30:00', 'TD Garden', null, null, 'scheduled']
                ];

                $insert_query = "INSERT INTO " . $this->table_name . " 
                               (home_team_id, away_team_id, category_id, tournament_name, match_date, venue, home_score, away_score, status) 
                               VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                $stmt = $this->conn->prepare($insert_query);
                
                foreach ($sample_matches as $match) {
                    $stmt->execute($match);
                }
                
                return true;
            }
        }
        
        return false;
    }
}
?>
