# -*- coding: utf-8 -*-
"""
配置文件 - Cấu hình crawler
"""

import os
from pathlib import Path

# Database Configuration
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': '',  # Thay đổi password của bạn
    'database': 'sports_news',
    'charset': 'utf8mb4'
}

# Upload Directory
BASE_DIR = Path(__file__).resolve().parent.parent
UPLOAD_DIR = BASE_DIR / 'uploads' / 'articles'
UPLOAD_DIR.mkdir(parents=True, exist_ok=True)

# API Base URL - Thay đổi theo domain của bạn
API_BASE_URL = 'http://localhost/com.nhd.news/api'  # Development
# API_BASE_URL = 'https://yourdomain.com/api'  # Production

# Default Author ID (user crawler_bot - ID từ setup_database.sql)
DEFAULT_AUTHOR_ID = 1  # ID của user crawler_bot (kiểm tra trong database)

# Upload paths (relative to project root)
UPLOAD_BASE_PATH = '/uploads/articles/'  # Đường dẫn lưu ảnh trong database

# Crawler Settings
USER_AGENT = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
REQUEST_TIMEOUT = 30
RETRY_TIMES = 3
DELAY_BETWEEN_REQUESTS = 2  # seconds

# Category Mapping (Vietnamese keywords to category_id)
CATEGORY_MAPPING = {
    'bóng đá': 1,
    'bong da': 1,
    'football': 1,
    'soccer': 1,
    'ngoại hạng anh': 1,
    'premier league': 1,
    'la liga': 1,
    'serie a': 1,
    'bundesliga': 1,
    'champions league': 1,
    'c1': 1,
    
    'bóng rổ': 2,
    'basketball': 2,
    'nba': 2,
    
    'quần vợt': 3,
    'tennis': 3,
    
    'võ thuật': 4,
    'boxing': 4,
    'mma': 4,
    'ufc': 4,
    
    'đua xe': 5,
    'formula 1': 5,
    'f1': 5,
    'motogp': 5,
}

# News Sources Configuration
NEWS_SOURCES = {
    'vnexpress': {
        'name': 'VnExpress Thể Thao',
        'base_url': 'https://vnexpress.net/the-thao',
        'enabled': True,
        'parser': 'VnExpressParser'
    },
    'bongdaplus': {
        'name': 'Bóng Đá Plus',
        'base_url': 'https://bongdaplus.vn',
        'enabled': True,
        'parser': 'BongDaPlusParser'
    },
    '24h': {
        'name': '24h Thể Thao',
        'base_url': 'https://www.24h.com.vn/bong-da-c48.html',
        'enabled': True,
        'parser': 'News24hParser'
    }
}

# Logging
LOG_FILE = BASE_DIR / 'crawler' / 'logs' / 'crawler.log'
LOG_FILE.parent.mkdir(parents=True, exist_ok=True)

