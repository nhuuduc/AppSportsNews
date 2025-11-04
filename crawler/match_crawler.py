# -*- coding: utf-8 -*-
"""
Match Crawler - Crawler chuyên dụng cho lịch thi đấu
"""

import logging
from logging.handlers import RotatingFileHandler
from colorama import init, Fore, Style
from database import DatabaseHandler
from parsers import VnExpressMatchParser, RobongMatchParser
from config import MATCH_SOURCES, LOG_FILE
import time
import sys
from datetime import datetime, timedelta

# Set UTF-8 encoding for Windows console
if sys.platform == 'win32':
    import io
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='replace')

# Initialize colorama
init(autoreset=True)

# Setup logging
def setup_logging():
    """Cấu hình logging"""
    logger = logging.getLogger('match_crawler')
    logger.setLevel(logging.INFO)
    
    # File handler
    log_file = LOG_FILE.parent / 'match_crawler.log'
    file_handler = RotatingFileHandler(
        log_file, 
        maxBytes=10*1024*1024,  # 10MB
        backupCount=5,
        encoding='utf-8'
    )
    file_handler.setLevel(logging.INFO)
    file_formatter = logging.Formatter(
        '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    file_handler.setFormatter(file_formatter)
    
    # Console handler
    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.INFO)
    console_formatter = logging.Formatter('%(message)s')
    console_handler.setFormatter(console_formatter)
    
    logger.addHandler(file_handler)
    logger.addHandler(console_handler)
    
    return logger

logger = setup_logging()


