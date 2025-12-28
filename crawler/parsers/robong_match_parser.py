# -*- coding: utf-8 -*-
"""
Robong Match Parser - Parser cho API Robong
"""

from parsers.base_parser import BaseParser
import logging
from datetime import datetime, timedelta
import json
from config import CATEGORY_MAPPING

logger = logging.getLogger(__name__)


class RobongMatchParser(BaseParser):
    """Parser cho API Robong lịch thi đấu"""
    
    def __init__(self):
        super().__init__('Robong Matches', 'https://rbapi.online/v1/match/list')
    
    def get_upcoming_matches(self, limit=50, days_range=None):
        """
        Lấy danh sách các trận đấu sắp diễn ra từ API Robong
        
        Args:
            limit: Số lượng trận đấu tối đa
            days_range: Tuple (days_before, days_after) để filter theo ngày
                       Ví dụ: (1, 1) = hôm qua, hôm nay, hôm sau
        """
        matches = []
        
        try:
            # Tính toán ngày để query API
            if days_range:
                days_before, days_after = days_range
                start_date = datetime.now() - timedelta(days=days_before)
                end_date = datetime.now() + timedelta(days=days_after)
                
                # Query cho từng ngày trong khoảng
                current_date = start_date
                while current_date <= end_date:
                    date_str = current_date.strftime('%d-%m-%Y')
                    date_matches = self._fetch_matches_for_date(date_str, limit)
                    matches.extend(date_matches)
                    current_date += timedelta(days=1)
            else:
                # Nếu không có days_range, lấy hôm nay và các ngày tiếp theo
                for i in range(7):  # Lấy 7 ngày tới
                    date = datetime.now() + timedelta(days=i)
                    date_str = date.strftime('%d-%m-%Y')
                    date_matches = self._fetch_matches_for_date(date_str, limit)
                    matches.extend(date_matches)
            
            # Loại bỏ trùng lặp dựa trên home_team và away_team
            seen = set()
            unique_matches = []
            for match in matches:
                match_date = match.get('match_date')
                if isinstance(match_date, datetime):
                    date_str = match_date.strftime('%Y-%m-%d %H:%M')
                else:
                    date_str = str(match_date)
                
                key = (match.get('home_team_name', '').lower(), 
                       match.get('away_team_name', '').lower(),
                       date_str)
                if key not in seen:
                    seen.add(key)
                    unique_matches.append(match)
            
            # Sắp xếp theo ngày
            unique_matches.sort(key=lambda x: x.get('match_date', datetime.now()))
            
            logger.info(f"✓ Tổng cộng tìm thấy {len(unique_matches)} trận đấu từ Robong API")
            return unique_matches[:limit]
            
        except Exception as e:
            logger.error(f"✗ Lỗi lấy matches từ Robong API: {e}", exc_info=True)
            return []
    
    def _fetch_matches_for_date(self, date_str, limit=50):
        """
        Lấy matches cho một ngày cụ thể
        
        Args:
            date_str: Ngày theo định dạng 'dd-mm-yyyy' (ví dụ: '05-11-2025')
            limit: Số lượng trận đấu tối đa
        """
        matches = []
        
        try:
            # URL API mới với tham số: type=schedule&state= (để lấy lịch thi đấu)
            api_url = f"{self.base_url}?sport_type=football&date={date_str}&type=schedule&state="
            logger.info(f"📡 Đang tải: {api_url}")
            
            # Sử dụng get_page từ BaseParser
            response_text = self.get_page(api_url)
            if not response_text:
                logger.warning(f"⚠ Không thể lấy dữ liệu từ API cho ngày {date_str}")
                return []
            
            # Parse JSON
            data = json.loads(response_text)
            
            # Kiểm tra status
            if not data.get('status', False):
                logger.warning(f"⚠ API trả về status=False cho ngày {date_str}")
                return []
            
            # Parse các competitions và matches
            result = data.get('result', [])
            for competition in result:
                competition_name = competition.get('name', '')
                competition_short_name = competition.get('short_name', '')
                competition_matches = competition.get('matches', [])
                
                logger.info(f"  ✓ Tìm thấy {len(competition_matches)} trận trong giải {competition_name}")
                
                for match_data in competition_matches:
                    match_info = self._parse_match_data(match_data, competition_name or competition_short_name)
                    if match_info:
                        matches.append(match_info)
                    
                    if len(matches) >= limit:
                        break
                
                if len(matches) >= limit:
                    break
            
            logger.info(f"✓ Đã parse {len(matches)} trận từ ngày {date_str}")
            return matches
            
        except json.JSONDecodeError as e:
            logger.error(f"✗ Lỗi parse JSON từ API: {e}")
            return []
        except Exception as e:
            logger.error(f"✗ Lỗi fetch matches cho ngày {date_str}: {e}", exc_info=True)
            return []
    
    def _parse_match_data(self, match_data, tournament_name):
        """
        Parse một match object từ API thành dict
        
        Args:
            match_data: Dict chứa thông tin match từ API
            tournament_name: Tên giải đấu
        """
        try:
            # Parse thời gian (Unix timestamp)
            match_time = match_data.get('match_time', 0)
            if match_time:
                match_date = datetime.fromtimestamp(match_time)
            else:
                match_date = datetime.now()
            
            # Parse teams
            home_team = match_data.get('home_team', {})
            away_team = match_data.get('away_team', {})
            
            home_team_name = home_team.get('name', '') or home_team.get('short_name', '')
            away_team_name = away_team.get('name', '') or away_team.get('short_name', '')
            
            if not home_team_name or not away_team_name:
                logger.warning(f"⚠ Thiếu tên đội trong match data")
                return None
            
            # Parse logo URLs
            home_team_logo = home_team.get('logo', '')
            away_team_logo = away_team.get('logo', '')
            
            # Parse status
            status_text = match_data.get('status_text', 'pending')
            status_map = {
                'pending': 'scheduled',
                'live': 'live',
                'finished': 'finished',
                'cancelled': 'cancelled'
            }
            status = status_map.get(status_text.lower(), 'scheduled')
            
            # Detect category từ tournament name
            category_id = self.detect_category_from_tournament(tournament_name)
            
            return {
                'home_team_name': home_team_name,
                'away_team_name': away_team_name,
                'home_team_logo': home_team_logo if home_team_logo else None,
                'away_team_logo': away_team_logo if away_team_logo else None,
                'match_date': match_date,
                'tournament_name': tournament_name,
                'category_id': category_id,
                'status': status,
                'venue': ''  # API không có venue
            }
            
        except Exception as e:
            logger.error(f"✗ Lỗi parse match data: {e}", exc_info=True)
            return None
    
    def detect_category_from_tournament(self, tournament_name):
        """Phát hiện category từ tên giải đấu"""
        if not tournament_name:
            return 1  # Default: Bóng đá
        
        name_lower = tournament_name.lower()
        
        # Mapping các giải đấu
        if any(x in name_lower for x in ['premier', 'ngoại hạng anh', 'fa cup', 'cup liên đoàn']):
            return 1  # Bóng đá
        elif any(x in name_lower for x in ['champion', 'europa', 'c1', 'c2', 'uefa']):
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
        elif any(x in name_lower for x in ['super liga', 'serbia']):
            return 1  # Bóng đá
        elif any(x in name_lower for x in ['afc', 'champions league']):
            return 1  # Bóng đá
        
        return 1  # Default: Bóng đá

