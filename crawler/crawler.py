# -*- coding: utf-8 -*-
"""
Main Crawler - Crawler chính cho hệ thống
"""

import logging
from logging.handlers import RotatingFileHandler
from colorama import init, Fore, Style
from database import DatabaseHandler
from parsers import VnExpressParser
from config import NEWS_SOURCES, LOG_FILE
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
        self.stats = {
            'total_crawled': 0,
            'total_saved': 0,
            'total_skipped': 0,
            'total_errors': 0
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
    
    def run(self, limit_per_source=10):
        """Chạy crawler cho tất cả các nguồn"""
        self.print_header()
        
        start_time = time.time()
        logger.info(f"🚀 Bắt đầu crawl lúc: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        
        # Crawl từng nguồn tin tức
        for source_name, source_config in NEWS_SOURCES.items():
            self.crawl_source(source_name, source_config, limit=limit_per_source)
        
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
            
            if db_stats.get('by_status'):
                print(f"{Fore.CYAN}  Bài viết theo trạng thái:")
                for status_info in db_stats['by_status']:
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
        
        # Crawl 10 bài viết từ mỗi nguồn
        crawler.run(limit_per_source=10)
        
        crawler.close()
        
    except KeyboardInterrupt:
        print(f"\n{Fore.YELLOW}⚠ Đã dừng crawler bởi người dùng{Style.RESET_ALL}")
    except Exception as e:
        logger.error(f"✗ Lỗi nghiêm trọng: {e}", exc_info=True)


if __name__ == '__main__':
    main()

