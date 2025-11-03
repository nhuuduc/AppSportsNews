# -*- coding: utf-8 -*-
"""
VnExpress Parser - Parser cho VnExpress Thể Thao
"""

from parsers.base_parser import BaseParser
import logging
from datetime import datetime
from config import DEFAULT_AUTHOR_ID

logger = logging.getLogger(__name__)


class VnExpressParser(BaseParser):
    """Parser cho VnExpress Thể Thao"""
    
    def __init__(self):
        super().__init__('VnExpress', 'https://vnexpress.net/the-thao')
    
    def get_article_list(self, limit=10):
        """Lấy danh sách bài viết từ trang chủ"""
        html = self.get_page(self.base_url)
        if not html:
            return []
        
        soup = self.parse_soup(html)
        articles = []
        
        # Tìm các bài viết
        article_items = soup.select('.item-news')[:limit]
        
        for item in article_items:
            try:
                title_tag = item.select_one('.title-news a')
                if not title_tag:
                    continue
                
                title = self.clean_text(title_tag.get_text())
                url = title_tag.get('href', '')
                
                # Đảm bảo URL đầy đủ
                if not url.startswith('http'):
                    url = f"https://vnexpress.net{url}"
                
                # Lấy thumbnail nếu có
                thumb_tag = item.select_one('img')
                thumbnail = thumb_tag.get('data-src') or thumb_tag.get('src') if thumb_tag else ''
                
                # Lấy mô tả
                desc_tag = item.select_one('.description')
                description = self.clean_text(desc_tag.get_text()) if desc_tag else ''
                
                articles.append({
                    'title': title,
                    'url': url,
                    'thumbnail': thumbnail,
                    'description': description
                })
                
            except Exception as e:
                logger.error(f"✗ Lỗi parse article item: {e}")
                continue
        
        logger.info(f"✓ Tìm thấy {len(articles)} bài viết từ VnExpress")
        return articles
    
    def parse_article(self, url):
        """Parse chi tiết một bài viết"""
        try:
            html = self.get_page(url)
            if not html:
                return None
            
            soup = self.parse_soup(html)
            
            # Lấy tiêu đề
            title_tag = soup.select_one('h1.title-detail')
            if not title_tag:
                logger.warning(f"⚠ Không tìm thấy tiêu đề: {url}")
                return None
            
            title = self.clean_text(title_tag.get_text())
            
            # Lấy mô tả
            desc_tag = soup.select_one('.description')
            summary = self.clean_text(desc_tag.get_text()) if desc_tag else ''
            
            # Lấy nội dung
            content_tag = soup.select_one('.fck_detail')
            if not content_tag:
                logger.warning(f"⚠ Không tìm thấy nội dung: {url}")
                return None
            
            # Tạo slug trước (cần cho download ảnh)
            slug = self.generate_slug(title)
            
            # Xử lý ảnh trong content HTML
            # Download tất cả ảnh và thay thế URL trong HTML
            content_html = self.process_content_images(content_tag, slug)
            
            if not content_html or len(content_html) < 50:
                logger.warning(f"⚠ Nội dung rỗng: {url}")
                return None
            
            # Lấy thumbnail
            thumb_tag = soup.select_one('.fig-picture img')
            thumbnail_url = ''
            if thumb_tag:
                thumbnail_url = thumb_tag.get('data-src') or thumb_tag.get('src') or ''
            
            # Download thumbnail
            if thumbnail_url:
                local_thumb = self.download_image(thumbnail_url, slug)
                if local_thumb:
                    thumbnail_url = local_thumb
            
            # Lấy text từ content để phân tích category và tags
            content_text = content_tag.get_text()
            
            # Phát hiện category
            category_id = self.detect_category(title, content_text, url)
            
            # Trích xuất tags
            tags = self.extract_tags(title, content_text)
            
            # Lấy ngày đăng
            time_tag = soup.select_one('.date')
            published_at = datetime.now()
            if time_tag:
                try:
                    time_text = self.clean_text(time_tag.get_text())
                    # Parse time từ format VnExpress
                    # TODO: Implement proper date parsing
                except:
                    pass
            
            article_data = {
                'title': title,
                'slug': slug,
                'summary': summary[:500] if summary else title[:200],  # Fallback nếu không có summary
                'content': content_html,  # Lưu HTML với ảnh đã được xử lý
                'thumbnail_url': thumbnail_url,
                'category_id': category_id,
                'author_id': DEFAULT_AUTHOR_ID,
                'status': 'published',
                'published_at': published_at,
                'tags': tags,
                'source_url': url,  # Tracking nguồn tin (để tránh duplicate)
                'is_featured': 0,  # Crawler không tự đánh dấu featured
                'is_breaking_news': 0,  # Crawler không tự đánh dấu breaking news
                'images': []  # Danh sách ảnh trong bài (nếu cần)
            }
            
            logger.info(f"✓ Parse thành công: {title}")
            return article_data
            
        except Exception as e:
            logger.error(f"✗ Lỗi parse article {url}: {e}")
            return None

