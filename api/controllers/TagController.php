<?php
/**
 * Tag Controller - Xử lý các thao tác với tag
 */
class TagController extends Controller {
    private $tagModel;
    
    public function __construct() {
        parent::__construct();
        $this->tagModel = new Tag($this->db);
    }
    
    /**
     * GET /tags
     */
    public function index() {
        $params = $this->getQueryParams();
        $limit = isset($params['limit']) ? min(intval($params['limit']), 100) : 50;
        
        $cache_key = "tags_all_" . $limit;
        
        $response = ResponseHelper::cache($cache_key, function() use ($limit) {
            $stmt = $this->tagModel->getAll($limit);
            $tags_arr = [];
            
            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                $tags_arr[] = [
                    "tag_id" => (int)$row['tag_id'],
                    "tag_name" => $row['tag_name'],
                    "tag_slug" => $row['tag_slug'],
                    "article_count" => isset($row['article_count']) ? (int)$row['article_count'] : 0
                ];
            }
            
            return ["tags" => $tags_arr];
        }, 600);
        
        $this->json($response, 200, 600);
    }
    
    /**
     * GET /tags/:id
     */
    public function show($params) {
        $tag_id = intval($params['id'] ?? 0);
        
        if ($tag_id <= 0) {
            $this->error('ID tag không hợp lệ', 400);
        }
        
        // Get tag info and associated articles
        $query_params = $this->getQueryParams();
        $page = isset($query_params['page']) ? intval($query_params['page']) : 1;
        $limit = isset($query_params['limit']) ? min(intval($query_params['limit']), 50) : 20;
        
        $cache_key = "tag_{$tag_id}_p{$page}_l{$limit}";
        
        $response = ResponseHelper::cache($cache_key, function() use ($tag_id, $page, $limit) {
            // Get tag info
            $this->tagModel->tag_id = $tag_id;
            $tag = $this->tagModel->getById();
            
            if (!$tag) {
                return null;
            }
            
            $tag['tag_id'] = (int)$tag['tag_id'];
            
            // Get articles with this tag
            $offset = ($page - 1) * $limit;
            $articles = $this->tagModel->getArticlesByTag($tag_id, $page, $limit);
            $total_count = $this->tagModel->getArticlesCount($tag_id);
            
            $tag['articles'] = $articles;
            $tag['total_articles'] = (int)$total_count;
            $tag['current_page'] = $page;
            $tag['total_pages'] = ceil($total_count / $limit);
            
            return $tag;
        }, 300);
        
        if ($response) {
            $this->json($response, 200, 300);
        } else {
            $this->error("Không tìm thấy tag", 404);
        }
    }
}
?>

