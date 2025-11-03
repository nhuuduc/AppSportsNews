<?php
class Video {
    private $conn;
    private $table_name = "videos";

    public $video_id;
    public $title;
    public $description;
    public $video_url;
    public $thumbnail_url;
    public $duration;
    public $category_id;
    public $match_id;
    public $view_count;
    public $created_at;

    public function __construct($db) {
        $this->conn = $db;
    }

    // Get all videos with pagination
    public function getAll($page = 1, $limit = 20, $category_id = null, $match_id = null) {
        $offset = ($page - 1) * $limit;
        
        $query = "SELECT 
                    v.video_id, v.title, v.description, v.video_url, v.thumbnail_url,
                    v.duration, v.category_id, v.match_id, v.view_count, v.created_at,
                    c.category_name,
                    m.tournament_name, 
                    ht.team_name as home_team_name, at.team_name as away_team_name
                  FROM " . $this->table_name . " v
                  LEFT JOIN categories c ON v.category_id = c.category_id
                  LEFT JOIN matches m ON v.match_id = m.match_id
                  LEFT JOIN teams ht ON m.home_team_id = ht.team_id
                  LEFT JOIN teams at ON m.away_team_id = at.team_id
                  WHERE 1=1";
        
        if ($category_id) {
            $query .= " AND v.category_id = :category_id";
        }
        
        if ($match_id) {
            $query .= " AND v.match_id = :match_id";
        }
        
        $query .= " ORDER BY v.created_at DESC LIMIT :limit OFFSET :offset";

        $stmt = $this->conn->prepare($query);
        
        if ($category_id) {
            $stmt->bindParam(':category_id', $category_id, PDO::PARAM_INT);
        }
        
        if ($match_id) {
            $stmt->bindParam(':match_id', $match_id, PDO::PARAM_INT);
        }
        
        $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
        $stmt->bindParam(':offset', $offset, PDO::PARAM_INT);
        $stmt->execute();
        return $stmt;
    }

    // Get total count for pagination
    public function getTotalCount($category_id = null) {
        $query = "SELECT COUNT(*) as total FROM " . $this->table_name . " WHERE 1=1";
        
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

    // Get video by ID
    public function getById() {
        $query = "SELECT 
                    v.video_id, v.title, v.description, v.video_url, v.thumbnail_url,
                    v.duration, v.category_id, v.match_id, v.view_count, v.created_at,
                    c.category_name,
                    m.tournament_name, m.match_date, m.venue,
                    ht.team_name as home_team_name, ht.logo_url as home_team_logo,
                    at.team_name as away_team_name, at.logo_url as away_team_logo
                  FROM " . $this->table_name . " v
                  LEFT JOIN categories c ON v.category_id = c.category_id
                  LEFT JOIN matches m ON v.match_id = m.match_id
                  LEFT JOIN teams ht ON m.home_team_id = ht.team_id
                  LEFT JOIN teams at ON m.away_team_id = at.team_id
                  WHERE v.video_id = ?";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(1, $this->video_id);
        $stmt->execute();
        
        if ($stmt->rowCount() > 0) {
            return $stmt->fetch(PDO::FETCH_ASSOC);
        }
        
        return null;
    }

    // Get highlight videos
    public function getHighlights($limit = 10) {
        $query = "SELECT 
                    v.video_id, v.title, v.description, v.video_url, v.thumbnail_url,
                    v.duration, v.category_id, v.match_id, v.view_count, v.created_at,
                    c.category_name,
                    m.tournament_name, m.match_date,
                    ht.team_name as home_team_name, ht.logo_url as home_team_logo,
                    at.team_name as away_team_name, at.logo_url as away_team_logo
                  FROM " . $this->table_name . " v
                  LEFT JOIN categories c ON v.category_id = c.category_id
                  LEFT JOIN matches m ON v.match_id = m.match_id
                  LEFT JOIN teams ht ON m.home_team_id = ht.team_id
                  LEFT JOIN teams at ON m.away_team_id = at.team_id
                  WHERE v.match_id IS NOT NULL
                  ORDER BY v.view_count DESC, v.created_at DESC
                  LIMIT :limit";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
        $stmt->execute();
        return $stmt;
    }

    // Update view count
    public function updateViewCount() {
        $query = "UPDATE " . $this->table_name . " 
                  SET view_count = view_count + 1 
                  WHERE video_id = ?";
        
        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(1, $this->video_id);
        
        if ($stmt->execute()) {
            return true;
        }
        return false;
    }

    // Create sample videos if table is empty
    public function createSampleData() {
        // Check if videos exist
        $check_query = "SELECT COUNT(*) as count FROM " . $this->table_name;
        $stmt = $this->conn->prepare($check_query);
        $stmt->execute();
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if ($row['count'] == 0) {
            $sample_videos = [
                ['Manchester United vs Manchester City Highlights', 'Best moments from the Manchester Derby', 'https://example.com/video1.mp4', 'https://example.com/thumb1.jpg', 180, 1, 1, 1250],
                ['Liverpool Training Session', 'Behind the scenes training footage', 'https://example.com/video2.mp4', 'https://example.com/thumb2.jpg', 300, 1, null, 890],
                ['Lakers vs Warriors Game Highlights', 'NBA game highlights and best plays', 'https://example.com/video3.mp4', 'https://example.com/thumb3.jpg', 240, 2, 4, 2100],
                ['Basketball Skills Tutorial', 'Learn professional basketball techniques', 'https://example.com/video4.mp4', 'https://example.com/thumb4.jpg', 420, 2, null, 1580]
            ];

            $insert_query = "INSERT INTO " . $this->table_name . " 
                           (title, description, video_url, thumbnail_url, duration, category_id, match_id, view_count) 
                           VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            $stmt = $this->conn->prepare($insert_query);
            
            foreach ($sample_videos as $video) {
                $stmt->execute($video);
            }
            
            return true;
        }
        
        return false;
    }
}
?>