class MatchCrawler:
    """Crawler chuyên dụng cho lịch thi đấu"""
    
    def __init__(self):
        self.db = DatabaseHandler()
        self.match_parsers = {
            'VnExpressMatchParser': VnExpressMatchParser(),
            'RobongMatchParser': RobongMatchParser(),
        }
        self.stats = {
            'matches_crawled': 0,
            'matches_saved': 0,
            'matches_skipped': 0,
            'matches_errors': 0
        }
    
    def print_header(self):
        """In header đẹp"""
        try:
            print(f"\n{Fore.CYAN}{'='*70}")
            print(f"{Fore.CYAN}|{' '*68}|")
            print(f"{Fore.CYAN}|{Fore.YELLOW}{'MATCH SCHEDULE CRAWLER':^68}{Fore.CYAN}|")
            print(f"{Fore.CYAN}|{Fore.GREEN}{'Công cụ crawl lịch thi đấu tự động':^68}{Fore.CYAN}|")
            print(f"{Fore.CYAN}|{' '*68}|")
            print(f"{Fore.CYAN}{'='*70}{Style.RESET_ALL}\n")
        except UnicodeEncodeError:
            # Fallback to ASCII if Unicode fails
            print(f"\n{Fore.CYAN}{'='*70}")
            print(f"{Fore.CYAN}|{' '*68}|")
            print(f"{Fore.CYAN}|{Fore.YELLOW}{'MATCH SCHEDULE CRAWLER':^68}{Fore.CYAN}|")
            print(f"{Fore.CYAN}|{Fore.GREEN}{'Công cụ crawl lịch thi đấu tự động':^68}{Fore.CYAN}|")
            print(f"{Fore.CYAN}|{' '*68}|")
            print(f"{Fore.CYAN}{'='*70}{Style.RESET_ALL}\n")
    
    def print_stats(self):
        """In thống kê"""
        try:
            print(f"\n{Fore.YELLOW}{'-'*70}")
            print(f"{Fore.YELLOW}THỐNG KÊ CRAWLER LỊCH THI ĐẤU:")
            print(f"{Fore.GREEN}  [OK] Tổng số trận đấu crawl: {self.stats['matches_crawled']}")
            print(f"{Fore.GREEN}  [OK] Đã lưu thành công: {self.stats['matches_saved']}")
            print(f"{Fore.YELLOW}  [SKIP] Đã bỏ qua (trùng): {self.stats['matches_skipped']}")
            print(f"{Fore.RED}  [ERROR] Lỗi: {self.stats['matches_errors']}")
            print(f"{Fore.YELLOW}{'-'*70}{Style.RESET_ALL}\n")
        except UnicodeEncodeError:
            # Fallback to ASCII
            print(f"\n{Fore.YELLOW}{'-'*70}")
            print(f"{Fore.YELLOW}THONG KE CRAWLER LICH THI DAU:")
            print(f"{Fore.GREEN}  [OK] Tong so tran dau crawl: {self.stats['matches_crawled']}")
            print(f"{Fore.GREEN}  [OK] Da luu thanh cong: {self.stats['matches_saved']}")
            print(f"{Fore.YELLOW}  [SKIP] Da bo qua (trung): {self.stats['matches_skipped']}")
            print(f"{Fore.RED}  [ERROR] Loi: {self.stats['matches_errors']}")
            print(f"{Fore.YELLOW}{'-'*70}{Style.RESET_ALL}\n")
    
    def crawl_matches(self, source_name, source_config, limit=50, days_range=None):
        """
        Crawl các trận đấu sắp diễn ra từ một nguồn
        
        Args:
            source_name: Tên nguồn
            source_config: Config của nguồn
            limit: Số lượng trận đấu tối đa
            days_range: Tuple (days_before, days_after) để filter theo ngày
                       Ví dụ: (1, 1) = hôm qua, hôm nay, hôm sau
        """
        if not source_config.get('enabled', False):
            logger.info(f"[SKIP] Nguồn matches {source_name} đã bị tắt")
            return
        
        print(f"\n{Fore.CYAN}▶ Bắt đầu crawl lịch thi đấu: {source_config['name']}")
        print(f"{Fore.CYAN}  URL: {source_config['base_url']}{Style.RESET_ALL}")
        
        parser_name = source_config.get('parser')
        if parser_name not in self.match_parsers:
            logger.error(f"[ERROR] Không tìm thấy match parser: {parser_name}")
            return
        
        parser = self.match_parsers[parser_name]
        
        # Cập nhật base_url từ config
        if hasattr(parser, 'base_url'):
            parser.base_url = source_config['base_url']
        
        try:
            # Lấy danh sách trận đấu với filter theo ngày
            matches = parser.get_upcoming_matches(limit=limit, days_range=days_range)
            
            if not matches:
                logger.warning(f"[WARN] Không tìm thấy trận đấu nào từ {source_name}")
                return
            
            print(f"{Fore.GREEN}  [OK] Tìm thấy {len(matches)} trận đấu\n")
            
            # Lưu từng trận đấu
            for idx, match_info in enumerate(matches, 1):
                home_team_name = match_info.get('home_team_name', 'Unknown')
                away_team_name = match_info.get('away_team_name', 'Unknown')
                match_date = match_info.get('match_date', datetime.now())
                
                print(f"{Fore.CYAN}  [{idx}/{len(matches)}] {home_team_name} vs {away_team_name}")
                print(f"      Ngày: {match_date.strftime('%d/%m/%Y %H:%M') if isinstance(match_date, datetime) else match_date}")
                
                self.stats['matches_crawled'] += 1
                
                # Lấy hoặc tạo teams
                home_team_id = self.db.get_or_create_team(
                    home_team_name,
                    team_code=match_info.get('home_team_code'),
                    logo_url=match_info.get('home_team_logo')
                )
                away_team_id = self.db.get_or_create_team(
                    away_team_name,
                    team_code=match_info.get('away_team_code'),
                    logo_url=match_info.get('away_team_logo')
                )
                
                if not home_team_id or not away_team_id:
                    logger.error(f"  {Fore.RED}[ERROR] Không thể tạo teams")
                    self.stats['matches_errors'] += 1
                    continue
                
                # Chuẩn bị dữ liệu match
                match_data = {
                    'home_team_id': home_team_id,
                    'away_team_id': away_team_id,
                    'home_team_name': home_team_name,
                    'away_team_name': away_team_name,
                    'match_date': match_date if isinstance(match_date, datetime) else datetime.now(),
                    'tournament_name': match_info.get('tournament_name', ''),
                    'category_id': match_info.get('category_id', 1),
                    'venue': match_info.get('venue', ''),
                    'status': match_info.get('status', 'scheduled')
                }
                
                # Lưu vào database
                match_id = self.db.insert_match(match_data)
                
                if match_id:
                    print(f"  {Fore.GREEN}[OK] Đã lưu (ID: {match_id})")
                    self.stats['matches_saved'] += 1
                else:
                    print(f"  {Fore.YELLOW}[SKIP] Bỏ qua (đã tồn tại)")
                    self.stats['matches_skipped'] += 1
                
                # Delay giữa các trận đấu
                time.sleep(1)
            
        except Exception as e:
            logger.error(f"[ERROR] Lỗi crawl matches từ {source_name}: {e}", exc_info=True)
            self.stats['matches_errors'] += 1
    
    def run(self, limit_per_source=50, days_range=None):
        """
        Chạy crawler cho tất cả các nguồn lịch thi đấu
        
        Args:
            limit_per_source: Số lượng trận đấu tối đa mỗi nguồn
            days_range: Tuple (days_before, days_after) để filter theo ngày
                       Ví dụ: (1, 1) = hôm qua, hôm nay, hôm sau
                       None = lấy tất cả các trận sắp diễn ra
        """
        self.print_header()
        
        start_time = time.time()
        logger.info(f"🚀 Bắt đầu crawl lịch thi đấu lúc: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        
        if days_range:
            days_before, days_after = days_range
            now = datetime.now().replace(hour=0, minute=0, second=0, microsecond=0)
            start_date = now - timedelta(days=days_before)
            end_date = now + timedelta(days=days_after)
            print(f"{Fore.YELLOW}📅 Lọc trận đấu từ {start_date.strftime('%d/%m/%Y')} đến {end_date.strftime('%d/%m/%Y')}{Style.RESET_ALL}\n")
        
        # Crawl các trận đấu sắp diễn ra từ tất cả nguồn
        for source_name, source_config in MATCH_SOURCES.items():
            self.crawl_matches(source_name, source_config, limit=limit_per_source, days_range=days_range)
        
        # Thống kê
        elapsed_time = time.time() - start_time
        self.print_stats()
        
        # Thống kê database
        db_stats = self.db.get_statistics()
        if db_stats:
            print(f"{Fore.CYAN}{'-'*70}")
            print(f"{Fore.CYAN}THỐNG KÊ DATABASE:")
            
            if db_stats.get('total_matches'):
                print(f"{Fore.GREEN}  - Tổng số trận đấu: {db_stats['total_matches']}")
            
            if db_stats.get('matches_by_status'):
                print(f"{Fore.CYAN}  Trận đấu theo trạng thái:")
                for status_info in db_stats['matches_by_status']:
                    print(f"{Fore.GREEN}    - {status_info['status']}: {status_info['count']}")
            
            print(f"{Fore.CYAN}{'-'*70}{Style.RESET_ALL}\n")
        
        logger.info(f"[OK] Hoàn thành trong {elapsed_time:.2f} giây")
        print(f"{Fore.GREEN}[OK] Crawler lịch thi đấu hoàn thành!{Style.RESET_ALL}\n")
    
    def close(self):
        """Đóng các kết nối"""
        self.db.close()


def main():
    """Hàm main"""
    try:
        crawler = MatchCrawler()
        
        # Crawl lịch thi đấu từ tất cả nguồn
        # days_range=(1, 1) = lấy hôm qua, hôm nay, hôm sau
        crawler.run(limit_per_source=50, days_range=(1, 1))
        
        crawler.close()
        
    except KeyboardInterrupt:
        print(f"\n{Fore.YELLOW}[WARN] Đã dừng crawler bởi người dùng{Style.RESET_ALL}")
    except Exception as e:
        logger.error(f"[ERROR] Lỗi nghiêm trọng: {e}", exc_info=True)


if __name__ == '__main__':
    main()

