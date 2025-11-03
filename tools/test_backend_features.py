"""
Comprehensive Backend API Testing Tool
Tests all major features of the Sports News API
"""

import requests
import mysql.connector
from mysql.connector import Error
import sys
import json
import os
from datetime import datetime
import random

# API Configuration
API_BASE_URL = "http://localhost/api"

# Database configuration
DB_CONFIG = {
    'host': 'localhost',
    'database': 'sports_news_db_v2',
    'user': 'root',
    'password': ''
}

class APITester:
    """API Testing class with comprehensive test methods"""
    
    def __init__(self, base_url=API_BASE_URL):
        self.base_url = base_url
        self.session_token = None
        self.user_id = None
        self.test_results = {
            'total': 0,
            'passed': 0,
            'failed': 0,
            'errors': []
        }
    
    def log_result(self, test_name, success, message=""):
        """Log test result"""
        self.test_results['total'] += 1
        if success:
            self.test_results['passed'] += 1
            print(f"  ✓ {test_name}")
            if message:
                print(f"    {message}")
        else:
            self.test_results['failed'] += 1
            print(f"  ✗ {test_name}")
            if message:
                print(f"    Error: {message}")
            self.test_results['errors'].append({
                'test': test_name,
                'error': message
            })
    
    def request(self, method, endpoint, **kwargs):
        """Make API request with error handling"""
        try:
            url = f"{self.base_url}{endpoint}"
            
            # Add auth header if session token exists
            if self.session_token and 'headers' not in kwargs:
                kwargs['headers'] = {}
            if self.session_token:
                if 'headers' not in kwargs:
                    kwargs['headers'] = {}
                kwargs['headers']['Authorization'] = f"Bearer {self.session_token}"
            
            kwargs['timeout'] = kwargs.get('timeout', 10)
            
            response = requests.request(method, url, **kwargs)
            
            return {
                'success': response.status_code in [200, 201],
                'status_code': response.status_code,
                'data': response.json() if response.headers.get('content-type', '').startswith('application/json') else response.text,
                'response': response
            }
        except Exception as e:
            return {
                'success': False,
                'error': str(e),
                'data': None
            }
    
    # ========================================
    # Auth Tests
    # ========================================
    
    def test_auth_health(self):
        """Test API health check"""
        print("\n🏥 Testing API Health...")
        result = self.request('GET', '/health')
        self.log_result(
            "Health Check",
            result['success'] and result['data'].get('status') == 'healthy',
            f"Database: {result['data'].get('database', 'unknown')}" if result['success'] else result.get('error')
        )
    
    def test_auth_login(self, email, password="password123"):
        """Test login"""
        print(f"\n🔐 Testing Login ({email})...")
        result = self.request('POST', '/auth/login', json={
            'email': email,
            'password': password
        })
        
        if result['success'] and result['data'].get('success'):
            self.session_token = result['data'].get('session_token')
            self.user_id = result['data'].get('user', {}).get('user_id')
            self.log_result(
                "Login",
                True,
                f"User: {result['data'].get('user', {}).get('full_name')}, Token: {self.session_token[:20]}..."
            )
            return True
        else:
            self.log_result(
                "Login",
                False,
                result['data'].get('message') if result['success'] else result.get('error')
            )
            return False
    
    def test_auth_me(self):
        """Test get current user info"""
        print("\n👤 Testing Get Current User...")
        result = self.request('GET', '/auth/me')
        self.log_result(
            "Get Current User",
            result['success'] and result['data'].get('user'),
            f"User ID: {result['data'].get('user', {}).get('user_id')}" if result['success'] else result.get('error')
        )
    
    def test_auth_verify_session(self):
        """Test verify session"""
        print("\n🔍 Testing Verify Session...")
        result = self.request('GET', '/auth/verify-session')
        self.log_result(
            "Verify Session",
            result['success'],
            "Session is valid" if result['success'] else result.get('error')
        )
    
    def test_auth_logout(self):
        """Test logout"""
        print("\n🚪 Testing Logout...")
        result = self.request('POST', '/auth/logout')
        self.log_result(
            "Logout",
            result['success'],
            "Logged out successfully" if result['success'] else result.get('error')
        )
        if result['success']:
            self.session_token = None
            self.user_id = None
    
    # ========================================
    # Articles Tests
    # ========================================
    
    def test_articles_list(self):
        """Test get articles list"""
        print("\n📰 Testing Get Articles List...")
        result = self.request('GET', '/articles')
        
        if result['success']:
            articles = result['data'].get('articles', [])
            self.log_result(
                "Get Articles",
                True,
                f"Retrieved {len(articles)} articles"
            )
            return articles
        else:
            self.log_result("Get Articles", False, result.get('error'))
            return []
    
    def test_articles_featured(self):
        """Test get featured articles"""
        print("\n⭐ Testing Get Featured Articles...")
        result = self.request('GET', '/articles/featured')
        self.log_result(
            "Get Featured Articles",
            result['success'],
            f"Retrieved {len(result['data'].get('articles', []))} articles" if result['success'] else result.get('error')
        )
    
    def test_articles_breaking(self):
        """Test get breaking news"""
        print("\n🔥 Testing Get Breaking News...")
        result = self.request('GET', '/articles/breaking')
        self.log_result(
            "Get Breaking News",
            result['success'],
            f"Retrieved {len(result['data'].get('articles', []))} articles" if result['success'] else result.get('error')
        )
    
    def test_articles_trending(self):
        """Test get trending articles"""
        print("\n📈 Testing Get Trending Articles...")
        result = self.request('GET', '/articles/trending')
        self.log_result(
            "Get Trending Articles",
            result['success'],
            f"Retrieved {len(result['data'].get('articles', []))} articles" if result['success'] else result.get('error')
        )
    
    def test_article_detail(self, article_id):
        """Test get article detail"""
        print(f"\n📄 Testing Get Article Detail (ID: {article_id})...")
        result = self.request('GET', f'/articles/{article_id}')
        self.log_result(
            "Get Article Detail",
            result['success'] and result['data'].get('article'),
            f"Title: {result['data'].get('article', {}).get('title', '')[:50]}..." if result['success'] else result.get('error')
        )
        return result['data'].get('article') if result['success'] else None
    
    def test_article_view(self, article_id):
        """Test increment article view"""
        print(f"\n👁️ Testing Increment Article View (ID: {article_id})...")
        result = self.request('POST', f'/articles/{article_id}/view')
        self.log_result(
            "Increment Article View",
            result['success'],
            "View count incremented" if result['success'] else result.get('error')
        )
    
    # ========================================
    # Categories Tests
    # ========================================
    
    def test_categories_list(self):
        """Test get categories list"""
        print("\n📂 Testing Get Categories...")
        result = self.request('GET', '/categories')
        
        if result['success']:
            categories = result['data'].get('categories', [])
            self.log_result(
                "Get Categories",
                True,
                f"Retrieved {len(categories)} categories"
            )
            return categories
        else:
            self.log_result("Get Categories", False, result.get('error'))
            return []
    
    def test_category_detail(self, category_id):
        """Test get category detail"""
        print(f"\n📂 Testing Get Category Detail (ID: {category_id})...")
        result = self.request('GET', f'/categories/{category_id}')
        self.log_result(
            "Get Category Detail",
            result['success'],
            f"Name: {result['data'].get('category', {}).get('name')}" if result['success'] else result.get('error')
        )
    
    # ========================================
    # Search Tests
    # ========================================
    
    def test_search(self, query="bóng đá"):
        """Test search functionality"""
        print(f"\n🔍 Testing Search (Query: '{query}')...")
        result = self.request('GET', f'/search?q={query}')
        
        if result['success']:
            results = result['data'].get('results', [])
            self.log_result(
                "Search",
                True,
                f"Found {len(results)} results"
            )
        else:
            self.log_result("Search", False, result.get('error'))
    
    # ========================================
    # Teams Tests
    # ========================================
    
    def test_teams_list(self):
        """Test get teams list"""
        print("\n⚽ Testing Get Teams...")
        result = self.request('GET', '/teams')
        
        if result['success']:
            teams = result['data'].get('teams', [])
            self.log_result(
                "Get Teams",
                True,
                f"Retrieved {len(teams)} teams"
            )
            return teams
        else:
            self.log_result("Get Teams", False, result.get('error'))
            return []
    
    def test_team_detail(self, team_id):
        """Test get team detail"""
        print(f"\n⚽ Testing Get Team Detail (ID: {team_id})...")
        result = self.request('GET', f'/teams/{team_id}')
        self.log_result(
            "Get Team Detail",
            result['success'],
            f"Name: {result['data'].get('team', {}).get('name')}" if result['success'] else result.get('error')
        )
    
    # ========================================
    # Matches Tests
    # ========================================
    
    def test_matches_list(self):
        """Test get matches list"""
        print("\n🏆 Testing Get Matches...")
        result = self.request('GET', '/matches')
        
        if result['success']:
            matches = result['data'].get('matches', [])
            self.log_result(
                "Get Matches",
                True,
                f"Retrieved {len(matches)} matches"
            )
            return matches
        else:
            self.log_result("Get Matches", False, result.get('error'))
            return []
    
    def test_matches_live(self):
        """Test get live matches"""
        print("\n🔴 Testing Get Live Matches...")
        result = self.request('GET', '/matches/live')
        self.log_result(
            "Get Live Matches",
            result['success'],
            f"Retrieved {len(result['data'].get('matches', []))} live matches" if result['success'] else result.get('error')
        )
    
    def test_matches_upcoming(self):
        """Test get upcoming matches"""
        print("\n📅 Testing Get Upcoming Matches...")
        result = self.request('GET', '/matches/upcoming')
        self.log_result(
            "Get Upcoming Matches",
            result['success'],
            f"Retrieved {len(result['data'].get('matches', []))} upcoming matches" if result['success'] else result.get('error')
        )
    
    def test_match_detail(self, match_id):
        """Test get match detail"""
        print(f"\n🏆 Testing Get Match Detail (ID: {match_id})...")
        result = self.request('GET', f'/matches/{match_id}')
        self.log_result(
            "Get Match Detail",
            result['success'],
            f"Match info retrieved" if result['success'] else result.get('error')
        )
    
    # ========================================
    # Videos Tests
    # ========================================
    
    def test_videos_list(self):
        """Test get videos list"""
        print("\n🎥 Testing Get Videos...")
        result = self.request('GET', '/videos')
        
        if result['success']:
            videos = result['data'].get('videos', [])
            self.log_result(
                "Get Videos",
                True,
                f"Retrieved {len(videos)} videos"
            )
            return videos
        else:
            self.log_result("Get Videos", False, result.get('error'))
            return []
    
    def test_videos_highlights(self):
        """Test get video highlights"""
        print("\n⭐ Testing Get Video Highlights...")
        result = self.request('GET', '/videos/highlights')
        self.log_result(
            "Get Video Highlights",
            result['success'],
            f"Retrieved {len(result['data'].get('videos', []))} highlights" if result['success'] else result.get('error')
        )
    
    def test_video_detail(self, video_id):
        """Test get video detail"""
        print(f"\n🎥 Testing Get Video Detail (ID: {video_id})...")
        result = self.request('GET', f'/videos/{video_id}')
        self.log_result(
            "Get Video Detail",
            result['success'],
            f"Title: {result['data'].get('video', {}).get('title', '')[:50]}..." if result['success'] else result.get('error')
        )
    
    # ========================================
    # Tags Tests
    # ========================================
    
    def test_tags_list(self):
        """Test get tags list"""
        print("\n🏷️ Testing Get Tags...")
        result = self.request('GET', '/tags')
        
        if result['success']:
            tags = result['data'].get('tags', [])
            self.log_result(
                "Get Tags",
                True,
                f"Retrieved {len(tags)} tags"
            )
            return tags
        else:
            self.log_result("Get Tags", False, result.get('error'))
            return []
    
    def test_tag_detail(self, tag_id):
        """Test get tag detail"""
        print(f"\n🏷️ Testing Get Tag Detail (ID: {tag_id})...")
        result = self.request('GET', f'/tags/{tag_id}')
        self.log_result(
            "Get Tag Detail",
            result['success'],
            f"Name: {result['data'].get('tag', {}).get('name')}" if result['success'] else result.get('error')
        )
    
    # ========================================
    # Comments Tests (Requires Auth)
    # ========================================
    
    def test_comments_get(self, article_id):
        """Test get comments for article"""
        print(f"\n💬 Testing Get Comments (Article ID: {article_id})...")
        result = self.request('GET', f'/articles/{article_id}/comments')
        
        if result['success']:
            comments = result['data'].get('comments', [])
            self.log_result(
                "Get Comments",
                True,
                f"Retrieved {len(comments)} comments"
            )
            return comments
        else:
            self.log_result("Get Comments", False, result.get('error'))
            return []
    
    def test_comment_create(self, article_id, content="Test comment from automated testing"):
        """Test create comment"""
        print(f"\n💬 Testing Create Comment (Article ID: {article_id})...")
        result = self.request('POST', f'/articles/{article_id}/comments', json={
            'content': content
        })
        
        # API returns: { "message": "...", "comment": { "comment_id": 123, ... } }
        if result['success']:
            comment = result['data'].get('comment', {})
            comment_id = comment.get('comment_id')
            message = result['data'].get('message', 'Comment created')
            
            self.log_result(
                "Create Comment",
                True,
                f"Comment ID: {comment_id}, {message}" if comment_id else message
            )
            return comment_id
        else:
            self.log_result(
                "Create Comment",
                False,
                result.get('error', 'Unknown error')
            )
            return None
    
    def test_comment_update(self, comment_id, content="Updated test comment"):
        """Test update comment"""
        print(f"\n✏️ Testing Update Comment (ID: {comment_id})...")
        result = self.request('PUT', f'/comments/{comment_id}', json={
            'content': content
        })
        self.log_result(
            "Update Comment",
            result['success'],
            "Comment updated" if result['success'] else result.get('error')
        )
    
    def test_comment_like(self, comment_id):
        """Test like/unlike comment"""
        print(f"\n❤️ Testing Like Comment (ID: {comment_id})...")
        result = self.request('POST', f'/comments/{comment_id}/like')
        self.log_result(
            "Like Comment",
            result['success'],
            f"Action: {result['data'].get('action', 'unknown')}" if result['success'] else result.get('error')
        )
    
    def test_comment_delete(self, comment_id):
        """Test delete comment"""
        print(f"\n🗑️ Testing Delete Comment (ID: {comment_id})...")
        result = self.request('DELETE', f'/comments/{comment_id}')
        self.log_result(
            "Delete Comment",
            result['success'],
            "Comment deleted" if result['success'] else result.get('error')
        )
    
    # ========================================
    # Likes Tests (Requires Auth)
    # ========================================
    
    def test_article_like(self, article_id):
        """Test like/unlike article"""
        print(f"\n❤️ Testing Like Article (ID: {article_id})...")
        result = self.request('POST', f'/articles/{article_id}/like')
        
        if result['success']:
            action = result['data'].get('action', 'unknown')
            likes_count = result['data'].get('likes_count', 0)
            self.log_result(
                "Like Article",
                True,
                f"Action: {action}, Total likes: {likes_count}"
            )
            return action
        else:
            self.log_result("Like Article", False, result.get('error'))
            return None
    
    def test_article_like_status(self, article_id):
        """Test get article like status"""
        print(f"\n💖 Testing Get Article Like Status (ID: {article_id})...")
        result = self.request('GET', f'/articles/{article_id}/like-status')
        
        if result['success']:
            is_liked = result['data'].get('is_liked', False)
            self.log_result(
                "Get Like Status",
                True,
                f"Liked: {'Yes' if is_liked else 'No'}"
            )
        else:
            self.log_result("Get Like Status", False, result.get('error'))
    
    # ========================================
    # Favorites Tests (Requires Auth)
    # ========================================
    
    def test_favorite_add(self, article_id):
        """Test add article to favorites"""
        print(f"\n⭐ Testing Add to Favorites (Article ID: {article_id})...")
        result = self.request('POST', '/favorites', json={
            'type': 'article',
            'article_id': article_id
        })
        
        # API returns: { "success": true, "favorite_id": 123, "message": "..." }
        if result['success']:
            favorite_id = result['data'].get('favorite_id')
            message = result['data'].get('message', 'Added to favorites')
            
            self.log_result(
                "Add to Favorites",
                True,
                f"Favorite ID: {favorite_id}, {message}" if favorite_id else message
            )
            return favorite_id
        else:
            self.log_result(
                "Add to Favorites",
                False,
                result.get('error', 'Unknown error')
            )
            return None
    
    def test_favorites_list(self):
        """Test get user's favorites"""
        print("\n⭐ Testing Get Favorites List...")
        result = self.request('GET', '/profile/favorites')
        
        if result['success']:
            favorites = result['data'].get('favorites', [])
            self.log_result(
                "Get Favorites",
                True,
                f"Retrieved {len(favorites)} favorites"
            )
            return favorites
        else:
            self.log_result("Get Favorites", False, result.get('error'))
            return []
    
    def test_favorite_remove(self, favorite_id):
        """Test remove from favorites"""
        print(f"\n🗑️ Testing Remove from Favorites (ID: {favorite_id})...")
        result = self.request('DELETE', f'/favorites/{favorite_id}')
        self.log_result(
            "Remove from Favorites",
            result['success'],
            "Removed successfully" if result['success'] else result.get('error')
        )
    
    # ========================================
    # Profile Tests (Requires Auth)
    # ========================================
    
    def test_profile_get(self):
        """Test get user profile"""
        print("\n👤 Testing Get Profile...")
        result = self.request('GET', '/profile')
        
        if result['success']:
            profile = result['data'].get('profile', {})
            self.log_result(
                "Get Profile",
                True,
                f"User: {profile.get('full_name')}, Email: {profile.get('email')}"
            )
            return profile
        else:
            self.log_result("Get Profile", False, result.get('error'))
            return None
    
    def test_profile_update(self, data):
        """Test update profile"""
        print("\n✏️ Testing Update Profile...")
        result = self.request('PUT', '/profile', json=data)
        self.log_result(
            "Update Profile",
            result['success'],
            "Profile updated successfully" if result['success'] else result.get('error')
        )
    
    def test_profile_update_password(self, old_password, new_password):
        """Test update password"""
        print("\n🔐 Testing Update Password...")
        result = self.request('PUT', '/profile/password', json={
            'old_password': old_password,
            'new_password': new_password,
            'new_password_confirmation': new_password
        })
        self.log_result(
            "Update Password",
            result['success'],
            "Password updated successfully" if result['success'] else result.get('error')
        )
    
    # ========================================
    # Posts Tests (Requires Auth)
    # ========================================
    
    def test_post_create(self, title="Test Post", content="This is a test post"):
        """Test create user post"""
        print("\n📝 Testing Create Post...")
        result = self.request('POST', '/posts/create', json={
            'title': title,
            'content': content,
            'category_id': 1
        })
        
        if result['success'] and result['data'].get('success'):
            post_id = result['data'].get('post', {}).get('article_id')
            self.log_result(
                "Create Post",
                True,
                f"Post ID: {post_id}"
            )
            return post_id
        else:
            self.log_result(
                "Create Post",
                False,
                result['data'].get('message') if result['success'] else result.get('error')
            )
            return None
    
    def test_my_posts(self):
        """Test get user's posts"""
        print("\n📝 Testing Get My Posts...")
        result = self.request('GET', '/posts/my-posts')
        
        if result['success']:
            posts = result['data'].get('posts', [])
            self.log_result(
                "Get My Posts",
                True,
                f"Retrieved {len(posts)} posts"
            )
            return posts
        else:
            self.log_result("Get My Posts", False, result.get('error'))
            return []
    
    def test_post_update(self, post_id, title="Updated Post", content="Updated content"):
        """Test update post"""
        print(f"\n✏️ Testing Update Post (ID: {post_id})...")
        result = self.request('POST', '/posts/update', json={
            'article_id': post_id,
            'title': title,
            'content': content
        })
        self.log_result(
            "Update Post",
            result['success'],
            "Post updated successfully" if result['success'] else result.get('error')
        )
    
    def test_post_delete(self, post_id):
        """Test delete post"""
        print(f"\n🗑️ Testing Delete Post (ID: {post_id})...")
        result = self.request('DELETE', '/posts/delete', json={
            'article_id': post_id
        })
        self.log_result(
            "Delete Post",
            result['success'],
            "Post deleted successfully" if result['success'] else result.get('error')
        )
    
    # ========================================
    # Summary
    # ========================================
    
    def print_summary(self):
        """Print test summary"""
        print(f"\n{'='*70}")
        print("📊 TEST SUMMARY")
        print(f"{'='*70}")
        print(f"Total Tests: {self.test_results['total']}")
        print(f"✓ Passed: {self.test_results['passed']}")
        print(f"✗ Failed: {self.test_results['failed']}")
        
        if self.test_results['total'] > 0:
            success_rate = (self.test_results['passed'] / self.test_results['total']) * 100
            print(f"Success Rate: {success_rate:.1f}%")
        
        if self.test_results['errors']:
            print(f"\n❌ Failed Tests:")
            for error in self.test_results['errors']:
                print(f"  - {error['test']}: {error['error']}")
        
        print(f"{'='*70}\n")


