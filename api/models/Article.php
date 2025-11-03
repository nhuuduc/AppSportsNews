<?php
class Article {
    private $conn;
    private $table_name = "articles";

    public $article_id;
    public $title;
    public $slug;
    public $summary;
    public $content;
    public $thumbnail_url;
    public $category_id;
    public $author_id;
    public $view_count;
    public $like_count;
    public $comment_count;
    public $is_featured;
    public $is_breaking_news;
    public $status;
    public $published_at;
    public $created_at;
    public $updated_at;

    public function __construct($db) {
        $this->conn = $db;
    }

    // Get all published articles with pagination
    public function getAll($page = 1, $limit = 20, $category_id = null) {
        $offset = ($page - 1) * $limit;
        
        $where_clause = "WHERE a.status = 'published'";
        if ($category_id) {
            $where_clause .= " AND a.category_id = :category_id";
        }

        $query = "SELECT 
                    a.article_id, a.title, a.slug, a.summary, a.content,
                    a.thumbnail_url, a.category_id, a.author_id,
                    COALESCE(v.view_count, 0) as view_count,
                    COALESCE(v.like_count, 0) as like_count,
                    COALESCE(cm.comment_count, 0) as comment_count,
                    a.is_featured, a.is_breaking_news,
                    a.status, a.published_at, a.created_at, a.updated_at,
                    c.category_name, u.full_name as author_name
                  FROM " . $this->table_name . " a
                  LEFT JOIN categories c ON a.category_id = c.category_id
                  LEFT JOIN users u ON a.author_id = u.user_id
                  LEFT JOIN article_views v ON a.article_id = v.article_id
                  LEFT JOIN (
                      SELECT article_id, COUNT(*) as comment_count 
                      FROM comments 
                      WHERE is_active = 1 
                      GROUP BY article_id
                  ) cm ON a.article_id = cm.article_id
                  " . $where_clause . "
                  ORDER BY a.published_at DESC
                  LIMIT :limit OFFSET :offset";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
        $stmt->bindParam(':offset', $offset, PDO::PARAM_INT);
        
        if ($category_id) {
            $stmt->bindParam(':category_id', $category_id, PDO::PARAM_INT);
        }
        
        $stmt->execute();
        return $stmt;
    }

    // Get total count for pagination
    public function getTotalCount($category_id = null) {
        $where_clause = "WHERE status = 'published'";
        if ($category_id) {
            $where_clause .= " AND category_id = :category_id";
        }

        $query = "SELECT COUNT(*) as total FROM " . $this->table_name . " " . $where_clause;
        $stmt = $this->conn->prepare($query);
        
        if ($category_id) {
            $stmt->bindParam(':category_id', $category_id, PDO::PARAM_INT);
        }
        
        $stmt->execute();
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        return $row['total'];
    }

    // Get article by ID
    public function getById() {
        $query = "SELECT 
                    a.article_id, a.title, a.slug, a.summary, a.content,
                    a.thumbnail_url, a.category_id, a.author_id,
                    COALESCE(v.view_count, 0) as view_count,
                    COALESCE(v.like_count, 0) as like_count,
                    COALESCE(cm.comment_count, 0) as comment_count,
                    a.is_featured, a.is_breaking_news,
                    a.status, a.published_at, a.created_at, a.updated_at,
                    c.category_name, u.full_name as author_name
                  FROM " . $this->table_name . " a
                  LEFT JOIN categories c ON a.category_id = c.category_id
                  LEFT JOIN users u ON a.author_id = u.user_id
                  LEFT JOIN article_views v ON a.article_id = v.article_id
                  LEFT JOIN (
                      SELECT article_id, COUNT(*) as comment_count 
                      FROM comments 
                      WHERE is_active = 1 
                      GROUP BY article_id
                  ) cm ON a.article_id = cm.article_id
                  WHERE a.article_id = ? AND a.status = 'published'";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(1, $this->article_id);
        $stmt->execute();
        return $stmt;
    }

    // Get article tags
    public function getArticleTags() {
        $query = "SELECT t.tag_id, t.tag_name, t.tag_slug
                  FROM tags t
                  INNER JOIN article_tags at ON t.tag_id = at.tag_id
                  WHERE at.article_id = ?
                  ORDER BY t.tag_name ASC";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(1, $this->article_id);
        $stmt->execute();
        return $stmt;
    }

    // Get article images
    public function getArticleImages() {
        $query = "SELECT image_id, image_url, caption, display_order
                  FROM article_images
                  WHERE article_id = ?
                  ORDER BY display_order ASC, image_id ASC";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(1, $this->article_id);
        $stmt->execute();
        return $stmt;
    }

