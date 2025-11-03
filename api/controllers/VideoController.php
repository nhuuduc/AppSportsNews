<?php
/**
 * Video Controller - Xử lý các thao tác với video
 */
class VideoController extends Controller {
    private $videoModel;
    
    public function __construct() {
        parent::__construct();
        $this->videoModel = new Video($this->db);
    }
    
    /**
     * GET /videos
     */
    public function index() {
        $params = $this->getQueryParams();
        
        $page = isset($params['page']) ? intval($params['page']) : 1;
        $limit = isset($params['limit']) ? min(intval($params['limit']), 50) : 20;
        $category_id = isset($params['category_id']) ? intval($params['category_id']) : null;
        $match_id = isset($params['match_id']) ? intval($params['match_id']) : null;
        
        $cache_key = "videos_p{$page}_l{$limit}_c" . ($category_id ?? 'all') . '_m' . ($match_id ?? 'all');
        
        $response = ResponseHelper::cache($cache_key, function() use ($page, $limit, $category_id, $match_id) {
            $stmt = $this->videoModel->getAll($page, $limit, $category_id, $match_id);
            
            $videos_arr = [];
            
            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                $videos_arr[] = [
                    "video_id" => (int)$row['video_id'],
                    "title" => $row['title'],
                    "description" => $row['description'],
                    "video_url" => $row['video_url'],
                    "thumbnail_url" => $row['thumbnail_url'],
                    "duration" => $row['duration'] ? (int)$row['duration'] : null,
                    "category_id" => $row['category_id'] ? (int)$row['category_id'] : null,
                    "match_id" => $row['match_id'] ? (int)$row['match_id'] : null,
                    "view_count" => (int)$row['view_count'],
                    "created_at" => $row['created_at'],
                    "category" => $row['category_name'] ? [
                        "category_id" => (int)$row['category_id'],
                        "category_name" => $row['category_name']
                    ] : null
                ];
            }
            
            return $videos_arr;
        }, 300);
        
        $this->json($response, 200, 300);
    }
    
    /**
     * GET /videos/:id
     */
    public function show($params) {
        $video_id = intval($params['id'] ?? 0);
        
        if ($video_id <= 0) {
            $this->error('ID video không hợp lệ', 400);
        }
        
        $cache_key = "video_" . $video_id;
        
        $video_data = ResponseHelper::cache($cache_key, function() use ($video_id) {
            $this->videoModel->video_id = $video_id;
            $data = $this->videoModel->getById();
            
            if ($data) {
                $data['video_id'] = (int)$data['video_id'];
                $data['duration'] = $data['duration'] ? (int)$data['duration'] : null;
                $data['category_id'] = $data['category_id'] ? (int)$data['category_id'] : null;
                $data['match_id'] = $data['match_id'] ? (int)$data['match_id'] : null;
                $data['view_count'] = (int)$data['view_count'];
            }
            return $data;
        }, 300);
        
        if ($video_data) {
            $this->json($video_data, 200, 300);
        } else {
            $this->error("Không tìm thấy video", 404);
        }
    }
    
    /**
     * GET /videos/highlights
     */
    public function highlights() {
        $params = $this->getQueryParams();
        $limit = isset($params['limit']) ? min(intval($params['limit']), 50) : 20;
        
        $cache_key = "videos_highlights_" . $limit;
        
        $response = ResponseHelper::cache($cache_key, function() use ($limit) {
            $stmt = $this->videoModel->getHighlights($limit);
            $videos_arr = [];
            
            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                $videos_arr[] = [
                    "video_id" => (int)$row['video_id'],
                    "title" => $row['title'],
                    "description" => $row['description'],
                    "video_url" => $row['video_url'],
                    "thumbnail_url" => $row['thumbnail_url'],
                    "duration" => $row['duration'] ? (int)$row['duration'] : null,
                    "category_id" => $row['category_id'] ? (int)$row['category_id'] : null,
                    "match_id" => $row['match_id'] ? (int)$row['match_id'] : null,
                    "view_count" => (int)$row['view_count'],
                    "created_at" => $row['created_at']
                ];
            }
            
            return $videos_arr;
        }, 300);
        
        $this->json($response, 200, 300);
    }
}
?>

