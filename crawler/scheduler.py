# -*- coding: utf-8 -*-
"""
Scheduler - Lịch trình tự động crawl
"""

import schedule
import time
import logging
from crawler import NewsCrawler
from datetime import datetime
from colorama import init, Fore, Style

init(autoreset=True)
logger = logging.getLogger(__name__)


def run_crawler_job():
    """Job chạy crawler"""
    print(f"\n{Fore.CYAN}{'='*70}")
    print(f"{Fore.YELLOW}⏰ Bắt đầu job crawl tự động - {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"{Fore.CYAN}{'='*70}{Style.RESET_ALL}\n")
    
    try:
        crawler = NewsCrawler()
        crawler.run(limit_per_source=5)  # Crawl 5 bài mỗi lần
        crawler.close()
        
        print(f"\n{Fore.GREEN}✓ Job crawl hoàn thành - {datetime.now().strftime('%H:%M:%S')}{Style.RESET_ALL}\n")
        
    except Exception as e:
        logger.error(f"✗ Lỗi trong job crawl: {e}", exc_info=True)


def main():
    """Main scheduler"""
    print(f"\n{Fore.CYAN}{'='*70}")
    print(f"{Fore.CYAN}║{' '*68}║")
    print(f"{Fore.CYAN}║{Fore.YELLOW}{'NEWS CRAWLER SCHEDULER':^68}{Fore.CYAN}║")
    print(f"{Fore.CYAN}║{Fore.GREEN}{'Tự động crawl tin tức theo lịch':^68}{Fore.CYAN}║")
    print(f"{Fore.CYAN}║{' '*68}║")
    print(f"{Fore.CYAN}{'='*70}{Style.RESET_ALL}\n")
    
    # Cấu hình lịch crawl
    print(f"{Fore.YELLOW}📅 Cấu hình lịch crawl:{Style.RESET_ALL}")
    print(f"{Fore.GREEN}  • Mỗi 2 giờ một lần")
    print(f"{Fore.GREEN}  • 06:00, 10:00, 14:00, 18:00, 22:00 hàng ngày{Style.RESET_ALL}\n")
    
    # Lên lịch crawl mỗi 2 giờ
    schedule.every(2).hours.do(run_crawler_job)
    
    # Hoặc lên lịch vào các giờ cụ thể
    schedule.every().day.at("06:00").do(run_crawler_job)
    schedule.every().day.at("10:00").do(run_crawler_job)
    schedule.every().day.at("14:00").do(run_crawler_job)
    schedule.every().day.at("18:00").do(run_crawler_job)
    schedule.every().day.at("22:00").do(run_crawler_job)
    
    print(f"{Fore.GREEN}✓ Scheduler đã khởi động!")
    print(f"{Fore.YELLOW}⏱  Đang chờ job tiếp theo...{Style.RESET_ALL}\n")
    
    # Chạy ngay lần đầu
    print(f"{Fore.CYAN}▶ Chạy crawler lần đầu...{Style.RESET_ALL}")
    run_crawler_job()
    
    # Vòng lặp chính
    try:
        while True:
            schedule.run_pending()
            time.sleep(60)  # Check mỗi phút
            
    except KeyboardInterrupt:
        print(f"\n{Fore.YELLOW}⚠ Đã dừng scheduler{Style.RESET_ALL}\n")


if __name__ == '__main__':
    main()