    // Get article with complete data (tags and images)
    public function getByIdComplete() {
        // Get basic article data
        $stmt = $this->getById();
        
        if ($stmt->rowCount() > 0) {
            $article = $stmt->fetch(PDO::FETCH_ASSOC);
            
            // Get tags
            $tags_stmt = $this->getArticleTags();
            $tags = [];
            while ($tag_row = $tags_stmt->fetch(PDO::FETCH_ASSOC)) {
                $tags[] = $tag_row;
            }
            $article['tags'] = $tags;
            
            // Get images
            $images_stmt = $this->getArticleImages();
            $images = [];
            while ($image_row = $images_stmt->fetch(PDO::FETCH_ASSOC)) {
                $images[] = $image_row;
            }
            $article['images'] = $images;
            
            return $article;
        }
        
        return null;
    }

    // Get featured articles
    public function getFeatured($limit = 5) {
        $query = "SELECT 
                    a.article_id, a.title, a.slug, a.summary, a.content,
                    a.thumbnail_url, a.category_id, a.author_id,
                    COALESCE(v.view_count, 0) as view_count,
                    COALESCE(v.like_count, 0) as like_count,
                    COALESCE(cm.comment_count, 0) as comment_count,
                    a.is_featured, a.is_breaking_news,
                    a.status, a.published_at, a.created_at, a.updated_at,
                    c.category_name, u.full_name as author_name
                  FROM " . $this->table_name . " a
                  LEFT JOIN categories c ON a.category_id = c.category_id
                  LEFT JOIN users u ON a.author_id = u.user_id
                  LEFT JOIN article_views v ON a.article_id = v.article_id
                  LEFT JOIN (
                      SELECT article_id, COUNT(*) as comment_count 
                      FROM comments 
                      WHERE is_active = 1 
                      GROUP BY article_id
                  ) cm ON a.article_id = cm.article_id
                  WHERE a.status = 'published' AND a.is_featured = 1
                  ORDER BY a.published_at DESC
                  LIMIT :limit";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
        $stmt->execute();
        return $stmt;
    }

    // Get breaking news
    public function getBreakingNews($limit = 3) {
        $query = "SELECT 
                    a.article_id, a.title, a.slug, a.summary, a.content,
                    a.thumbnail_url, a.category_id, a.author_id,
                    COALESCE(v.view_count, 0) as view_count,
                    COALESCE(v.like_count, 0) as like_count,
                    COALESCE(cm.comment_count, 0) as comment_count,
                    a.is_featured, a.is_breaking_news,
                    a.status, a.published_at, a.created_at, a.updated_at,
                    c.category_name, u.full_name as author_name
                  FROM " . $this->table_name . " a
                  LEFT JOIN categories c ON a.category_id = c.category_id
                  LEFT JOIN users u ON a.author_id = u.user_id
                  LEFT JOIN article_views v ON a.article_id = v.article_id
                  LEFT JOIN (
                      SELECT article_id, COUNT(*) as comment_count 
                      FROM comments 
                      WHERE is_active = 1 
                      GROUP BY article_id
                  ) cm ON a.article_id = cm.article_id
                  WHERE a.status = 'published' AND a.is_breaking_news = 1
                  ORDER BY a.published_at DESC
                  LIMIT :limit";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
        $stmt->execute();
        return $stmt;
    }

    // Get trending articles (using stored procedure if exists, otherwise fallback)
    public function getTrending($limit = 10) {
        try {
            // Try to use stored procedure first
            $query = "CALL GetTrendingArticles(:limit)";
            $stmt = $this->conn->prepare($query);
            $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
            $stmt->execute();
            return $stmt;
        } catch (Exception $e) {
            // Fallback to manual calculation
            $query = "SELECT 
                        a.article_id, a.title, a.slug, a.summary, a.content,
                        a.thumbnail_url, a.category_id, a.author_id,
                        COALESCE(v.view_count, 0) as view_count,
                        COALESCE(v.like_count, 0) as like_count,
                        COALESCE(cm.comment_count, 0) as comment_count,
                        a.is_featured, a.is_breaking_news,
                        a.status, a.published_at, a.created_at, a.updated_at,
                        c.category_name, u.full_name as author_name,
                        (COALESCE(v.view_count, 0) * 0.5 + COALESCE(v.like_count, 0) * 2 + COALESCE(cm.comment_count, 0) * 1.5) as trending_score
                      FROM " . $this->table_name . " a
                      LEFT JOIN categories c ON a.category_id = c.category_id
                      LEFT JOIN users u ON a.author_id = u.user_id
                      LEFT JOIN article_views v ON a.article_id = v.article_id
                      LEFT JOIN (
                          SELECT article_id, COUNT(*) as comment_count 
                          FROM comments 
                          WHERE is_active = 1 
                          GROUP BY article_id
                      ) cm ON a.article_id = cm.article_id
                      WHERE a.status = 'published' 
                      AND a.published_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
                      ORDER BY trending_score DESC
                      LIMIT :limit";

            $stmt = $this->conn->prepare($query);
            $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
            $stmt->execute();
            return $stmt;
        }
    }
}
?>
