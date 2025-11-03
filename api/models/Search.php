<?php
class Search {
    private $conn;
    
    public function __construct($db) {
        $this->conn = $db;
    }
    
    /**
     * Search articles by keyword (title, summary)
     */
    public function searchArticles($keyword, $page = 1, $limit = 20) {
        $offset = ($page - 1) * $limit;
        $search_term = "%{$keyword}%";
        
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
                  AND (a.title LIKE :search_term OR a.summary LIKE :search_term2)
                  ORDER BY a.published_at DESC
                  LIMIT :limit OFFSET :offset";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(':search_term', $search_term, PDO::PARAM_STR);
        $stmt->bindParam(':search_term2', $search_term, PDO::PARAM_STR);
        $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
        $stmt->bindParam(':offset', $offset, PDO::PARAM_INT);
        $stmt->execute();

        $articles = $stmt->fetchAll(PDO::FETCH_ASSOC);

        // Convert boolean fields from database values (0/1) to actual booleans
        foreach ($articles as &$article) {
            $article['is_featured'] = (bool)$article['is_featured'];
            $article['is_breaking_news'] = (bool)$article['is_breaking_news'];
        }

        // Get total count
        $count_query = "SELECT COUNT(*) as total FROM articles a
                        WHERE a.status = 'published'
                        AND (a.title LIKE :search_term OR a.summary LIKE :search_term2)";
        $count_stmt = $this->conn->prepare($count_query);
        $count_stmt->bindParam(':search_term', $search_term, PDO::PARAM_STR);
        $count_stmt->bindParam(':search_term2', $search_term, PDO::PARAM_STR);
        $count_stmt->execute();
        $total = $count_stmt->fetch(PDO::FETCH_ASSOC)['total'];

        return [
            'data' => $articles,
            'total' => (int)$total
        ];
    }
    
    /**
     * Search matches by keyword (team names, tournament)
     */
    public function searchMatches($keyword, $limit = 20) {
        $search_term = "%{$keyword}%";
        
        $query = "SELECT 
                    m.match_id, m.home_team_id, m.away_team_id, m.category_id,
                    m.tournament_name, m.match_date, m.venue, m.home_score, m.away_score,
                    m.status, m.highlight_url, m.created_at, m.updated_at,
                    ht.team_id as home_team_id_full, ht.team_name as home_team_name, 
                    ht.team_code as home_team_code, ht.logo_url as home_team_logo,
                    ht.country as home_team_country, ht.stadium as home_team_stadium,
                    ht.founded_year as home_team_founded_year, ht.created_at as home_team_created_at,
                    at.team_id as away_team_id_full, at.team_name as away_team_name, 
                    at.team_code as away_team_code, at.logo_url as away_team_logo,
                    at.country as away_team_country, at.stadium as away_team_stadium,
                    at.founded_year as away_team_founded_year, at.created_at as away_team_created_at,
                    c.category_name
                  FROM matches m
                  LEFT JOIN teams ht ON m.home_team_id = ht.team_id
                  LEFT JOIN teams at ON m.away_team_id = at.team_id
                  LEFT JOIN categories c ON m.category_id = c.category_id
                  WHERE ht.team_name LIKE :search_term
                  OR at.team_name LIKE :search_term2
                  OR m.tournament_name LIKE :search_term3
                  ORDER BY m.match_date DESC
                  LIMIT :limit";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(':search_term', $search_term, PDO::PARAM_STR);
        $stmt->bindParam(':search_term2', $search_term, PDO::PARAM_STR);
        $stmt->bindParam(':search_term3', $search_term, PDO::PARAM_STR);
        $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
        $stmt->execute();
        
        $matches = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        // Transform to include nested team objects
        $transformed = [];
        foreach ($matches as $match) {
            $transformed[] = [
                'match_id' => (int)$match['match_id'],
                'home_team_id' => (int)$match['home_team_id'],
                'away_team_id' => (int)$match['away_team_id'],
                'category_id' => (int)$match['category_id'],
                'tournament_name' => $match['tournament_name'],
                'match_date' => $match['match_date'],
                'venue' => $match['venue'],
                'home_score' => $match['home_score'] !== null ? (int)$match['home_score'] : null,
                'away_score' => $match['away_score'] !== null ? (int)$match['away_score'] : null,
                'status' => $match['status'],
                'highlight_url' => $match['highlight_url'],
                'created_at' => $match['created_at'],
                'updated_at' => $match['updated_at'],
                'home_team' => [
                    'team_id' => (int)$match['home_team_id_full'],
                    'team_name' => $match['home_team_name'],
                    'team_code' => $match['home_team_code'],
                    'logo_url' => $match['home_team_logo'],
                    'country' => $match['home_team_country'],
                    'stadium' => $match['home_team_stadium'],
                    'founded_year' => $match['home_team_founded_year'] !== null ? (int)$match['home_team_founded_year'] : null,
                    'created_at' => $match['home_team_created_at']
                ],
                'away_team' => [
                    'team_id' => (int)$match['away_team_id_full'],
                    'team_name' => $match['away_team_name'],
                    'team_code' => $match['away_team_code'],
                    'logo_url' => $match['away_team_logo'],
                    'country' => $match['away_team_country'],
                    'stadium' => $match['away_team_stadium'],
                    'founded_year' => $match['away_team_founded_year'] !== null ? (int)$match['away_team_founded_year'] : null,
                    'created_at' => $match['away_team_created_at']
                ]
            ];
        }
        
        return $transformed;
    }
    