def get_test_user_from_db():
    """Get a random test user from database"""
    try:
        connection = mysql.connector.connect(**DB_CONFIG)
        cursor = connection.cursor(dictionary=True)
        
        cursor.execute("""
            SELECT user_id, email, username, full_name 
            FROM users 
            WHERE role = 'user' AND email_verified_at IS NOT NULL AND is_active = 1
            ORDER BY RAND()
            LIMIT 1
        """)
        
        user = cursor.fetchone()
        cursor.close()
        connection.close()
        
        return user
        
    except Error as e:
        print(f"✗ Database error: {e}")
        return None


def run_public_tests():
    """Run tests for public endpoints (no auth required)"""
    print(f"\n{'='*70}")
    print("🌐 TESTING PUBLIC ENDPOINTS")
    print(f"{'='*70}")
    
    tester = APITester()
    
    # Health check
    tester.test_auth_health()
    
    # Articles
    articles = tester.test_articles_list()
    tester.test_articles_featured()
    tester.test_articles_breaking()
    tester.test_articles_trending()
    
    if articles:
        article_id = articles[0].get('article_id')
        tester.test_article_detail(article_id)
        tester.test_article_view(article_id)
        
        # Comments (public read)
        tester.test_comments_get(article_id)
    
    # Categories
    categories = tester.test_categories_list()
    if categories:
        tester.test_category_detail(categories[0].get('category_id'))
    
    # Search
    tester.test_search("bóng đá")
    
    # Teams
    teams = tester.test_teams_list()
    if teams:
        tester.test_team_detail(teams[0].get('team_id'))
    
    # Matches
    matches = tester.test_matches_list()
    tester.test_matches_live()
    tester.test_matches_upcoming()
    if matches:
        tester.test_match_detail(matches[0].get('match_id'))
    
    # Videos
    videos = tester.test_videos_list()
    tester.test_videos_highlights()
    if videos:
        tester.test_video_detail(videos[0].get('video_id'))
    
    # Tags
    tags = tester.test_tags_list()
    if tags:
        tester.test_tag_detail(tags[0].get('tag_id'))
    
    tester.print_summary()
    return tester


