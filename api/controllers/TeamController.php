<?php
/**
 * Team Controller - Xử lý các thao tác với đội bóng
 */
class TeamController extends Controller {
    private $teamModel;
    
    public function __construct() {
        parent::__construct();
        $this->teamModel = new Team($this->db);
    }
    
    /**
     * GET /teams
     */
    public function index() {
        $params = $this->getQueryParams();
        
        $page = isset($params['page']) ? intval($params['page']) : 1;
        $limit = isset($params['limit']) ? min(intval($params['limit']), 100) : 50;
        $category_id = isset($params['category_id']) ? intval($params['category_id']) : null;
        
        $cache_key = "teams_p{$page}_l{$limit}_c" . ($category_id ?? 'all');
        
        $response = ResponseHelper::cache($cache_key, function() use ($page, $limit, $category_id) {
            $stmt = $this->teamModel->getAll($page, $limit, $category_id);
            $total_count = $this->teamModel->getTotalCount($category_id);
            $total_pages = ceil($total_count / $limit);
            
            $teams_arr = [];
            
            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                $teams_arr[] = [
                    "team_id" => (int)$row['team_id'],
                    "team_name" => $row['team_name'],
                    "team_code" => $row['team_code'],
                    "logo_url" => $row['logo_url'],
                    "category_id" => $row['category_id'] ? (int)$row['category_id'] : null,
                    "category_name" => $row['category_name'] ?? null,
                    "country" => $row['country'],
                    "stadium" => $row['stadium'],
                    "founded_year" => $row['founded_year'] ? (int)$row['founded_year'] : null,
                    "is_active" => (bool)$row['is_active']
                ];
            }
            
            return [
                "teams" => $teams_arr,
                "totalCount" => (int)$total_count,
                "currentPage" => (int)$page,
                "totalPages" => (int)$total_pages
            ];
        }, 600);
        
        $this->json($response, 200, 600);
    }
    
    /**
     * GET /teams/:id
     */
    public function show($params) {
        $team_id = intval($params['id'] ?? 0);
        
        if ($team_id <= 0) {
            $this->error('ID đội bóng không hợp lệ', 400);
        }
        
        $cache_key = "team_" . $team_id;
        
        $team_data = ResponseHelper::cache($cache_key, function() use ($team_id) {
            $this->teamModel->team_id = $team_id;
            $data = $this->teamModel->getByIdComplete();
            
            if ($data) {
                $data['team_id'] = (int)$data['team_id'];
                $data['category_id'] = $data['category_id'] ? (int)$data['category_id'] : null;
                $data['founded_year'] = $data['founded_year'] ? (int)$data['founded_year'] : null;
                $data['is_active'] = (bool)$data['is_active'];
            }
            return $data;
        }, 600);
        
        if ($team_data) {
            $this->json($team_data, 200, 600);
        } else {
            $this->error("Không tìm thấy đội bóng", 404);
        }
    }
}
?>

