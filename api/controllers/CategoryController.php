<?php
/**
 * Category Controller - Xử lý các thao tác với danh mục
 */
class CategoryController extends Controller {
    private $categoryModel;
    
    public function __construct() {
        parent::__construct();
        $this->categoryModel = new Category($this->db);
    }
    
    /**
     * GET /categories
     */
    public function index() {
        $cache_key = "categories_all";
        
        $response = ResponseHelper::cache($cache_key, function() {
            $stmt = $this->categoryModel->getAll();
            $categories_arr = [];
            
            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                $categories_arr[] = [
                    "category_id" => (int)$row['category_id'],
                    "category_name" => $row['category_name'],
                    "category_slug" => $row['category_slug'],
                    "description" => $row['description'],
                    "icon_url" => $row['icon_url'],
                    "parent_id" => $row['parent_id'] ? (int)$row['parent_id'] : null,
                    "display_order" => (int)$row['display_order'],
                    "is_active" => (bool)$row['is_active']
                ];
            }
            
            return ["categories" => $categories_arr];
        }, 600);
        
        $this->json($response, 200, 600);
    }
    
    /**
     * GET /categories/:id
     */
    public function show($params) {
        $category_id = intval($params['id'] ?? 0);
        
        if ($category_id <= 0) {
            $this->error('ID danh mục không hợp lệ', 400);
        }
        
        $cache_key = "category_" . $category_id;
        
        $category_data = ResponseHelper::cache($cache_key, function() use ($category_id) {
            $this->categoryModel->category_id = $category_id;
            $stmt = $this->categoryModel->getById();
            $data = $stmt->fetch(PDO::FETCH_ASSOC);
            
            if ($data) {
                $data['category_id'] = (int)$data['category_id'];
                $data['parent_id'] = $data['parent_id'] ? (int)$data['parent_id'] : null;
                $data['display_order'] = (int)$data['display_order'];
                $data['is_active'] = (bool)$data['is_active'];
            }
            return $data;
        }, 600);
        
        if ($category_data) {
            $this->json($category_data, 200, 600);
        } else {
            $this->error("Không tìm thấy danh mục", 404);
        }
    }
}
?>