    /**
     * Search teams by keyword (team name, country)
     */
    public function searchTeams($keyword, $page = 1, $limit = 20) {
        $offset = ($page - 1) * $limit;
        $search_term = "%{$keyword}%";
        
        $query = "SELECT 
                    t.team_id, t.team_name, t.team_code, t.logo_url, 
                    t.category_id, t.country, t.stadium, t.founded_year,
                    t.description, t.is_active, t.created_at,
                    c.category_name
                  FROM teams t
                  LEFT JOIN categories c ON t.category_id = c.category_id
                  WHERE t.is_active = 1
                  AND (t.team_name LIKE :search_term OR t.country LIKE :search_term2)
                  ORDER BY t.team_name ASC
                  LIMIT :limit OFFSET :offset";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(':search_term', $search_term, PDO::PARAM_STR);
        $stmt->bindParam(':search_term2', $search_term, PDO::PARAM_STR);
        $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
        $stmt->bindParam(':offset', $offset, PDO::PARAM_INT);
        $stmt->execute();

        $teams = $stmt->fetchAll(PDO::FETCH_ASSOC);

        // Convert boolean fields from database values (0/1) to actual booleans
        foreach ($teams as &$team) {
            $team['is_active'] = (bool)$team['is_active'];
        }

        // Get total count
        $count_query = "SELECT COUNT(*) as total FROM teams t
                        WHERE t.is_active = 1
                        AND (t.team_name LIKE :search_term OR t.country LIKE :search_term2)";
        $count_stmt = $this->conn->prepare($count_query);
        $count_stmt->bindParam(':search_term', $search_term, PDO::PARAM_STR);
        $count_stmt->bindParam(':search_term2', $search_term, PDO::PARAM_STR);
        $count_stmt->execute();
        $total = $count_stmt->fetch(PDO::FETCH_ASSOC)['total'];

        return [
            'data' => $teams,
            'total' => (int)$total
        ];
    }
    
    /**
     * Search all types (articles, matches, teams)
     */
    public function searchAll($keyword, $limit = 20) {
        $results = [
            'articles' => $this->searchArticles($keyword, $limit),
            'matches' => $this->searchMatches($keyword, $limit),
            'teams' => $this->searchTeams($keyword, $limit)
        ];
        
        $total = count($results['articles']) + count($results['matches']) + count($results['teams']);
        $results['total'] = $total;
        
        return $results;
    }
}
?>
