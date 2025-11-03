# -*- coding: utf-8 -*-
"""
Match Parser - Parser cho các trận đấu sắp diễn ra
"""

from parsers.base_parser import BaseParser
import logging
from datetime import datetime, timedelta
import re
import time
from urllib.parse import urljoin
from config import CATEGORY_MAPPING, PAGE_LOAD_DELAY

logger = logging.getLogger(__name__)


class VnExpressMatchParser(BaseParser):
    """Parser cho lịch thi đấu VnExpress"""
    
    def __init__(self):
        super().__init__('VnExpress Matches', 'https://vnexpress.net/the-thao/ngoai-hang-anh/lich-thi-dau')
    
    def get_tournament_links(self):
        """Lấy danh sách các link giải đấu từ trang chính"""
        html = self.get_page(self.base_url)
        if not html:
            return []
        
        soup = self.parse_soup(html)
        tournament_links = []
        
        # Tìm các link đến trang giải đấu
        # Pattern: /the-thao/du-lieu-bong-da/giai-dau/...
        links = soup.select('a[href*="/du-lieu-bong-da/giai-dau/"]')
        
        seen_urls = set()
        for link in links:
            href = link.get('href', '')
            if not href:
                continue
            
            # Tạo absolute URL
            if href.startswith('/'):
                href = 'https://vnexpress.net' + href
            elif not href.startswith('http'):
                continue
            
            # Bỏ qua các URL trùng lặp
            if href in seen_urls:
                continue
            seen_urls.add(href)
            
            # Lấy tên giải đấu từ text hoặc từ URL
            tournament_name = self.clean_text(link.get_text())
            if not tournament_name or len(tournament_name) < 3:
                # Lấy từ URL nếu không có text
                parts = href.split('/')
                if len(parts) > 0:
                    tournament_name = parts[-1].replace('-', ' ').title()
            
            tournament_links.append({
                'url': href,
                'name': tournament_name
            })
        
        logger.info(f"✓ Tìm thấy {len(tournament_links)} giải đấu từ VnExpress")
        return tournament_links
    
    def get_upcoming_matches(self, limit=50, days_range=None):
        """Lấy danh sách các trận đấu sắp diễn ra"""
        matches = []
        
        # Parse từ trang Ngoại Hạng Anh
        logger.info(f"📥 Đang tải trang: {self.base_url}")
        html = self.get_page(self.base_url)
        if not html:
            logger.warning("⚠ Không thể lấy HTML từ trang")
            return []
        
        soup = self.parse_soup(html)
        logger.info(f"✓ Đã parse HTML thành công")
        
        # Thử tìm lịch thi đấu trực tiếp trong trang (widget, table, etc.)
        match_containers = soup.select(
            '.schedule-table, .match-list, .fixture-list, '
            '.list-match, table.schedule, .match-schedule, '
            '[class*="schedule"], [class*="fixture"], [class*="match-list"]'
        )
        
        if match_containers:
            logger.info(f"✓ Tìm thấy {len(match_containers)} container chứa lịch thi đấu")
            for container in match_containers:
                container_matches = self.extract_matches_from_text(
                    container.get_text(), 
                    'Ngoại Hạng Anh'
                )
                if container_matches:
                    logger.info(f"  ✓ Parse được {len(container_matches)} trận từ container")
                matches.extend(container_matches)
        
        # Parse từ các bài viết - THỬ TẤT CẢ các bài viết, không chỉ những bài có từ khóa
        article_items = soup.select('.item-news, article, .news-item, .list_news li, .item_normal, .item-news-common')[:limit * 3]
        logger.info(f"✓ Tìm thấy {len(article_items)} bài viết để parse")
        
        articles_checked = 0
        for item in article_items:
            try:
                title_tag = item.select_one('.title-news a, h3 a, .title a, a.title, a')
                if not title_tag:
                    continue
                
                title = self.clean_text(title_tag.get_text())
                url = title_tag.get('href', '')
                
                if not url or not title:
                    continue
                
                # Tạo absolute URL
                if url.startswith('/'):
                    url = 'https://vnexpress.net' + url
                elif not url.startswith('http'):
                    continue
                
                articles_checked += 1
                
                # Parse từ title trước (nhanh hơn)
                title_matches = self.extract_matches_from_text(title, 'Ngoại Hạng Anh')
                if title_matches:
                    logger.info(f"  ✓ Tìm thấy {len(title_matches)} trận trong title: {title[:50]}...")
                    matches.extend(title_matches)
                    if len(matches) >= limit:
                        break
                
                # Kiểm tra nếu là bài về lịch thi đấu hoặc có từ khóa liên quan
                title_lower = title.lower()
                keywords = [
                    'lịch thi đấu', 'lịch đấu', 'fixture', 'schedule',
                    'vs', 'đấu', 'gặp', 'match', 'premier league',
                    'ngoại hạng anh', 'vòng', 'round', 'lịch', 'vòng đấu'
                ]
                
                if any(keyword in title_lower for keyword in keywords):
                    match_data = self.parse_match_from_article(url, title)
                    if match_data:
                        if isinstance(match_data, list):
                            logger.info(f"  ✓ Parse được {len(match_data)} trận từ bài: {title[:50]}...")
                            matches.extend(match_data)
                        else:
                            logger.info(f"  ✓ Parse được 1 trận từ bài: {title[:50]}...")
                            matches.append(match_data)
                        
                        if len(matches) >= limit:
                            break
                        
            except Exception as e:
                logger.debug(f"  ⚠ Lỗi parse article: {e}")
                continue
        
        logger.info(f"✓ Đã kiểm tra {articles_checked} bài viết")
        
        # Nếu vẫn chưa đủ, thử parse từ toàn bộ nội dung trang
        if len(matches) < limit:
            logger.info("📄 Đang parse từ toàn bộ nội dung trang...")
            page_text = soup.get_text()
            text_matches = self.extract_matches_from_text(page_text, 'Ngoại Hạng Anh')
            if text_matches:
                logger.info(f"  ✓ Parse được {len(text_matches)} trận từ nội dung trang")
            matches.extend(text_matches)
        
        # Loại bỏ trùng lặp dựa trên home_team và away_team
        seen = set()
        unique_matches = []
        for match in matches:
            key = (match.get('home_team_name', '').lower(), match.get('away_team_name', '').lower())
            if key not in seen and key[0] and key[1]:
                seen.add(key)
                unique_matches.append(match)
        matches = unique_matches
        
        # Filter theo days_range nếu có
        if days_range and matches:
            start_date = datetime.now() - timedelta(days=days_range[0])
            end_date = datetime.now() + timedelta(days=days_range[1])
            filtered_matches = []
            for match in matches:
                match_date = match.get('match_date')
                if isinstance(match_date, datetime):
                    if start_date <= match_date <= end_date:
                        filtered_matches.append(match)
                else:
                    # Nếu không có ngày, giữ lại (sẽ dùng default date)
                    filtered_matches.append(match)
            matches = filtered_matches
        
        logger.info(f"✓ Tổng cộng tìm thấy {len(matches)} trận đấu từ VnExpress")
        return matches[:limit]
    
    def parse_tournament_matches(self, tournament_url, tournament_name, limit=10):
        """Parse lịch thi đấu từ trang giải đấu cụ thể"""
        html = self.get_page(tournament_url)
        if not html:
            return []
        
        soup = self.parse_soup(html)
        matches = []
        
        # Tìm các phần tử có thể chứa thông tin trận đấu
        # Thử nhiều selector khác nhau
        match_selectors = [
            '.match', '.fixture', '.schedule-item', '.match-item',
            '[class*="match"]', '[class*="fixture"]', '[class*="schedule"]',
            'table tr', '.list-match li', '.match-list .item'
        ]
        
        match_elements = []
        for selector in match_selectors:
            elements = soup.select(selector)
            if elements:
                match_elements = elements
                logger.info(f"  ✓ Tìm thấy {len(elements)} phần tử với selector: {selector}")
                break
        
        # Nếu không tìm thấy phần tử match cụ thể, parse từ toàn bộ nội dung
        if not match_elements:
            # Tìm các pattern trong text
            content_text = soup.get_text()
            matches_found = self.extract_matches_from_text(content_text, tournament_name)
            return matches_found[:limit]
        
        # Parse từ các phần tử match
        for element in match_elements[:limit]:
            try:
                match_data = self.parse_match_element(element, tournament_name)
                if match_data:
                    matches.append(match_data)
            except Exception as e:
                logger.debug(f"  ⚠ Không parse được element: {e}")
                continue
        
        return matches[:limit]
    
    def is_valid_team_name(self, name):
        """Kiểm tra xem tên có phải là tên đội bóng hợp lệ không"""
        if not name or len(name.strip()) < 2:
            return False
        
        name = name.strip()
        name_lower = name.lower()
        
        # Loại bỏ các từ không phải tên đội (strict hơn)
        invalid_keywords = [
            'vnexpress', 'thể thao', 'lịch thi đấu', 'lịch đấu', 'mới nhất',
            'tin tức', 'kết quả', 'bảng xếp hạng', 'chân dung', 'phân tích',
            'hôm nay', 'ngày mai', 'cuộc', 'trận', 'đấu', 'gặp', 'và', 'hoặc',
            'xem', 'video', 'ảnh', 'clip', 'highlight', 'tổng hợp'
        ]
        
        # Kiểm tra các từ không hợp lệ (chỉ reject nếu là từ đơn lẻ hoặc chứa từ đầu)
        for keyword in invalid_keywords:
            if name_lower == keyword or name_lower.startswith(keyword + ' ') or name_lower.endswith(' ' + keyword):
                return False
        
        # Tên đội thường không quá dài
        words = name.split()
        if len(words) > 6:  # Cho phép tên đội dài hơn một chút
            return False
        
        # Tên đội không nên chỉ là số hoặc ký tự đặc biệt
        if name.replace(' ', '').replace('.', '').replace('-', '').isdigit():
            return False
        
        # Tên đội phải có ít nhất 1 chữ cái
        if not any(c.isalpha() for c in name):
            return False
        
        return True
    
    def parse_match_element(self, element, tournament_name):
        """Parse một phần tử match thành dict"""
        text = element.get_text()
        current_year = datetime.now().year
        
        # Tìm pattern: "Team A vs Team B" hoặc "Team A - Team B"
        # Cải thiện pattern để match tốt hơn
        match_pattern = r'([A-Za-zÀ-ỹ][A-Za-zÀ-ỹ\s\-\.]{1,30}?)\s*(?:vs|v\.s|đấu|gặp|[-–—])\s*([A-Za-zÀ-ỹ][A-Za-zÀ-ỹ\s\-\.]{1,30}?)'
        match = re.search(match_pattern, text, re.IGNORECASE)
        
        if not match:
            return None
        
        home_team = self.clean_text(match.group(1))
        away_team = self.clean_text(match.group(2))
        
        # Validate tên đội
        if not self.is_valid_team_name(home_team) or not self.is_valid_team_name(away_team):
            return None
        
        # Tìm ngày giờ trong text
        match_date = self.extract_match_date(text, current_year)
        
        # Tìm logo đội (nếu có)
        home_logo = None
        away_logo = None
        imgs = element.select('img')
        if len(imgs) >= 2:
            home_logo = imgs[0].get('src') or imgs[0].get('data-src')
            away_logo = imgs[1].get('src') or imgs[1].get('data-src')
        
        return {
            'home_team_name': home_team,
            'away_team_name': away_team,
            'home_team_logo': home_logo,
            'away_team_logo': away_logo,
            'match_date': match_date,
            'tournament_name': tournament_name,
            'category_id': self.detect_category_from_tournament(tournament_name),
            'status': 'scheduled'
        }
    
    def extract_matches_from_text(self, text, tournament_name, current_year=None):
        """Trích xuất các trận đấu từ text bằng regex"""
        matches = []
        
        if current_year is None:
            current_year = datetime.now().year
        
        if not text or len(text.strip()) < 10:
            return matches
        
        # Pattern: "Team A vs Team B" hoặc "Team A - Team B"
        # Cải thiện pattern để match tốt hơn với tên đội Premier League
        # Cho phép số trong tên đội (như "Man City", "Man Utd")
        match_patterns = [
            # Pattern chính: Team vs Team
            r'([A-Za-zÀ-ỹ][A-Za-zÀ-ỹ0-9\s\-\.]{1,35}?)\s+(?:vs|v\.s|đấu|gặp|v|VS)\s+([A-Za-zÀ-ỹ][A-Za-zÀ-ỹ0-9\s\-\.]{1,35}?)',
            # Pattern với dấu gạch ngang
            r'([A-Za-zÀ-ỹ][A-Za-zÀ-ỹ0-9\s\-\.]{1,35}?)\s*[-–—]\s*([A-Za-zÀ-ỹ][A-Za-zÀ-ỹ0-9\s\-\.]{1,35}?)',
        ]
        
        found_matches = []
        for pattern in match_patterns:
            matches_found = re.findall(pattern, text, re.IGNORECASE)
            found_matches.extend(matches_found)
        
        # Loại bỏ trùng lặp
        seen_pairs = set()
        unique_matches = []
        for match in found_matches:
            pair = (match[0].strip().lower(), match[1].strip().lower())
            if pair not in seen_pairs:
                seen_pairs.add(pair)
                unique_matches.append(match)
        
        for match in unique_matches[:30]:  # Tăng giới hạn lên 30 trận
            home_team = self.clean_text(match[0])
            away_team = self.clean_text(match[1])
            
            # Loại bỏ các từ không hợp lệ ở đầu/cuối
            home_team = home_team.strip(' .,;:!?()[]{}"\'-–—')
            away_team = away_team.strip(' .,;:!?()[]{}"\'-–—')
            
            # Validate tên đội
            if not self.is_valid_team_name(home_team) or not self.is_valid_team_name(away_team):
                continue
            
            # Tìm ngày giờ gần nhất
            match_date = self.extract_match_date(text, current_year)
            
            matches.append({
                'home_team_name': home_team,
                'away_team_name': away_team,
                'match_date': match_date,
                'tournament_name': tournament_name,
                'category_id': self.detect_category_from_tournament(tournament_name),
                'status': 'scheduled'
            })
        
        return matches
    
    def detect_category_from_tournament(self, tournament_name):
        """Phát hiện category từ tên giải đấu"""
        name_lower = tournament_name.lower()
        
        # Mapping các giải đấu
        if any(x in name_lower for x in ['premier', 'ngoại hạng anh', 'fa cup', 'cup liên đoàn']):
            return 1  # Bóng đá
        elif any(x in name_lower for x in ['champion', 'europa', 'c1', 'c2']):
            return 1  # Bóng đá
        elif any(x in name_lower for x in ['la liga', 'laliga', 'cup nhà vua']):
            return 1  # Bóng đá
        elif any(x in name_lower for x in ['bundesliga', 'cup qg đức']):
            return 1  # Bóng đá
        elif any(x in name_lower for x in ['serie a', 'cup qg italy']):
            return 1  # Bóng đá
        elif any(x in name_lower for x in ['ligue', 'cup qg pháp']):
            return 1  # Bóng đá
        elif any(x in name_lower for x in ['v-league', 'vleague']):
            return 1  # Bóng đá
        
        return 1  # Default: Bóng đá
    
    def parse_match_from_article(self, url, title):
        """Parse trận đấu từ bài viết về lịch thi đấu"""
        try:
            html = self.get_page(url)
            if not html:
                return None
            
            soup = self.parse_soup(html)
            content_text = soup.get_text()
            
            # Tìm các trận đấu trong nội dung
            # Pattern: "Team A vs Team B" hoặc "Team A - Team B"
            matches = []
            
            # Tìm ngày giờ trong bài viết
            date_patterns = [
                r'(\d{1,2})[\/\-](\d{1,2})[\/\-](\d{4})',  # dd/mm/yyyy
                r'(\d{4})[\/\-](\d{1,2})[\/\-](\d{1,2})',  # yyyy/mm/dd
            ]
            
            time_patterns = [
                r'(\d{1,2}):(\d{2})',  # HH:MM
            ]
            
            # Parse từ title và content
            # Ví dụ: "Lịch thi đấu Premier League: Man City vs Liverpool 15/01/2025 20:00"
            match_pattern = r'([A-Za-zÀ-ỹ\s]+?)\s*(?:vs|v\.s|đấu|gặp|-)\s*([A-Za-zÀ-ỹ\s]+?)(?:\s+\d{1,2}[\/\-]\d{1,2})?'
            
            found_matches = re.findall(match_pattern, title + ' ' + content_text[:500], re.IGNORECASE)
            
            for match in found_matches[:10]:  # Giới hạn 10 trận
                home_team = match[0].strip()
                away_team = match[1].strip()
                
                if len(home_team) < 3 or len(away_team) < 3:
                    continue
                
                # Tìm ngày giờ
                match_date = self.extract_match_date(content_text)
                
                matches.append({
                    'home_team_name': home_team,
                    'away_team_name': away_team,
                    'match_date': match_date,
                    'tournament_name': self.extract_tournament(title, content_text),
                    'category_id': self.detect_category(title, content_text, url),
                    'status': 'scheduled'
                })
            
            return matches if matches else None
            
        except Exception as e:
            logger.error(f"✗ Lỗi parse match from article {url}: {e}")
            return None
    
    def extract_match_date(self, text, current_year=None):
        """Trích xuất ngày giờ từ text"""
        try:
            if current_year is None:
                current_year = datetime.now().year
            
            # Tìm ngày giờ trong text
            # Pattern: dd/mm/yyyy HH:MM hoặc yyyy-mm-dd HH:MM:SS
            date_pattern = r'(\d{1,2})[\/\-](\d{1,2})[\/\-](\d{4})\s+(\d{1,2}):(\d{2})'
            match = re.search(date_pattern, text)
            
            if match:
                day, month, year, hour, minute = match.groups()
                return datetime(int(year), int(month), int(day), int(hour), int(minute))
            
            # Pattern khác: yyyy-mm-dd HH:MM
            date_pattern2 = r'(\d{4})[\/\-](\d{1,2})[\/\-](\d{1,2})\s+(\d{1,2}):(\d{2})'
            match = re.search(date_pattern2, text)
            
            if match:
                year, month, day, hour, minute = match.groups()
                return datetime(int(year), int(month), int(day), int(hour), int(minute))
            
            # Pattern: dd/mm HH:MM (không có năm, dùng current_year)
            date_pattern3 = r'(\d{1,2})[\/\-](\d{1,2})\s+(\d{1,2}):(\d{2})'
            match = re.search(date_pattern3, text)
            
            if match:
                day, month, hour, minute = match.groups()
                return datetime(current_year, int(month), int(day), int(hour), int(minute))
            
            # Mặc định: hôm nay + 1 ngày, 20:00
            return datetime.now().replace(hour=20, minute=0, second=0, microsecond=0) + timedelta(days=1)
            
        except Exception as e:
            logger.warning(f"⚠ Không parse được ngày giờ: {e}")
            return datetime.now().replace(hour=20, minute=0, second=0, microsecond=0) + timedelta(days=1)
    
    def extract_tournament(self, title, content):
        """Trích xuất tên giải đấu"""
        tournaments = [
            'Premier League', 'La Liga', 'Serie A', 'Bundesliga',
            'Champions League', 'Europa League', 'World Cup',
            'V-League', 'AFF Cup', 'SEA Games',
            'Ngoại hạng Anh', 'C1', 'C2'
        ]
        
        text = (title + ' ' + content[:200]).lower()
        for tournament in tournaments:
            if tournament.lower() in text:
                return tournament
        
        # Nếu URL chứa "ngoai-hang-anh", mặc định là Ngoại Hạng Anh
        if hasattr(self, 'base_url') and 'ngoai-hang-anh' in self.base_url.lower():
            return 'Ngoại Hạng Anh'
        
        return 'Giải đấu'


