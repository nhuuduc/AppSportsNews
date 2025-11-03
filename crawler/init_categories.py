# -*- coding: utf-8 -*-
"""
Script khởi tạo categories tự động
Chạy script này nếu gặp lỗi foreign key constraint với category_id
"""

import mysql.connector
from config import DB_CONFIG
import sys

def init_categories():
    """Khởi tạo categories cơ bản"""
    
    # Format: (id, name, slug, description, icon_url, parent_id, is_active, display_order)
    categories = [
        (1, 'Bóng đá', 'bong-da', 'Tin tức bóng đá trong nước và quốc tế', None, None, 1, 1),
        (2, 'Bóng rổ', 'bong-ro', 'Tin tức NBA, bóng rổ quốc tế', None, None, 1, 2),
        (3, 'Quần vợt', 'quan-vot', 'Tin tức tennis, Grand Slam', None, None, 1, 3),
        (4, 'Võ thuật', 'vo-thuat', 'Boxing, MMA, UFC và các môn võ thuật', None, None, 1, 4),
        (5, 'Đua xe', 'dua-xe', 'F1, MotoGP và các giải đua xe', None, None, 1, 5),
        (6, 'Thể thao điện tử', 'the-thao-dien-tu', 'Esports, gaming thể thao', None, None, 1, 6),
        (7, 'Thể thao khác', 'the-thao-khac', 'Các môn thể thao khác', None, None, 1, 7)
    ]
    
    try:
        print("=" * 60)
        print("  KHỞI TẠO CATEGORIES - Sports News v2.0")
        print("=" * 60)
        print()
        
        # Kết nối database
        print(f"[INFO] Đang kết nối đến database: {DB_CONFIG['database']}...")
        connection = mysql.connector.connect(**DB_CONFIG)
        cursor = connection.cursor()
        
        print("[SUCCESS] Đã kết nối thành công!")
        print()
        
        # Kiểm tra categories hiện tại
        cursor.execute("SELECT COUNT(*) FROM categories WHERE is_active = 1")
        count = cursor.fetchone()[0]
        print(f"[INFO] Số categories hiện tại: {count}")
        print()
        
        # Thêm categories (khớp với schema API)
        insert_query = """
            INSERT INTO categories 
            (category_id, category_name, category_slug, description, icon_url, parent_id, is_active, display_order)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE 
                category_name = VALUES(category_name),
                description = VALUES(description),
                icon_url = VALUES(icon_url),
                parent_id = VALUES(parent_id),
                is_active = VALUES(is_active),
                display_order = VALUES(display_order)
        """
        
        added = 0
        updated = 0
        
        for category in categories:
            # Kiểm tra category đã tồn tại chưa
            cursor.execute("SELECT category_id FROM categories WHERE category_id = %s", (category[0],))
            exists = cursor.fetchone()
            
            cursor.execute(insert_query, category)
            
            if exists:
                updated += 1
                print(f"[UPDATE] {category[1]} (ID: {category[0]})")
            else:
                added += 1
                print(f"[ADD] {category[1]} (ID: {category[0]})")
        
        connection.commit()
        
        print()
        print("=" * 60)
        print(f"[SUCCESS] Hoàn tất!")
        print(f"  - Đã thêm mới: {added} categories")
        print(f"  - Đã cập nhật: {updated} categories")
        print("=" * 60)
        print()
        
        # Hiển thị danh sách categories
        cursor.execute("""
            SELECT category_id, category_name, category_slug 
            FROM categories 
            WHERE is_active = 1 
            ORDER BY display_order
        """)
        
        print("📁 DANH SÁCH CATEGORIES:")
        print("-" * 60)
        print(f"{'ID':<5} {'Tên Category':<25} {'Slug':<30}")
        print("-" * 60)
        
        for row in cursor.fetchall():
            print(f"{row[0]:<5} {row[1]:<25} {row[2]:<30}")
        
        print("-" * 60)
        print()
        
        cursor.close()
        connection.close()
        
        print("✅ Bạn có thể chạy crawler ngay bây giờ!")
        print()
        
        return True
        
    except mysql.connector.Error as e:
        print()
        print("=" * 60)
        print("[ERROR] Lỗi khi khởi tạo categories!")
        print("=" * 60)
        print(f"Chi tiết lỗi: {e}")
        print()
        print("Vui lòng kiểm tra:")
        print("1. MySQL đang chạy")
        print(f"2. Database '{DB_CONFIG['database']}' đã được tạo")
        print("3. Thông tin kết nối trong config.py đúng")
        print()
        return False
    
    except Exception as e:
        print()
        print("=" * 60)
        print("[ERROR] Lỗi không xác định!")
        print("=" * 60)
        print(f"Chi tiết lỗi: {e}")
        print()
        return False


if __name__ == "__main__":
    success = init_categories()
    sys.exit(0 if success else 1)
