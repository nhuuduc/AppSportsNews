<?php
/**
 * Search Controller - Xử lý tìm kiếm
 */
class SearchController extends Controller {
    private $searchModel;
    
    public function __construct() {
        parent::__construct();
        $this->searchModel = new Search($this->db);
    }
    
    /**
     * GET /search
     */
    public function search() {
        $params = $this->getQueryParams();
        
        $query = trim($params['q'] ?? $params['query'] ?? '');
        $type = $params['type'] ?? 'articles'; // articles, teams, all
        $page = isset($params['page']) ? intval($params['page']) : 1;
        $limit = isset($params['limit']) ? min(intval($params['limit']), 50) : 20;
        
        if (empty($query)) {
            $this->error('Vui lòng nhập từ khóa tìm kiếm', 400);
        }
        
        if (strlen($query) < 2) {
            $this->error('Từ khóa tìm kiếm phải có ít nhất 2 ký tự', 400);
        }
        
        $cache_key = "search_" . md5($query . $type . $page . $limit);
        
        $response = ResponseHelper::cache($cache_key, function() use ($query, $type, $page, $limit) {
            $results = [];
            
            if ($type === 'articles' || $type === 'all') {
                $articles = $this->searchModel->searchArticles($query, $page, $limit);
                $results['articles'] = $articles['data'];
                $results['articles_total'] = $articles['total'];
            }
            
            if ($type === 'teams' || $type === 'all') {
                $teams = $this->searchModel->searchTeams($query, $page, $limit);
                $results['teams'] = $teams['data'];
                $results['teams_total'] = $teams['total'];
            }
            
            $results['query'] = $query;
            $results['type'] = $type;
            $results['page'] = $page;
            $results['limit'] = $limit;
            
            return $results;
        }, 180);
        
        // Add is_liked field for articles if user is authenticated
        $user = $this->getOptionalAuthUser();
        if ($user && isset($response['articles'])) {
            foreach ($response['articles'] as &$article) {
                $article['is_liked'] = $this->checkArticleLiked($article['article_id'], $user['user_id']);
            }
        } else if (isset($response['articles'])) {
            foreach ($response['articles'] as &$article) {
                $article['is_liked'] = false;
            }
        }
        
        $this->json($response, 200, 0); // No cache for personalized data
    }
}
?>

