# -*- coding: utf-8 -*-
"""
Main Crawler - Crawler chính cho hệ thống
"""

import logging
from logging.handlers import RotatingFileHandler
from colorama import init, Fore, Style
from database import DatabaseHandler
from parsers import VnExpressParser, VnExpressMatchParser
from config import NEWS_SOURCES, MATCH_SOURCES, LOG_FILE
import time
from datetime import datetime

# Initialize colorama
init(autoreset=True)

# Setup logging
def setup_logging():
    """Cấu hình logging"""
    logger = logging.getLogger()
    logger.setLevel(logging.INFO)
    
    # File handler
    file_handler = RotatingFileHandler(
        LOG_FILE, 
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


class NewsCrawler:
    """Crawler chính cho tin tức thể thao"""
    
    def __init__(self):
        self.db = DatabaseHandler()
        self.parsers = {
            'VnExpressParser': VnExpressParser(),
        }
        self.match_parsers = {
            'VnExpressMatchParser': VnExpressMatchParser(),
        }
        self.stats = {
            'total_crawled': 0,
            'total_saved': 0,
            'total_skipped': 0,
            'total_errors': 0,
            'matches_crawled': 0,
            'matches_saved': 0,
            'matches_skipped': 0,
            'matches_errors': 0
        }
    
    def print_header(self):
        """In header đẹp"""
        print(f"\n{Fore.CYAN}{'='*70}")
        print(f"{Fore.CYAN}║{' '*68}║")
        print(f"{Fore.CYAN}║{Fore.YELLOW}{'SPORTS NEWS CRAWLER':^68}{Fore.CYAN}║")
        print(f"{Fore.CYAN}║{Fore.GREEN}{'Công cụ crawl tin tức thể thao tự động':^68}{Fore.CYAN}║")
        print(f"{Fore.CYAN}║{' '*68}║")
        print(f"{Fore.CYAN}{'='*70}{Style.RESET_ALL}\n")
    
    def print_stats(self):
        """In thống kê"""
        print(f"\n{Fore.YELLOW}{'─'*70}")
        print(f"{Fore.YELLOW}THỐNG KÊ CRAWLER:")
        print(f"{Fore.GREEN}  ✓ Tổng số bài crawl: {self.stats['total_crawled']}")
        print(f"{Fore.GREEN}  ✓ Đã lưu thành công: {self.stats['total_saved']}")
        print(f"{Fore.YELLOW}  ⚠ Đã bỏ qua (trùng): {self.stats['total_skipped']}")
        print(f"{Fore.RED}  ✗ Lỗi: {self.stats['total_errors']}")
        print(f"{Fore.CYAN}  ─────────────────────────────────────")
        print(f"{Fore.GREEN}  ✓ Tổng số trận đấu crawl: {self.stats['matches_crawled']}")
        print(f"{Fore.GREEN}  ✓ Đã lưu thành công: {self.stats['matches_saved']}")
        print(f"{Fore.YELLOW}  ⚠ Đã bỏ qua (trùng): {self.stats['matches_skipped']}")
        print(f"{Fore.RED}  ✗ Lỗi: {self.stats['matches_errors']}")
        print(f"{Fore.YELLOW}{'─'*70}{Style.RESET_ALL}\n")
    
    def crawl_source(self, source_name, source_config, limit=10):
        """Crawl một nguồn tin"""
        if not source_config.get('enabled', False):
            logger.info(f"⊗ Nguồn {source_name} đã bị tắt")
            return
        
        print(f"\n{Fore.CYAN}▶ Bắt đầu crawl: {source_config['name']}")
        print(f"{Fore.CYAN}  URL: {source_config['base_url']}{Style.RESET_ALL}")
        
        parser_name = source_config.get('parser')
        if parser_name not in self.parsers:
            logger.error(f"✗ Không tìm thấy parser: {parser_name}")
            return
        
        parser = self.parsers[parser_name]
        
        try:
            # Lấy danh sách bài viết
            articles = parser.get_article_list(limit=limit)
            
            if not articles:
                logger.warning(f"⚠ Không tìm thấy bài viết nào từ {source_name}")
                return
            
            print(f"{Fore.GREEN}  ✓ Tìm thấy {len(articles)} bài viết\n")
            
            # Parse và lưu từng bài viết
            for idx, article_info in enumerate(articles, 1):
                print(f"{Fore.CYAN}  [{idx}/{len(articles)}] {article_info['title'][:60]}...")
                
                self.stats['total_crawled'] += 1
                
                # Parse chi tiết bài viết
                article_data = parser.parse_article(article_info['url'])
                
                if not article_data:
                    logger.error(f"  {Fore.RED}✗ Không thể parse bài viết")
                    self.stats['total_errors'] += 1
                    continue
                
                # Lưu vào database
                article_id = self.db.insert_article(article_data)
                
                if article_id:
                    print(f"  {Fore.GREEN}✓ Đã lưu (ID: {article_id})")
                    self.stats['total_saved'] += 1
                    
                    # Thêm tags
                    if article_data.get('tags'):
                        self.db.insert_article_tags(article_id, article_data['tags'])
                    
                    # Thêm images
                    if article_data.get('images'):
                        self.db.insert_article_images(article_id, article_data['images'])
                else:
                    print(f"  {Fore.YELLOW}⚠ Bỏ qua (đã tồn tại)")
                    self.stats['total_skipped'] += 1
                
                # Delay giữa các bài viết
                time.sleep(2)
            
        except Exception as e:
            logger.error(f"✗ Lỗi crawl nguồn {source_name}: {e}")
            self.stats['total_errors'] += 1
    
    def crawl_matches(self, source_name, source_config, limit=50):
        """Crawl các trận đấu sắp diễn ra từ một nguồn"""
        if not source_config.get('enabled', False):
            logger.info(f"⊗ Nguồn matches {source_name} đã bị tắt")
            return
        
        print(f"\n{Fore.CYAN}▶ Bắt đầu crawl lịch thi đấu: {source_config['name']}")
        print(f"{Fore.CYAN}  URL: {source_config['base_url']}{Style.RESET_ALL}")
        
        parser_name = source_config.get('parser')
        if parser_name not in self.match_parsers:
            logger.error(f"✗ Không tìm thấy match parser: {parser_name}")
            return
        
        parser = self.match_parsers[parser_name]
        
        try:
            # Lấy danh sách trận đấu
            matches = parser.get_upcoming_matches(limit=limit)
            
            if not matches:
                logger.warning(f"⚠ Không tìm thấy trận đấu nào từ {source_name}")
                return
            
            print(f"{Fore.GREEN}  ✓ Tìm thấy {len(matches)} trận đấu\n")
            
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
                    logger.error(f"  {Fore.RED}✗ Không thể tạo teams")
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
                    print(f"  {Fore.GREEN}✓ Đã lưu (ID: {match_id})")
                    self.stats['matches_saved'] += 1
                else:
                    print(f"  {Fore.YELLOW}⚠ Bỏ qua (đã tồn tại)")
                    self.stats['matches_skipped'] += 1
                
                # Delay giữa các trận đấu
                time.sleep(1)
            
        except Exception as e:
            logger.error(f"✗ Lỗi crawl matches từ {source_name}: {e}", exc_info=True)
            self.stats['matches_errors'] += 1
    
    def run(self, limit_per_source=10, crawl_matches=True):
        """Chạy crawler cho tất cả các nguồn"""
        self.print_header()
        
        start_time = time.time()
        logger.info(f"🚀 Bắt đầu crawl lúc: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        
        # Crawl từng nguồn tin tức
        for source_name, source_config in NEWS_SOURCES.items():
            self.crawl_source(source_name, source_config, limit=limit_per_source)
        
        # Crawl các trận đấu sắp diễn ra
        if crawl_matches:
            print(f"\n{Fore.MAGENTA}{'='*70}")
            print(f"{Fore.MAGENTA}▶ BẮT ĐẦU CRAWL LỊCH THI ĐẤU")
            print(f"{Fore.MAGENTA}{'='*70}{Style.RESET_ALL}\n")
            
            for source_name, source_config in MATCH_SOURCES.items():
                self.crawl_matches(source_name, source_config, limit=50)
        
        # Thống kê
        elapsed_time = time.time() - start_time
        self.print_stats()
        
        # Thống kê database
        db_stats = self.db.get_statistics()
        if db_stats:
            print(f"{Fore.CYAN}{'─'*70}")
            print(f"{Fore.CYAN}THỐNG KÊ DATABASE:")
            print(f"{Fore.GREEN}  • Tổng số bài viết: {db_stats['total_articles']}")
            print(f"{Fore.GREEN}  • Tổng số categories: {db_stats['total_categories']}")
            print(f"{Fore.GREEN}  • Tổng số tags: {db_stats['total_tags']}")
            
            if db_stats.get('total_matches'):
                print(f"{Fore.GREEN}  • Tổng số trận đấu: {db_stats['total_matches']}")
            
            if db_stats.get('by_status'):
                print(f"{Fore.CYAN}  Bài viết theo trạng thái:")
                for status_info in db_stats['by_status']:
                    print(f"{Fore.GREEN}    - {status_info['status']}: {status_info['count']}")
            
            if db_stats.get('matches_by_status'):
                print(f"{Fore.CYAN}  Trận đấu theo trạng thái:")
                for status_info in db_stats['matches_by_status']:
                    print(f"{Fore.GREEN}    - {status_info['status']}: {status_info['count']}")
            
            print(f"{Fore.CYAN}{'─'*70}{Style.RESET_ALL}\n")
        
        logger.info(f"✓ Hoàn thành trong {elapsed_time:.2f} giây")
        print(f"{Fore.GREEN}✓ Crawler hoàn thành!{Style.RESET_ALL}\n")
    
    def close(self):
        """Đóng các kết nối"""
        self.db.close()


def main():
    """Hàm main"""
    try:
        crawler = NewsCrawler()
        
        # Crawl 10 bài viết từ mỗi nguồn và các trận đấu sắp diễn ra
        crawler.run(limit_per_source=10, crawl_matches=True)
        
        crawler.close()
        
    except KeyboardInterrupt:
        print(f"\n{Fore.YELLOW}⚠ Đã dừng crawler bởi người dùng{Style.RESET_ALL}")
    except Exception as e:
        logger.error(f"✗ Lỗi nghiêm trọng: {e}", exc_info=True)


if __name__ == '__main__':
    main()

