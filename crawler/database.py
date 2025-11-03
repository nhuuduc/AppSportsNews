# -*- coding: utf-8 -*-
"""
Database Handler - Xử lý kết nối và thao tác với database
"""

import mysql.connector
from mysql.connector import Error
from config import DB_CONFIG
import logging
from datetime import datetime
from slugify import slugify

logger = logging.getLogger(__name__)


class DatabaseHandler:
    """Xử lý tất cả các thao tác với database"""
    
    def __init__(self):
        self.connection = None
        self.connect()
    
    def connect(self):
        """Kết nối đến database"""
        try:
            self.connection = mysql.connector.connect(**DB_CONFIG)
            if self.connection.is_connected():

                logger.info(f"✓ Đã kết nối đến database thành công: {DB_CONFIG}")
                return True
        except Error as e:
            logger.error(f"✗ Lỗi kết nối database: {e}")
            return False
    
    def close(self):
        """Đóng kết nối database"""
        if self.connection and self.connection.is_connected():
            self.connection.close()
            logger.info("✓ Đã đóng kết nối database")
    
    def article_exists(self, slug):
        """Kiểm tra bài viết đã tồn tại chưa (theo slug)"""
        try:
            cursor = self.connection.cursor()
            query = "SELECT article_id FROM articles WHERE slug = %s LIMIT 1"
            cursor.execute(query, (slug,))
            result = cursor.fetchone()
            cursor.close()
            return result is not None
        except Error as e:
            logger.error(f"✗ Lỗi kiểm tra bài viết: {e}")
            return False
    
    def get_or_create_category(self, category_name):
        """Lấy hoặc tạo category mới"""
        try:
            cursor = self.connection.cursor()
            
            # Kiểm tra category đã tồn tại
            query = "SELECT category_id FROM categories WHERE category_name = %s LIMIT 1"
            cursor.execute(query, (category_name,))
            result = cursor.fetchone()
            
            if result:
                cursor.close()
                return result[0]
            
            # Tạo category mới
            slug = slugify(category_name, separator='-')
            insert_query = """
                INSERT INTO categories (category_name, category_slug, is_active)
                VALUES (%s, %s, 1)
            """
            cursor.execute(insert_query, (category_name, slug))
            self.connection.commit()
            category_id = cursor.lastrowid
            cursor.close()
            
            logger.info(f"✓ Đã tạo category mới: {category_name} (ID: {category_id})")
            return category_id
            
        except Error as e:
            logger.error(f"✗ Lỗi xử lý category: {e}")
            return None
    
    def get_or_create_tag(self, tag_name):
        """Lấy hoặc tạo tag mới"""
        try:
            cursor = self.connection.cursor()
            
            # Kiểm tra tag đã tồn tại
            query = "SELECT tag_id FROM tags WHERE tag_name = %s LIMIT 1"
            cursor.execute(query, (tag_name,))
            result = cursor.fetchone()
            
            if result:
                cursor.close()
                return result[0]
            
            # Tạo tag mới
            slug = slugify(tag_name, separator='-')
            insert_query = """
                INSERT INTO tags (tag_name, tag_slug)
                VALUES (%s, %s)
            """
            cursor.execute(insert_query, (tag_name, slug))
            self.connection.commit()
            tag_id = cursor.lastrowid
            cursor.close()
            
            return tag_id
            
        except Error as e:
            logger.error(f"✗ Lỗi xử lý tag: {e}")
            return None
    
    def insert_article(self, article_data):
        """Thêm bài viết mới vào database"""
        try:
            # Kiểm tra trùng lặp
            if self.article_exists(article_data['slug']):
                logger.warning(f"⚠ Bài viết đã tồn tại: {article_data['title']}")
                return None
            
            cursor = self.connection.cursor()
            
            # Thêm source_url để tracking nguồn tin
            insert_query = """
                INSERT INTO articles 
                (title, slug, summary, content, thumbnail_url, category_id, 
                 author_id, is_featured, is_breaking_news, status, published_at, created_at)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW())
            """
            
            values = (
                article_data['title'],
                article_data['slug'],
                article_data.get('summary', ''),
                article_data['content'],
                article_data.get('thumbnail_url', ''),
                article_data['category_id'],
                article_data['author_id'],
                article_data.get('is_featured', 0),
                article_data.get('is_breaking_news', 0),
                article_data.get('status', 'published'),
                article_data.get('published_at', datetime.now())
            )
            
            cursor.execute(insert_query, values)
            article_id = cursor.lastrowid
            
            # Khởi tạo article_views cho bài viết mới (phù hợp với API)
            views_query = """
                INSERT INTO article_views (article_id, view_count, like_count, comment_count, liked_user_ids)
                VALUES (%s, 0, 0, 0, '')
                ON DUPLICATE KEY UPDATE article_id = article_id
            """
            cursor.execute(views_query, (article_id,))
            
            self.connection.commit()
            cursor.close()
            
            logger.info(f"✓ Đã thêm bài viết: {article_data['title']} (ID: {article_id})")
            return article_id
            
        except Error as e:
            logger.error(f"✗ Lỗi thêm bài viết: {e}")
            self.connection.rollback()
            return None
    
    def insert_article_tags(self, article_id, tags):
        """Thêm tags cho bài viết"""
        try:
            cursor = self.connection.cursor()
            
            for tag_name in tags:
                tag_id = self.get_or_create_tag(tag_name)
                if tag_id:
                    # Kiểm tra đã có chưa
                    check_query = """
                        SELECT 1 FROM article_tags 
                        WHERE article_id = %s AND tag_id = %s
                    """
                    cursor.execute(check_query, (article_id, tag_id))
                    if not cursor.fetchone():
                        insert_query = """
                            INSERT INTO article_tags (article_id, tag_id)
                            VALUES (%s, %s)
                        """
                        cursor.execute(insert_query, (article_id, tag_id))
            
            self.connection.commit()
            cursor.close()
            logger.info(f"✓ Đã thêm {len(tags)} tags cho bài viết ID: {article_id}")
            
        except Error as e:
            logger.error(f"✗ Lỗi thêm tags: {e}")
            self.connection.rollback()
    
    def insert_article_images(self, article_id, images):
        """Thêm hình ảnh cho bài viết"""
        try:
            cursor = self.connection.cursor()
            
            for idx, image_data in enumerate(images):
                insert_query = """
                    INSERT INTO article_images 
                    (article_id, image_url, caption, display_order)
                    VALUES (%s, %s, %s, %s)
                """
                values = (
                    article_id,
                    image_data.get('url', ''),
                    image_data.get('caption', ''),
                    idx
                )
                cursor.execute(insert_query, values)
            
            self.connection.commit()
            cursor.close()
            logger.info(f"✓ Đã thêm {len(images)} hình ảnh cho bài viết ID: {article_id}")
            
        except Error as e:
            logger.error(f"✗ Lỗi thêm hình ảnh: {e}")
            self.connection.rollback()
    
    def get_statistics(self):
        """Lấy thống kê database"""
        try:
            cursor = self.connection.cursor(dictionary=True)
            
            stats = {}
            
            # Tổng số bài viết
            cursor.execute("SELECT COUNT(*) as total FROM articles")
            stats['total_articles'] = cursor.fetchone()['total']
            
            # Bài viết theo status
            cursor.execute("""
                SELECT status, COUNT(*) as count 
                FROM articles 
                GROUP BY status
            """)
            stats['by_status'] = cursor.fetchall()
            
            # Tổng số categories
            cursor.execute("SELECT COUNT(*) as total FROM categories WHERE is_active = 1")
            stats['total_categories'] = cursor.fetchone()['total']
            
            # Tổng số tags
            cursor.execute("SELECT COUNT(*) as total FROM tags")
            stats['total_tags'] = cursor.fetchone()['total']
            
            cursor.close()
            return stats
            
        except Error as e:
            logger.error(f"✗ Lỗi lấy thống kê: {e}")
            return None

