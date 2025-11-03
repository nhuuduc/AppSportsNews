<?php
/**
 * Match Controller - Xử lý các thao tác với trận đấu
 */
class MatchController extends Controller {
    private $matchModel;
    
    public function __construct() {
        parent::__construct();
        $this->matchModel = new MatchModel($this->db);
    }
    
    /**
     * GET /matches
     */
    public function index() {
        $params = $this->getQueryParams();
        
        $page = isset($params['page']) ? intval($params['page']) : 1;
        $limit = isset($params['limit']) ? min(intval($params['limit']), 100) : 20;
        $status = $params['status'] ?? 'all'; // all, scheduled, live, finished
        $category_id = isset($params['category_id']) ? intval($params['category_id']) : null;
        
        $cache_key = "matches_p{$page}_l{$limit}_s{$status}_c" . ($category_id ?? 'all');
        $cache_time = ($status === 'live') ? 30 : 300; // Live matches: 30s cache
        
        $response = ResponseHelper::cache($cache_key, function() use ($page, $limit, $status, $category_id) {
            $stmt = $this->matchModel->getAll($page, $limit, $status, $category_id);
            
            $matches_arr = [];
            
            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                $matches_arr[] = [
                    "match_id" => (int)$row['match_id'],
                    "home_team_id" => (int)$row['home_team_id'],
                    "away_team_id" => (int)$row['away_team_id'],
                    "home_team" => [
                        "team_id" => (int)$row['home_team_id'],
                        "team_name" => $row['home_team_name'],
                        "logo_url" => $row['home_logo_url']
                    ],
                    "away_team" => [
                        "team_id" => (int)$row['away_team_id'],
                        "team_name" => $row['away_team_name'],
                        "logo_url" => $row['away_logo_url']
                    ],
                    "category_id" => (int)$row['category_id'],
                    "category_name" => $row['category_name'] ?? null,
                    "tournament_name" => $row['tournament_name'],
                    "match_date" => $row['match_date'],
                    "venue" => $row['venue'],
                    "home_score" => $row['home_score'] !== null ? (int)$row['home_score'] : null,
                    "away_score" => $row['away_score'] !== null ? (int)$row['away_score'] : null,
                    "status" => $row['status'],
                    "highlight_url" => $row['highlight_url'],
                    "created_at" => $row['created_at'] ?? null,
                    "updated_at" => $row['updated_at'] ?? null
                ];
            }
            
            return $matches_arr;
        }, $cache_time);
        
        $this->json($response, 200, $cache_time);
    }
    
    /**
     * GET /matches/:id
     */
    public function show($params) {
        $match_id = intval($params['id'] ?? 0);
        
        if ($match_id <= 0) {
            $this->error('ID trận đấu không hợp lệ', 400);
        }
        
        $cache_key = "match_" . $match_id;
        
        $match_data = ResponseHelper::cache($cache_key, function() use ($match_id) {
            $this->matchModel->match_id = $match_id;
            $data = $this->matchModel->getByIdComplete();
            
            if ($data) {
                $data['match_id'] = (int)$data['match_id'];
                $data['home_team_id'] = (int)$data['home_team_id'];
                $data['away_team_id'] = (int)$data['away_team_id'];
                $data['category_id'] = (int)$data['category_id'];
                $data['home_score'] = $data['home_score'] !== null ? (int)$data['home_score'] : null;
                $data['away_score'] = $data['away_score'] !== null ? (int)$data['away_score'] : null;
            }
            return $data;
        }, 120);
        
        if ($match_data) {
            $this->json($match_data, 200, 120);
        } else {
            $this->error("Không tìm thấy trận đấu", 404);
        }
    }
    
    /**
     * GET /matches/live
     */
    public function live() {
        $params = $this->getQueryParams();
        $limit = isset($params['limit']) ? min(intval($params['limit']), 50) : 20;
        
        $cache_key = "matches_live_" . $limit;
        
        $response = ResponseHelper::cache($cache_key, function() use ($limit) {
            $stmt = $this->matchModel->getLive($limit);
            $matches_arr = [];
            
            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                $matches_arr[] = [
                    "match_id" => (int)$row['match_id'],
                    "home_team_id" => (int)$row['home_team_id'],
                    "away_team_id" => (int)$row['away_team_id'],
                    "home_team" => [
                        "team_id" => (int)$row['home_team_id'],
                        "team_name" => $row['home_team_name'],
                        "logo_url" => $row['home_logo_url']
                    ],
                    "away_team" => [
                        "team_id" => (int)$row['away_team_id'],
                        "team_name" => $row['away_team_name'],
                        "logo_url" => $row['away_logo_url']
                    ],
                    "category_id" => (int)$row['category_id'],
                    "tournament_name" => $row['tournament_name'],
                    "match_date" => $row['match_date'],
                    "venue" => $row['venue'],
                    "home_score" => $row['home_score'] !== null ? (int)$row['home_score'] : null,
                    "away_score" => $row['away_score'] !== null ? (int)$row['away_score'] : null,
                    "status" => "live",
                    "created_at" => $row['created_at'] ?? null,
                    "updated_at" => $row['updated_at'] ?? null
                ];
            }
            
            return $matches_arr;
        }, 30); // Cache 30s for live matches
        
        $this->json($response, 200, 30);
    }
    
    /**
     * GET /matches/upcoming
     */
    public function upcoming() {
        $params = $this->getQueryParams();
        $limit = isset($params['limit']) ? min(intval($params['limit']), 50) : 20;
        
        $cache_key = "matches_upcoming_" . $limit;
        
        $response = ResponseHelper::cache($cache_key, function() use ($limit) {
            $stmt = $this->matchModel->getUpcoming($limit);
            $matches_arr = [];
            
            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                $matches_arr[] = [
                    "match_id" => (int)$row['match_id'],
                    "home_team_id" => (int)$row['home_team_id'],
                    "away_team_id" => (int)$row['away_team_id'],
                    "home_team" => [
                        "team_id" => (int)$row['home_team_id'],
                        "team_name" => $row['home_team_name'],
                        "logo_url" => $row['home_logo_url']
                    ],
                    "away_team" => [
                        "team_id" => (int)$row['away_team_id'],
                        "team_name" => $row['away_team_name'],
                        "logo_url" => $row['away_logo_url']
                    ],
                    "category_id" => (int)$row['category_id'],
                    "tournament_name" => $row['tournament_name'],
                    "match_date" => $row['match_date'],
                    "venue" => $row['venue'],
                    "status" => "scheduled",
                    "created_at" => $row['created_at'] ?? null,
                    "updated_at" => $row['updated_at'] ?? null
                ];
            }
            
            return $matches_arr;
        }, 300);
        
        $this->json($response, 200, 300);
    }
}
?>

