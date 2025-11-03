<?php
class Tag {
    private $conn;
    private $table_name = "tags";

    public $tag_id;
    public $tag_name;
    public $tag_slug;
    public $created_at;

    public function __construct($db) {
        $this->conn = $db;
    }

    // Get all tags
    public function getAll($limit = 100) {
        $query = "SELECT tag_id, tag_name, tag_slug, created_at,
                         (SELECT COUNT(*) FROM article_tags WHERE tag_id = t.tag_id) as article_count
                  FROM " . $this->table_name . " t
                  ORDER BY tag_name ASC
                  LIMIT :limit";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
        $stmt->execute();
        return $stmt;
    }

    // Get tag by ID
    public function getById() {
        $query = "SELECT tag_id, tag_name, tag_slug, created_at,
                         (SELECT COUNT(*) FROM article_tags WHERE tag_id = t.tag_id) as article_count
                  FROM " . $this->table_name . " t
                  WHERE tag_id = ?";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(1, $this->tag_id);
        $stmt->execute();
        
        if ($stmt->rowCount() > 0) {
            return $stmt->fetch(PDO::FETCH_ASSOC);
        }
        
        return null;
    }

    // Get popular tags (most used)
    public function getPopular($limit = 10) {
        $query = "SELECT t.tag_id, t.tag_name, t.tag_slug, t.created_at,
                         COUNT(at.article_id) as article_count
                  FROM " . $this->table_name . " t
                  LEFT JOIN article_tags at ON t.tag_id = at.tag_id
                  GROUP BY t.tag_id, t.tag_name, t.tag_slug, t.created_at
                  HAVING article_count > 0
                  ORDER BY article_count DESC, t.tag_name ASC
                  LIMIT :limit";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
        $stmt->execute();
        return $stmt;
    }

    // Get articles by tag
    public function getArticlesByTag($tag_id, $page = 1, $limit = 20) {
        $offset = ($page - 1) * $limit;
        
        $query = "SELECT 
                    a.article_id, a.title, a.slug, a.summary, a.content,
                    a.thumbnail_url, a.category_id, a.author_id,
                    COALESCE(v.view_count, 0) as view_count,
                    COALESCE(v.like_count, 0) as like_count,
                    COALESCE(cm.comment_count, 0) as comment_count,
                    a.is_featured, a.is_breaking_news,
                    a.status, a.published_at, a.created_at, a.updated_at,
                    c.category_name, u.full_name as author_name
                  FROM articles a
                  INNER JOIN article_tags at ON a.article_id = at.article_id
                  LEFT JOIN categories c ON a.category_id = c.category_id
                  LEFT JOIN users u ON a.author_id = u.user_id
                  LEFT JOIN article_views v ON a.article_id = v.article_id
                  LEFT JOIN (
                      SELECT article_id, COUNT(*) as comment_count 
                      FROM comments 
                      WHERE is_active = 1 
                      GROUP BY article_id
                  ) cm ON a.article_id = cm.article_id
                  WHERE at.tag_id = :tag_id AND a.status = 'published'
                  ORDER BY a.published_at DESC
                  LIMIT :limit OFFSET :offset";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(':tag_id', $tag_id, PDO::PARAM_INT);
        $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
        $stmt->bindParam(':offset', $offset, PDO::PARAM_INT);
        $stmt->execute();
        
        $articles = [];
        while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
            $row['article_id'] = (int)$row['article_id'];
            $row['category_id'] = (int)$row['category_id'];
            $row['author_id'] = (int)$row['author_id'];
            $row['view_count'] = (int)$row['view_count'];
            $row['like_count'] = (int)$row['like_count'];
            $row['comment_count'] = (int)$row['comment_count'];
            $row['is_featured'] = (bool)$row['is_featured'];
            $row['is_breaking_news'] = (bool)$row['is_breaking_news'];
            $articles[] = $row;
        }
        
        return $articles;
    }

    // Get total article count for a tag (for pagination)
    public function getArticlesCount($tag_id) {
        $query = "SELECT COUNT(*) as total 
                  FROM articles a
                  INNER JOIN article_tags at ON a.article_id = at.article_id
                  WHERE at.tag_id = :tag_id AND a.status = 'published'";
        
        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(':tag_id', $tag_id, PDO::PARAM_INT);
        $stmt->execute();
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        return $row['total'];
    }

    // Create slug from tag name
    private function createSlug($text) {
        // Convert to lowercase
        $text = strtolower($text);
        // Remove special characters except letters, numbers, and spaces
        $text = preg_replace('/[^a-z0-9\s-]/', '', $text);
        // Replace spaces and multiple hyphens with single hyphen
        $text = preg_replace('/[\s-]+/', '-', $text);
        // Remove leading/trailing hyphens
        return trim($text, '-');
    }

    // Create sample tags if table is empty
    public function createSampleData() {
        // Check if tags exist
        $check_query = "SELECT COUNT(*) as count FROM " . $this->table_name;
        $stmt = $this->conn->prepare($check_query);
        $stmt->execute();
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if ($row['count'] == 0) {
            $sample_tags = [
                'Football', 'Soccer', 'Basketball', 'Premier League', 'NBA', 
                'Champions League', 'World Cup', 'Olympics', 'Transfer News', 
                'Match Report', 'Player Profile', 'Team News', 'Injury Update',
                'Breaking News', 'Analysis', 'Interview', 'Highlights', 'Goals',
                'Statistics', 'Prediction'
            ];

            $insert_query = "INSERT INTO " . $this->table_name . " 
                           (tag_name, tag_slug) 
                           VALUES (?, ?)";
            
            $stmt = $this->conn->prepare($insert_query);
            
            foreach ($sample_tags as $tag_name) {
                $tag_slug = $this->createSlug($tag_name);
                $stmt->execute([$tag_name, $tag_slug]);
            }
            
            return true;
        }
        
        return false;
    }
}
?>