def run_authenticated_tests(email=None, password="password123"):
    """Run tests for authenticated endpoints"""
    print(f"\n{'='*70}")
    print("🔒 TESTING AUTHENTICATED ENDPOINTS")
    print(f"{'='*70}")
    
    # Get test user
    if not email:
        print("\n📋 Getting test user from database...")
        user = get_test_user_from_db()
        if not user:
            print("✗ No test user found. Please run create_fake_users.py first.")
            return None
        email = user['email']
        print(f"✓ Using test user: {user['full_name']} ({email})")
    
    tester = APITester()
    
    # Login
    if not tester.test_auth_login(email, password):
        print("✗ Login failed. Cannot continue with authenticated tests.")
        return None
    
    # Auth tests
    tester.test_auth_me()
    tester.test_auth_verify_session()
    
    # Profile tests
    profile = tester.test_profile_get()
    tester.test_profile_update({
        'phone': '0987654321',
        'bio': 'Updated bio from test'
    })
    
    # Get an article for testing
    print("\n📰 Getting an article for testing...")
    result = tester.request('GET', '/articles?limit=1')
    if result['success'] and result['data'].get('articles'):
        article = result['data']['articles'][0]
        article_id = article['article_id']
        print(f"✓ Using article: {article['title'][:50]}...")
        
        # Like tests
        tester.test_article_like(article_id)
        tester.test_article_like_status(article_id)
        tester.test_article_like(article_id)  # Unlike
        
        # Favorite tests
        favorite_id = tester.test_favorite_add(article_id)
        favorites = tester.test_favorites_list()
        if favorite_id:
            tester.test_favorite_remove(favorite_id)
        
        # Comment tests
        comment_id = tester.test_comment_create(article_id, "Great article!")
        if comment_id:
            tester.test_comment_update(comment_id, "Updated: Great article!")
            tester.test_comment_like(comment_id)
            tester.test_comment_delete(comment_id)
    
    # Post tests
    post_id = tester.test_post_create("Test Article", "This is a test article created by automated testing.")
    tester.test_my_posts()
    if post_id:
        tester.test_post_update(post_id, "Updated Test Article", "Updated content")
        tester.test_post_delete(post_id)
    
    # Logout
    tester.test_auth_logout()
    
    tester.print_summary()
    return tester


