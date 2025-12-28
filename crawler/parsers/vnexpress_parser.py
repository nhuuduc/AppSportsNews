# -*- coding: utf-8 -*-
"""
VnExpress Parser - Parser cho VnExpress Thể Thao
"""

from parsers.base_parser import BaseParser
import logging
from datetime import datetime
from urllib.parse import urljoin
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
        
        # Tìm các bài viết - thử nhiều selector để tương thích với cấu trúc mới
        article_items = []
        
        # Thử các selector mới trước
        selectors = [
            '.item-news',
            '.article-item',
            '.story-item',
            'article.item-news',
            'article.article-item',
            '.thumb-art',
            '.list-news-subfolder .item-news',
            '.container .item-news'
        ]
        
        for selector in selectors:
            article_items = soup.select(selector)
            if article_items:
                logger.info(f"✓ Tìm thấy {len(article_items)} bài viết với selector: {selector}")
                break
        
        if not article_items:
            logger.warning("⚠ Không tìm thấy bài viết với bất kỳ selector nào")
            return []
        
        article_items = article_items[:limit]
        
        for item in article_items:
            try:
                # Thử nhiều selector cho title
                title_tag = None
                title_selectors = [
                    '.title-news a',
                    'h3.title-news a',
                    'h2.title-news a',
                    '.title a',
                    'a.title-news',
                    'h3 a',
                    'h2 a',
                    'a[href*="/the-thao/"]'
                ]
                
                for selector in title_selectors:
                    title_tag = item.select_one(selector)
                    if title_tag:
                        break
                
                if not title_tag:
                    # Thử tìm thẻ a trực tiếp
                    title_tag = item.find('a', href=True)
                
                if not title_tag:
                    continue
                
                title = self.clean_text(title_tag.get_text())
                url = title_tag.get('href', '')
                
                # Đảm bảo URL đầy đủ
                if not url.startswith('http'):
                    url = f"https://vnexpress.net{url}"
                
                # Lấy thumbnail nếu có - thử nhiều selector
                thumbnail = ''
                thumb_selectors = ['img', '.thumb img', 'picture img', '.thumb-art img']
                for selector in thumb_selectors:
                    thumb_tag = item.select_one(selector)
                    if thumb_tag:
                        thumbnail = (thumb_tag.get('data-src') or 
                                   thumb_tag.get('data-original') or 
                                   thumb_tag.get('src') or 
                                   thumb_tag.get('data-lazy-src') or '')
                        if thumbnail:
                            break
                
                # Lấy mô tả - thử nhiều selector
                description = ''
                desc_selectors = ['.description', '.sapo', '.lead', '.summary', 'p.description']
                for selector in desc_selectors:
                    desc_tag = item.select_one(selector)
                    if desc_tag:
                        description = self.clean_text(desc_tag.get_text())
                        if description:
                            break
                
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
            
            # Lấy tiêu đề - thử nhiều selector
            title_tag = None
            title_selectors = [
                'h1.title-detail',
                'h1.title-news',
                'h1.article-title',
                'h1',
                '.title-detail',
                '.article-title'
            ]
            
            for selector in title_selectors:
                title_tag = soup.select_one(selector)
                if title_tag:
                    break
            
            if not title_tag:
                logger.warning(f"⚠ Không tìm thấy tiêu đề: {url}")
                return None
            
            title = self.clean_text(title_tag.get_text())
            
            # Lấy mô tả - thử nhiều selector
            desc_tag = None
            desc_selectors = [
                '.description',
                '.sapo',
                '.lead',
                '.article-summary',
                'p.description',
                '.article-lead'
            ]
            
            for selector in desc_selectors:
                desc_tag = soup.select_one(selector)
                if desc_tag:
                    break
            
            summary = self.clean_text(desc_tag.get_text()) if desc_tag else ''
            
            # Lấy nội dung - thử nhiều selector
            content_tag = None
            content_selectors = [
                '.fck_detail',
                '.Normal',
                '.article-body',
                '.article-content',
                '.content-detail',
                '.article-body-content',
                '[class*="fck"]',
                '[class*="Normal"]'
            ]
            
            for selector in content_selectors:
                content_tag = soup.select_one(selector)
                if content_tag:
                    logger.info(f"✓ Tìm thấy nội dung với selector: {selector}")
                    break
            
            if not content_tag:
                logger.warning(f"⚠ Không tìm thấy nội dung: {url}")
                return None
            
            # Tạo slug trước (cần cho download ảnh)
            slug = self.generate_slug(title)
            
            # Thu thập danh sách ảnh từ content trước khi xử lý
            images_list = []
            img_tags = content_tag.find_all('img')
            for img in img_tags:
                # Thử nhiều thuộc tính để lấy URL ảnh (ưu tiên data-src cho lazy load)
                img_url = (img.get('data-src') or 
                          img.get('data-original') or 
                          img.get('src') or 
                          img.get('data-lazy-src'))
                
                if img_url:
                    # Chuyển thành URL tuyệt đối nếu cần
                    if not img_url.startswith('http'):
                        img_url = urljoin('https://vnexpress.net', img_url)
                    
                    # Loại bỏ các ký tự không hợp lệ và làm sạch URL
                    # Giữ nguyên query parameters vì CDN có thể cần chúng
                    img_url = img_url.strip()
                    
                    # Validate URL hợp lệ
                    if not img_url.startswith('http'):
                        logger.warning(f"  ⚠ Bỏ qua URL ảnh không hợp lệ: {img_url[:50]}...")
                        continue
                    
                    # Lấy caption nếu có
                    caption = ''
                    # Tìm caption trong thẻ figure hoặc từ alt
                    figure = img.find_parent('figure')
                    if figure:
                        caption_tag = figure.find('figcaption')
                        if caption_tag:
                            caption = self.clean_text(caption_tag.get_text())
                    if not caption:
                        caption = img.get('alt', '') or img.get('title', '')
                    
                    images_list.append({
                        'url': img_url,
                        'caption': caption[:500] if caption else ''  # Giới hạn độ dài caption
                    })
            
            # Xử lý ảnh trong content HTML
            # Download tất cả ảnh và thay thế URL trong HTML
            content_html = self.process_content_images(content_tag, slug)
            
            if not content_html or len(content_html) < 50:
                logger.warning(f"⚠ Nội dung rỗng: {url}")
                return None
            
            # Lấy thumbnail - thử nhiều selector
            thumb_tag = None
            thumb_selectors = [
                '.fig-picture img',
                '.fig-picture picture img',
                '.article-thumb img',
                '.article-image img',
                'picture img',
                '.container-figure img',
                'figure img'
            ]
            
            for selector in thumb_selectors:
                thumb_tag = soup.select_one(selector)
                if thumb_tag:
                    break
            
            thumbnail_url = ''
            if thumb_tag:
                thumbnail_url = (thumb_tag.get('data-src') or 
                               thumb_tag.get('data-original') or 
                               thumb_tag.get('src') or 
                               thumb_tag.get('data-lazy-src') or '')
            
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
            
            # Lấy ngày đăng - thử nhiều selector
            time_tag = None
            time_selectors = [
                '.date',
                '.header-content .date',
                '.article-date',
                'time',
                '[datetime]',
                '.date-time',
                '.article-time'
            ]
            
            for selector in time_selectors:
                time_tag = soup.select_one(selector)
                if time_tag:
                    break
            
            published_at = datetime.now()
            if time_tag:
                try:
                    # Thử lấy từ attribute datetime trước
                    datetime_attr = time_tag.get('datetime')
                    if datetime_attr:
                        # Parse ISO format: 2024-01-01T10:00:00+07:00 hoặc 2024-01-01T10:00:00
                        try:
                            # Thử parse ISO format đơn giản
                            if 'T' in datetime_attr:
                                date_part, time_part = datetime_attr.split('T')
                                year, month, day = date_part.split('-')
                                time_part = time_part.split('+')[0].split('-')[0]  # Bỏ timezone
                                hour, minute, second = time_part.split(':')
                                published_at = datetime(
                                    int(year), int(month), int(day),
                                    int(hour), int(minute), int(second.split('.')[0]) if '.' in second else int(second)
                                )
                        except:
                            pass
                    else:
                        time_text = self.clean_text(time_tag.get_text())
                        # Parse time từ format VnExpress
                        # TODO: Implement proper date parsing
                except Exception as e:
                    logger.debug(f"Không thể parse ngày đăng: {e}")
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
                'images': images_list  # Danh sách ảnh trong bài
            }
            
            logger.info(f"✓ Parse thành công: {title}")
            return article_data
            
        except Exception as e:
            logger.error(f"✗ Lỗi parse article {url}: {e}")
            return None

