"""
Test script to verify fake users can login and use backend API
"""

import requests
import mysql.connector
from mysql.connector import Error
import sys
import random

# API Configuration
API_BASE_URL = "http://localhost/api"  # Thay đổi nếu API ở địa chỉ khác

# Database configuration
DB_CONFIG = {
    'host': 'localhost',
    'database': 'sports_news_db_v2',
    'user': 'root',
    'password': ''
}

def get_random_users(count=5):
    """Get random users from database"""
    try:
        connection = mysql.connector.connect(**DB_CONFIG)
        cursor = connection.cursor(dictionary=True)
        
        query = """
            SELECT user_id, email, username, full_name, role, 
                   email_verified_at IS NOT NULL as verified
            FROM users 
            WHERE role = 'user'
            ORDER BY RAND()
            LIMIT %s
        """
        
        cursor.execute(query, (count,))
        users = cursor.fetchall()
        
        cursor.close()
        connection.close()
        
        return users
        
    except Error as e:
        print(f"✗ Lỗi database: {e}")
        return []

def test_login(email, password="password123"):
    """Test login API"""
    try:
        response = requests.post(
            f"{API_BASE_URL}/auth/login",
            json={
                "email": email,
                "password": password
            },
            timeout=5
        )
        
        if response.status_code == 200:
            data = response.json()
            if data.get('success'):
                return {
                    'success': True,
                    'session_token': data.get('session_token'),
                    'user': data.get('user')
                }
        
        return {
            'success': False,
            'error': response.json().get('message', 'Unknown error')
        }
        
    except requests.exceptions.RequestException as e:
        return {
            'success': False,
            'error': f"Connection error: {str(e)}"
        }

def test_get_profile(session_token):
    """Test get profile API"""
    try:
        response = requests.get(
            f"{API_BASE_URL}/auth/me",
            headers={
                "Authorization": f"Bearer {session_token}"
            },
            timeout=5
        )
        
        if response.status_code == 200:
            data = response.json()
            return {
                'success': True,
                'profile': data.get('user')
            }
        
        return {
            'success': False,
            'error': response.json().get('message', 'Unknown error')
        }
        
    except requests.exceptions.RequestException as e:
        return {
            'success': False,
            'error': f"Connection error: {str(e)}"
        }

def test_get_articles(session_token=None):
    """Test get articles API"""
    try:
        headers = {}
        if session_token:
            headers["Authorization"] = f"Bearer {session_token}"
        
        response = requests.get(
            f"{API_BASE_URL}/articles",
            headers=headers,
            timeout=5
        )
        
        if response.status_code == 200:
            data = response.json()
            return {
                'success': True,
                'count': len(data.get('articles', []))
            }
        
        return {
            'success': False,
            'error': response.json().get('message', 'Unknown error')
        }
        
    except requests.exceptions.RequestException as e:
        return {
            'success': False,
            'error': f"Connection error: {str(e)}"
        }

def run_tests():
    """Run all tests"""
    print(f"\n{'='*70}")
    print(f"KIỂM TRA FAKE USERS VỚI BACKEND API")
    print(f"{'='*70}")
    print(f"API URL: {API_BASE_URL}")
    print(f"{'='*70}\n")
    
    # Get random users
    print("📋 Lấy danh sách users ngẫu nhiên từ database...")
    users = get_random_users(5)
    
    if not users:
        print("✗ Không tìm thấy users nào trong database!")
        print("  Vui lòng chạy create_fake_users.py trước.")
        return
    
    print(f"✓ Tìm thấy {len(users)} users để test\n")
    
    # Test each user
    success_count = 0
    fail_count = 0
    
    for i, user in enumerate(users, 1):
        print(f"\n{'─'*70}")
        print(f"TEST #{i} - {user['full_name']}")
        print(f"{'─'*70}")
        print(f"Email: {user['email']}")
        print(f"Username: {user['username']}")
        print(f"Role: {user['role']}")
        print(f"Verified: {'✓' if user['verified'] else '✗'}")
        print()
        
        # Test 1: Login
        print("🔐 Test 1: Login...")
        login_result = test_login(user['email'])
        
        if login_result['success']:
            print(f"  ✓ Login thành công")
            print(f"  Session token: {login_result['session_token'][:20]}...")
            session_token = login_result['session_token']
            
            # Test 2: Get profile
            print("\n👤 Test 2: Get profile...")
            profile_result = test_get_profile(session_token)
            
            if profile_result['success']:
                print(f"  ✓ Get profile thành công")
                profile = profile_result['profile']
                print(f"  User ID: {profile.get('user_id')}")
                print(f"  Full name: {profile.get('full_name')}")
                print(f"  Email verified: {profile.get('email_verified')}")
            else:
                print(f"  ✗ Get profile thất bại: {profile_result['error']}")
            
            # Test 3: Get articles (authenticated)
            print("\n📰 Test 3: Get articles (authenticated)...")
            articles_result = test_get_articles(session_token)
            
            if articles_result['success']:
                print(f"  ✓ Get articles thành công")
                print(f"  Số lượng articles: {articles_result['count']}")
            else:
                print(f"  ✗ Get articles thất bại: {articles_result['error']}")
            
            success_count += 1
            print(f"\n✅ User test PASSED")
            
        else:
            print(f"  ✗ Login thất bại: {login_result['error']}")
            fail_count += 1
            print(f"\n❌ User test FAILED")
    
    # Summary
    print(f"\n{'='*70}")
    print(f"KẾT QUẢ KIỂM TRA")
    print(f"{'='*70}")
    print(f"Tổng số tests: {len(users)}")
    print(f"✓ Thành công: {success_count}")
    print(f"✗ Thất bại: {fail_count}")
    print(f"Tỉ lệ: {(success_count/len(users)*100):.1f}%")
    print(f"{'='*70}\n")

def test_admin_login():
    """Test admin accounts"""
    print(f"\n{'='*70}")
    print(f"KIỂM TRA ADMIN ACCOUNTS")
    print(f"{'='*70}\n")
    
    admin_accounts = [
        {
            'email': 'admin@nhd.news',
            'password': 'admin123',
            'role': 'Admin'
        },
        {
            'email': 'moderator@nhd.news',
            'password': 'mod123',
            'role': 'Moderator'
        }
    ]
    
    for account in admin_accounts:
        print(f"🔐 Testing {account['role']}...")
        print(f"  Email: {account['email']}")
        
        result = test_login(account['email'], account['password'])
        
        if result['success']:
            print(f"  ✓ Login thành công")
            print(f"  Role: {result['user'].get('role')}")
            print(f"  Username: {result['user'].get('username')}")
        else:
            print(f"  ✗ Login thất bại: {result['error']}")
        
        print()
    
    print(f"{'='*70}\n")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        command = sys.argv[1]
        
        if command == "admin":
            test_admin_login()
        elif command == "users":
            run_tests()
        elif command == "all":
            test_admin_login()
            run_tests()
        else:
            print("Lệnh không hợp lệ!")
            print("\nCách sử dụng:")
            print("  python test_users.py admin  - Test admin/moderator accounts")
            print("  python test_users.py users  - Test random users")
            print("  python test_users.py all    - Test tất cả")
    else:
        # Default: test all
        test_admin_login()
        run_tests()



