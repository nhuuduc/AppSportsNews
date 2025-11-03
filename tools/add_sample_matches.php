<?php
/**
 * Script để thêm trận đấu mẫu vào database
 * Sử dụng: php add_sample_matches.php
 */

require_once __DIR__ . '/../api/config/database.php';

try {
    $database = new Database();
    $db = $database->getConnection();
    
    echo "=== THÊM TRẬN ĐẤU MẪU VÀO DATABASE ===\n\n";
    
    // Kiểm tra và tạo teams nếu chưa có
    $team_check = $db->query("SELECT COUNT(*) as count FROM teams")->fetch(PDO::FETCH_ASSOC);
    if ($team_check['count'] == 0) {
        echo "Đang tạo teams mẫu...\n";
        $sample_teams = [
            ['Manchester United', 'MUN', 'https://upload.wikimedia.org/wikipedia/en/7/7a/Manchester_United_FC_crest.svg', 1, 'England', 'Old Trafford', 1878, 'Manchester United Football Club'],
            ['Manchester City', 'MCI', 'https://upload.wikimedia.org/wikipedia/en/e/eb/Manchester_City_FC_badge.svg', 1, 'England', 'Etihad Stadium', 1880, 'Manchester City Football Club'],
            ['Liverpool', 'LIV', 'https://upload.wikimedia.org/wikipedia/en/0/0c/Liverpool_FC.svg', 1, 'England', 'Anfield', 1892, 'Liverpool Football Club'],
            ['Chelsea', 'CHE', 'https://upload.wikimedia.org/wikipedia/en/c/cc/Chelsea_FC.svg', 1, 'England', 'Stamford Bridge', 1905, 'Chelsea Football Club'],
            ['Arsenal', 'ARS', 'https://upload.wikimedia.org/wikipedia/en/5/53/Arsenal_FC.svg', 1, 'England', 'Emirates Stadium', 1886, 'Arsenal Football Club'],
            ['Real Madrid', 'RMA', 'https://upload.wikimedia.org/wikipedia/en/5/56/Real_Madrid_CF.svg', 1, 'Spain', 'Santiago Bernabéu', 1902, 'Real Madrid Club de Fútbol'],
            ['Barcelona', 'BAR', 'https://upload.wikimedia.org/wikipedia/en/4/47/FC_Barcelona_%28crest%29.svg', 1, 'Spain', 'Camp Nou', 1899, 'Futbol Club Barcelona'],
            ['Bayern Munich', 'BAY', 'https://upload.wikimedia.org/wikipedia/commons/1/1b/FC_Bayern_M%C3%BCnchen_logo_%282017%29.svg', 1, 'Germany', 'Allianz Arena', 1900, 'FC Bayern München'],
            ['Paris Saint-Germain', 'PSG', 'https://upload.wikimedia.org/wikipedia/en/a/a7/Paris_Saint-Germain_F.C..svg', 1, 'France', 'Parc des Princes', 1970, 'Paris Saint-Germain Football Club'],
            ['Juventus', 'JUV', 'https://upload.wikimedia.org/wikipedia/commons/1/15/Juventus_FC_2017_logo.svg', 1, 'Italy', 'Allianz Stadium', 1897, 'Juventus Football Club'],
        ];
        
        $stmt = $db->prepare("INSERT INTO teams (team_name, team_code, logo_url, category_id, country, stadium, founded_year, description, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)");
        
        foreach ($sample_teams as $team) {
            $stmt->execute($team);
        }
        echo "✓ Đã tạo " . count($sample_teams) . " teams\n\n";
    } else {
        echo "✓ Database đã có " . $team_check['count'] . " teams\n\n";
    }
    
    // Lấy danh sách team IDs
    $teams = $db->query("SELECT team_id, team_name FROM teams ORDER BY team_id")->fetchAll(PDO::FETCH_ASSOC);
    
    if (count($teams) < 4) {
        echo "⚠ Cần ít nhất 4 teams để tạo trận đấu!\n";
        exit(1);
    }
    
    // Tạo trận đấu mẫu
    $current_date = date('Y-m-d H:i:s');
    $matches = [
        // Trận đã kết thúc
        [
            'home_team_id' => $teams[0]['team_id'],
            'away_team_id' => $teams[1]['team_id'],
            'category_id' => 1,
            'tournament_name' => 'Premier League 2024/25',
            'match_date' => date('Y-m-d H:i:s', strtotime('-2 days')),
            'venue' => 'Old Trafford',
            'home_score' => 2,
            'away_score' => 1,
            'status' => 'finished',
            'highlight_url' => 'https://www.youtube.com/watch?v=example1'
        ],
        [
            'home_team_id' => $teams[2]['team_id'],
            'away_team_id' => $teams[3]['team_id'],
            'category_id' => 1,
            'tournament_name' => 'Premier League 2024/25',
            'match_date' => date('Y-m-d H:i:s', strtotime('-1 day')),
            'venue' => 'Anfield',
            'home_score' => 3,
            'away_score' => 0,
            'status' => 'finished',
            'highlight_url' => 'https://www.youtube.com/watch?v=example2'
        ],
        // Trận đang diễn ra
        [
            'home_team_id' => $teams[4]['team_id'] ?? $teams[0]['team_id'],
            'away_team_id' => $teams[5]['team_id'] ?? $teams[1]['team_id'],
            'category_id' => 1,
            'tournament_name' => 'Premier League 2024/25',
            'match_date' => $current_date,
            'venue' => 'Emirates Stadium',
            'home_score' => 1,
            'away_score' => 1,
            'status' => 'live',
            'highlight_url' => null
        ],
        // Trận sắp diễn ra
        [
            'home_team_id' => $teams[6]['team_id'] ?? $teams[2]['team_id'],
            'away_team_id' => $teams[7]['team_id'] ?? $teams[3]['team_id'],
            'category_id' => 1,
            'tournament_name' => 'UEFA Champions League',
            'match_date' => date('Y-m-d H:i:s', strtotime('+1 day')),
            'venue' => 'Camp Nou',
            'home_score' => null,
            'away_score' => null,
            'status' => 'scheduled',
            'highlight_url' => null
        ],
        [
            'home_team_id' => $teams[8]['team_id'] ?? $teams[0]['team_id'],
            'away_team_id' => $teams[9]['team_id'] ?? $teams[1]['team_id'],
            'category_id' => 1,
            'tournament_name' => 'UEFA Champions League',
            'match_date' => date('Y-m-d H:i:s', strtotime('+2 days')),
            'venue' => 'Allianz Arena',
            'home_score' => null,
            'away_score' => null,
            'status' => 'scheduled',
            'highlight_url' => null
        ],
        [
            'home_team_id' => $teams[1]['team_id'],
            'away_team_id' => $teams[2]['team_id'],
            'category_id' => 1,
            'tournament_name' => 'Premier League 2024/25',
            'match_date' => date('Y-m-d H:i:s', strtotime('+3 days')),
            'venue' => 'Etihad Stadium',
            'home_score' => null,
            'away_score' => null,
            'status' => 'scheduled',
            'highlight_url' => null
        ],
        [
            'home_team_id' => $teams[3]['team_id'],
            'away_team_id' => $teams[4]['team_id'] ?? $teams[0]['team_id'],
            'category_id' => 1,
            'tournament_name' => 'Premier League 2024/25',
            'match_date' => date('Y-m-d H:i:s', strtotime('+4 days')),
            'venue' => 'Stamford Bridge',
            'home_score' => null,
            'away_score' => null,
            'status' => 'scheduled',
            'highlight_url' => null
        ],
        [
            'home_team_id' => $teams[5]['team_id'] ?? $teams[1]['team_id'],
            'away_team_id' => $teams[0]['team_id'],
            'category_id' => 1,
            'tournament_name' => 'UEFA Champions League',
            'match_date' => date('Y-m-d H:i:s', strtotime('+5 days')),
            'venue' => 'Santiago Bernabéu',
            'home_score' => null,
            'away_score' => null,
            'status' => 'scheduled',
            'highlight_url' => null
        ],
    ];
    
    $stmt = $db->prepare("
        INSERT INTO matches 
        (home_team_id, away_team_id, category_id, tournament_name, match_date, venue, home_score, away_score, status, highlight_url) 
        VALUES 
        (:home_team_id, :away_team_id, :category_id, :tournament_name, :match_date, :venue, :home_score, :away_score, :status, :highlight_url)
    ");
    
    echo "Đang thêm trận đấu...\n";
    echo str_repeat("-", 80) . "\n";
    
    $added = 0;
    foreach ($matches as $index => $match) {
        try {
            $stmt->execute($match);
            $added++;
            
            $home_team = array_values(array_filter($teams, fn($t) => $t['team_id'] == $match['home_team_id']))[0] ?? null;
            $away_team = array_values(array_filter($teams, fn($t) => $t['team_id'] == $match['away_team_id']))[0] ?? null;
            
            $status_emoji = [
                'finished' => '✅',
                'live' => '🔴',
                'scheduled' => '📅'
            ];
            
            echo sprintf(
                "%s Trận %d: %s vs %s - %s (%s)\n",
                $status_emoji[$match['status']] ?? '⚽',
                $index + 1,
                $home_team['team_name'] ?? 'Unknown',
                $away_team['team_name'] ?? 'Unknown',
                $match['tournament_name'],
                $match['status']
            );
            
            if ($match['home_score'] !== null) {
                echo "   Tỷ số: {$match['home_score']} - {$match['away_score']}\n";
            }
            echo "   Thời gian: " . date('d/m/Y H:i', strtotime($match['match_date'])) . "\n";
            echo "   Sân vận động: {$match['venue']}\n\n";
            
        } catch (PDOException $e) {
            echo "⚠ Lỗi khi thêm trận " . ($index + 1) . ": " . $e->getMessage() . "\n\n";
        }
    }
    
    echo str_repeat("-", 80) . "\n";
    echo "\n✅ Hoàn thành! Đã thêm $added trận đấu vào database.\n";
    
    // Hiển thị tổng kết
    $total_matches = $db->query("SELECT COUNT(*) as count FROM matches")->fetch(PDO::FETCH_ASSOC);
    $finished = $db->query("SELECT COUNT(*) as count FROM matches WHERE status = 'finished'")->fetch(PDO::FETCH_ASSOC);
    $live = $db->query("SELECT COUNT(*) as count FROM matches WHERE status = 'live'")->fetch(PDO::FETCH_ASSOC);
    $scheduled = $db->query("SELECT COUNT(*) as count FROM matches WHERE status = 'scheduled'")->fetch(PDO::FETCH_ASSOC);
    
    echo "\n📊 TỔNG KẾT:\n";
    echo "   Tổng số trận: " . $total_matches['count'] . "\n";
    echo "   ✅ Đã kết thúc: " . $finished['count'] . "\n";
    echo "   🔴 Đang diễn ra: " . $live['count'] . "\n";
    echo "   📅 Sắp diễn ra: " . $scheduled['count'] . "\n";
    
} catch (PDOException $e) {
    echo "❌ Lỗi database: " . $e->getMessage() . "\n";
    exit(1);
} catch (Exception $e) {
    echo "❌ Lỗi: " . $e->getMessage() . "\n";
    exit(1);
}
?>

























