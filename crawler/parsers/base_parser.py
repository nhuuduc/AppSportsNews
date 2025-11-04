# -*- coding: utf-8 -*-
"""
Base Parser - Lớp cơ sở cho các parser
"""

import requests
from bs4 import BeautifulSoup
import logging
import time
from slugify import slugify
from config import USER_AGENT, REQUEST_TIMEOUT, RETRY_TIMES, DELAY_BETWEEN_REQUESTS
from datetime import datetime
from urllib.parse import urljoin

logger = logging.getLogger(__name__)


class BaseParser:
    """Lớp cơ sở cho tất cả các parser"""
    
    def __init__(self, source_name, base_url):
        self.source_name = source_name
        self.base_url = base_url
        self.session = requests.Session()
        self.session.headers.update({'User-Agent': USER_AGENT})
    
    def get_page(self, url, retry=RETRY_TIMES):
        """Lấy nội dung trang web"""
        for attempt in range(retry):
            try:
                logger.info(f"📡 Đang tải: {url}")
                response = self.session.get(url, timeout=REQUEST_TIMEOUT)
                response.encoding = 'utf-8'
                
                if response.status_code == 200:
                    time.sleep(DELAY_BETWEEN_REQUESTS)
                    return response.text
                else:
                    logger.warning(f"⚠ HTTP {response.status_code}: {url}")
                    
            except Exception as e:
                logger.error(f"✗ Lỗi tải trang (lần {attempt + 1}/{retry}): {e}")
                if attempt < retry - 1:
                    time.sleep(5)
        
        return None
    
    def parse_soup(self, html):
        """Parse HTML thành BeautifulSoup object"""
        return BeautifulSoup(html, 'lxml')
    
    def clean_text(self, text):
        """Làm sạch text"""
        if not text:
            return ""
        return ' '.join(text.strip().split())
    
    def generate_slug(self, title):
        """Tạo slug từ tiêu đề (unique với timestamp)"""
        base_slug = slugify(title, separator='-')
        # Thêm timestamp chi tiết để đảm bảo unique (giống API PostController)
        timestamp = datetime.now().strftime('%Y%m%d%H%M%S')
        return f"{base_slug}-{timestamp}"
    
    def download_image(self, image_url, article_slug):
        """
        Trả về URL ảnh trực tiếp từ trang báo (không download)
        Xử lý URL VnExpress CDN và validate format
        """
        try:
            # Tạo absolute URL nếu là relative path
            if not image_url.startswith('http'):
                image_url = urljoin(self.base_url, image_url)
            
            # Validate URL
            if not image_url.startswith('http'):
                logger.warning(f"  ⚠ URL ảnh không hợp lệ: {image_url}")
                return None
            
            # Xử lý URL VnExpress CDN - đảm bảo URL đầy đủ
            # VnExpress CDN URLs thường có format: https://i1-thethao.vnecdn.net/YYYY/MM/DD/filename-timestamp
            # Giữ nguyên query parameters vì chúng có thể chứa thông tin resize/optimize
            if 'vnecdn.net' in image_url:
                # Giữ nguyên query parameters (không xóa)
                # Nếu URL không có extension trong filename, log để debug
                filename = image_url.split('?')[0].split('/')[-1]
                if '.' not in filename:
                    logger.debug(f"  ℹ URL CDN không có extension rõ ràng: {image_url[:60]}...")
            
            # Validate URL có vẻ hợp lệ
            logger.info(f"  ✓ Sử dụng URL ảnh gốc: {image_url[:60]}...")
            return image_url
                
        except Exception as e:
            logger.error(f"✗ Lỗi xử lý URL ảnh {image_url}: {e}")
        
        return None
    
    def process_content_images(self, content_html, article_slug):
        """
        Xử lý tất cả ảnh trong content HTML:
        - Tìm tất cả thẻ <img>
        - Chuyển đổi thành URL tuyệt đối (không download)
        - Thay thế src cũ bằng URL gốc
        
        Args:
            content_html: BeautifulSoup object hoặc string HTML
            article_slug: Slug của bài viết (để đặt tên file)
            
        Returns:
            HTML string đã được xử lý (với URL ảnh tuyệt đối)
        """
        try:
            # Parse HTML nếu là string
            if isinstance(content_html, str):
                soup = BeautifulSoup(content_html, 'lxml')
            else:
                soup = content_html
            
            # Tìm tất cả thẻ img
            img_tags = soup.find_all('img')
            
            if not img_tags:
                logger.info("ℹ Không có ảnh trong content")
                return str(soup)
            
            logger.info(f"🖼️ Tìm thấy {len(img_tags)} ảnh trong content, đang xử lý URL...")
            
            processed_count = 0
            for img in img_tags:
                # Lấy URL ảnh (thử nhiều thuộc tính)
                img_url = img.get('data-src') or img.get('src') or img.get('data-original')
                
                if not img_url:
                    continue
                
                # Chuyển thành URL tuyệt đối
                absolute_url = self.download_image(img_url, article_slug)
                
                if absolute_url:
                    # Thay thế URL cũ bằng URL gốc
                    img['src'] = absolute_url
                    
                    # Xóa các thuộc tính lazy load
                    if img.get('data-src'):
                        del img['data-src']
                    if img.get('data-original'):
                        del img['data-original']
                    
                    processed_count += 1
                else:
                    logger.warning(f"  ⚠ Không xử lý được: {img_url[:50]}...")
            
            logger.info(f"✓ Đã xử lý {processed_count}/{len(img_tags)} ảnh trong content")
            
            # Trả về HTML đã xử lý
            # Lấy body content (bỏ các thẻ html, body tự động thêm vào)
            body = soup.find('body')
            if body:
                return ''.join(str(child) for child in body.children)
            else:
                return str(soup)
            
        except Exception as e:
            logger.error(f"✗ Lỗi xử lý ảnh trong content: {e}")
            return str(content_html)
    
    def detect_category(self, title, content, url):
        """Phát hiện category từ nội dung (override trong subclass)"""
        from config import CATEGORY_MAPPING
        
        text = f"{title} {content} {url}".lower()
        
        for keyword, category_id in CATEGORY_MAPPING.items():
            if keyword in text:
                return category_id
        
        return 1  # Default: Bóng đá
    
    def extract_tags(self, title, content):
        """Trích xuất tags từ nội dung"""
        tags = []
        
        # Danh sách các keywords phổ biến trong bóng đá
        common_keywords = [
            'Premier League', 'La Liga', 'Serie A', 'Bundesliga',
            'Champions League', 'Europa League', 'World Cup',
            'Manchester United', 'Liverpool', 'Real Madrid', 'Barcelona',
            'Arsenal', 'Chelsea', 'Man City', 'PSG', 'Bayern Munich',
            'Messi', 'Ronaldo', 'Neymar', 'Mbappe',
            'V-League', 'AFF Cup', 'SEA Games',
            'Chuyển nhượng', 'Transfer', 'HLV', 'Coach'
        ]
        
        text = f"{title} {content}"
        
        for keyword in common_keywords:
            if keyword.lower() in text.lower():
                tags.append(keyword)
        
        return tags[:10]  # Giới hạn 10 tags
    
    def get_article_list(self):
        """Lấy danh sách bài viết (phải override trong subclass)"""
        raise NotImplementedError("Phải implement method get_article_list()")
    
    def parse_article(self, url):
        """Parse chi tiết một bài viết (phải override trong subclass)"""
        raise NotImplementedError("Phải implement method parse_article()")

