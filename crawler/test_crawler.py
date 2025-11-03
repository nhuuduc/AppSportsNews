# -*- coding: utf-8 -*-
"""
Test Script - Kiểm tra crawler
"""

import sys
from colorama import init, Fore, Style
from database import DatabaseHandler
from parsers import VnExpressParser

init(autoreset=True)


def test_database_connection():
    """Test kết nối database"""
    print(f"\n{Fore.CYAN}[TEST] Kiểm tra kết nối database...")
    
    try:
        db = DatabaseHandler()
        if db.connection and db.connection.is_connected():
            print(f"{Fore.GREEN}✓ Kết nối database thành công!")
            
            # Lấy thống kê
            stats = db.get_statistics()
            if stats:
                print(f"{Fore.GREEN}✓ Database có {stats['total_articles']} bài viết")
                print(f"{Fore.GREEN}✓ Database có {stats['total_categories']} categories")
            
            db.close()
            return True
        else:
            print(f"{Fore.RED}✗ Không thể kết nối database!")
            return False
    except Exception as e:
        print(f"{Fore.RED}✗ Lỗi: {e}")
        return False


def test_parser():
    """Test parser"""
    print(f"\n{Fore.CYAN}[TEST] Kiểm tra VnExpress Parser...")
    
    try:
        parser = VnExpressParser()
        
        # Test lấy danh sách
        print(f"{Fore.YELLOW}  Đang lấy danh sách bài viết...")
        articles = parser.get_article_list(limit=2)
        
        if articles:
            print(f"{Fore.GREEN}✓ Lấy được {len(articles)} bài viết")
            
            # Test parse bài đầu tiên
            if articles:
                print(f"\n{Fore.YELLOW}  Đang parse bài viết đầu tiên...")
                print(f"  URL: {articles[0]['url']}")
                
                article_data = parser.parse_article(articles[0]['url'])
                
                if article_data:
                    print(f"{Fore.GREEN}✓ Parse thành công!")
                    print(f"  Title: {article_data['title'][:60]}...")
                    print(f"  Slug: {article_data['slug']}")
                    print(f"  Content length: {len(article_data['content'])} chars")
                    print(f"  Tags: {len(article_data.get('tags', []))}")
                    print(f"  Images: {len(article_data.get('images', []))}")
                    return True
                else:
                    print(f"{Fore.RED}✗ Không thể parse bài viết!")
                    return False
        else:
            print(f"{Fore.RED}✗ Không lấy được bài viết nào!")
            return False
            
    except Exception as e:
        print(f"{Fore.RED}✗ Lỗi: {e}")
        import traceback
        traceback.print_exc()
        return False


def main():
    """Main test"""
    print(f"\n{Fore.CYAN}{'='*70}")
    print(f"{Fore.YELLOW}KIỂM TRA CRAWLER")
    print(f"{Fore.CYAN}{'='*70}{Style.RESET_ALL}")
    
    # Test database
    db_ok = test_database_connection()
    
    # Test parser
    parser_ok = test_parser()
    
    # Kết quả
    print(f"\n{Fore.CYAN}{'='*70}")
    print(f"{Fore.YELLOW}KẾT QUẢ KIỂM TRA")
    print(f"{Fore.CYAN}{'='*70}")
    
    if db_ok:
        print(f"{Fore.GREEN}✓ Database: OK")
    else:
        print(f"{Fore.RED}✗ Database: FAILED")
    
    if parser_ok:
        print(f"{Fore.GREEN}✓ Parser: OK")
    else:
        print(f"{Fore.RED}✗ Parser: FAILED")
    
    print(f"{Fore.CYAN}{'='*70}{Style.RESET_ALL}\n")
    
    if db_ok and parser_ok:
        print(f"{Fore.GREEN}✓ Tất cả test đều PASS! Bạn có thể chạy crawler.")
        return 0
    else:
        print(f"{Fore.RED}✗ Có test FAILED! Vui lòng kiểm tra lại cấu hình.")
        return 1


if __name__ == '__main__':
    sys.exit(main())

