<?php
/**
 * Clear API Cache
 * Truy cập: http://localhost/api/clear_cache.php
 */

require_once 'config/ResponseHelper.php';

// Clear all cache files
ResponseHelper::clearCache();

header('Content-Type: application/json');
echo json_encode([
    'success' => true,
    'message' => 'Cache đã được xóa thành công',
    'timestamp' => date('Y-m-d H:i:s')
]);
?>