def run_all_tests():
    """Run all tests"""
    print(f"\n{'='*70}")
    print("🚀 COMPREHENSIVE BACKEND API TESTING")
    print(f"{'='*70}")
    print(f"API: {API_BASE_URL}")
    print(f"Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"{'='*70}")
    
    # Run public tests
    public_tester = run_public_tests()
    
    # Run authenticated tests
    auth_tester = run_authenticated_tests()
    
    # Overall summary
    if public_tester and auth_tester:
        total_tests = public_tester.test_results['total'] + auth_tester.test_results['total']
        total_passed = public_tester.test_results['passed'] + auth_tester.test_results['passed']
        total_failed = public_tester.test_results['failed'] + auth_tester.test_results['failed']
        
        print(f"\n{'='*70}")
        print("🎯 OVERALL SUMMARY")
        print(f"{'='*70}")
        print(f"Total Tests: {total_tests}")
        print(f"✓ Passed: {total_passed}")
        print(f"✗ Failed: {total_failed}")
        
        if total_tests > 0:
            success_rate = (total_passed / total_tests) * 100
            print(f"Success Rate: {success_rate:.1f}%")
        
        print(f"{'='*70}\n")


if __name__ == "__main__":
    if len(sys.argv) > 1:
        command = sys.argv[1]
        
        if command == "public":
            run_public_tests()
        elif command == "auth":
            email = sys.argv[2] if len(sys.argv) > 2 else None
            password = sys.argv[3] if len(sys.argv) > 3 else "password123"
            run_authenticated_tests(email, password)
        elif command == "all":
            run_all_tests()
        else:
            print("Lệnh không hợp lệ!")
            print("\nCách sử dụng:")
            print("  python test_backend_features.py public              - Test public endpoints")
            print("  python test_backend_features.py auth [email] [pass] - Test authenticated endpoints")
            print("  python test_backend_features.py all                 - Test tất cả")
    else:
        # Default: run all tests
        run_all_tests()

