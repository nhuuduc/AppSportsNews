"""
Tool to create fake users for testing backend functionality
Generates realistic user data and inserts into the database
"""

import mysql.connector
from mysql.connector import Error
from faker import Faker
import bcrypt
import random
from datetime import datetime, timedelta
import sys

# Database configuration (matching PHP config)
DB_CONFIG = {
    'host': 'localhost',
    'database': 'sports_news_db_v2',
    'user': 'root',
    'password': ''
}

# Initialize Faker with Vietnamese locale
fake = Faker(['vi_VN', 'en_US'])

def create_connection():
    """Create database connection"""
    try:
        connection = mysql.connector.connect(**DB_CONFIG)
        if connection.is_connected():
            print(f"✓ Đã kết nối thành công đến database: {DB_CONFIG['database']}")
            return connection
    except Error as e:
        print(f"✗ Lỗi kết nối database: {e}")
        sys.exit(1)

def hash_password(password):
    """Hash password using bcrypt (compatible with PHP password_hash)"""
    # bcrypt với cost factor 10 (mặc định của PHP PASSWORD_BCRYPT)
    salt = bcrypt.gensalt(rounds=10)
    hashed = bcrypt.hashpw(password.encode('utf-8'), salt)
    return hashed.decode('utf-8')

def generate_username(full_name):
    """Generate username from full name"""
    # Loại bỏ dấu và tạo username
    name_parts = full_name.lower().split()
    if len(name_parts) >= 2:
        username = name_parts[-1] + ''.join([p[0] for p in name_parts[:-1]])
    else:
        username = name_parts[0]
    
    # Thêm số ngẫu nhiên để tránh trùng
    username = username + str(random.randint(100, 9999))
    return username

def create_fake_user(cursor, role='user', verified=True, active=True):
    """Create a single fake user"""
    
    # Generate user data
    full_name = fake.name()
    username = generate_username(full_name)
    email = fake.unique.email()
    password = 'password123'  # Default password cho test
    password_hash = hash_password(password)
    
    # Random profile data
    phone = fake.phone_number() if random.random() > 0.3 else None
    date_of_birth = fake.date_of_birth(minimum_age=18, maximum_age=65) if random.random() > 0.4 else None
    gender = random.choice(['male', 'female', 'other']) if random.random() > 0.3 else None
    
    # Avatar URL (optional)
    avatar_url = None
    if random.random() > 0.5:
        avatar_url = f"https://i.pravatar.cc/300?u={email}"
    
    # Created date (random trong 6 tháng qua)
    days_ago = random.randint(1, 180)
    created_at = datetime.now() - timedelta(days=days_ago)
    
    # Email verification time (nếu verified)
    email_verified_at = None
    if verified:
        verify_delay = random.randint(1, 72)  # 1-72 giờ sau khi tạo
        email_verified_at = created_at + timedelta(hours=verify_delay)
    
    try:
        # Insert user
        insert_query = """
            INSERT INTO users 
            (email, username, password_hash, full_name, avatar_url, phone, 
             date_of_birth, gender, role, email_verified_at, is_active, created_at)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
        """
        
        cursor.execute(insert_query, (
            email, username, password_hash, full_name, avatar_url, phone,
            date_of_birth, gender, role, email_verified_at, active, created_at
        ))
        
        user_id = cursor.lastrowid
        
        return {
            'user_id': user_id,
            'email': email,
            'username': username,
            'password': password,
            'full_name': full_name,
            'role': role,
            'verified': verified,
            'active': active
        }
        
    except Error as e:
        print(f"✗ Lỗi tạo user: {e}")
        return None

def create_admin_users(cursor, count=2):
    """Create admin users with known credentials"""
    admins = []
    
    admin_data = [
        {
            'email': 'admin@nhd.news',
            'username': 'admin',
            'full_name': 'System Administrator',
            'password': 'admin123'
        },
        {
            'email': 'moderator@nhd.news',
            'username': 'moderator',
            'full_name': 'Content Moderator',
            'password': 'mod123'
        }
    ]
    
    for i in range(min(count, len(admin_data))):
        data = admin_data[i]
        role = 'admin' if i == 0 else 'moderator'
        
        try:
            password_hash = hash_password(data['password'])
            created_at = datetime.now()
            email_verified_at = created_at
            
            insert_query = """
                INSERT INTO users 
                (email, username, password_hash, full_name, role, email_verified_at, is_active, created_at)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
            """
            
            cursor.execute(insert_query, (
                data['email'], data['username'], password_hash, data['full_name'],
                role, email_verified_at, True, created_at
            ))
            
            user_id = cursor.lastrowid
            
            admins.append({
                'user_id': user_id,
                'email': data['email'],
                'username': data['username'],
                'password': data['password'],
                'full_name': data['full_name'],
                'role': role
            })
            
            print(f"✓ Đã tạo {role}: {data['email']} / {data['password']}")
            
        except Error as e:
            if 'Duplicate entry' in str(e):
                print(f"⚠ {role} đã tồn tại: {data['email']}")
            else:
                print(f"✗ Lỗi tạo {role}: {e}")
    
    return admins

def generate_users(num_users=50, num_admins=2, verified_ratio=0.8, active_ratio=0.95):
    """
    Generate fake users in database
    
    Args:
        num_users: Số lượng user thường cần tạo
        num_admins: Số lượng admin/moderator (tối đa 2)
        verified_ratio: Tỉ lệ user đã xác thực email (0.0 - 1.0)
        active_ratio: Tỉ lệ user active (0.0 - 1.0)
    """
    
    connection = create_connection()
    cursor = connection.cursor()
    
    print(f"\n{'='*60}")
    print(f"BẮT ĐẦU TẠO FAKE USERS")
    print(f"{'='*60}")
    print(f"Số lượng users thường: {num_users}")
    print(f"Số lượng admin/mod: {num_admins}")
    print(f"Tỉ lệ verified: {verified_ratio*100}%")
    print(f"Tỉ lệ active: {active_ratio*100}%")
    print(f"{'='*60}\n")
    
    created_users = []
    
    # Create admin users first
    if num_admins > 0:
        print("📋 Tạo admin/moderator users...")
        admins = create_admin_users(cursor, num_admins)
        created_users.extend(admins)
        print()
    
    # Create regular users
    print("📋 Tạo regular users...")
    success_count = 0
    fail_count = 0
    
    for i in range(num_users):
        # Determine if user should be verified and active
        verified = random.random() < verified_ratio
        active = random.random() < active_ratio
        
        user = create_fake_user(cursor, role='user', verified=verified, active=active)
        
        if user:
            created_users.append(user)
            success_count += 1
            
            # Progress indicator
            if (i + 1) % 10 == 0:
                print(f"  Đã tạo {i + 1}/{num_users} users...")
        else:
            fail_count += 1
    
    # Commit changes
    connection.commit()
    
    # Print summary
    print(f"\n{'='*60}")
    print(f"KẾT QUẢ")
    print(f"{'='*60}")
    print(f"✓ Tổng số users đã tạo: {len(created_users)}")
    print(f"  - Admin/Moderator: {len([u for u in created_users if u['role'] in ['admin', 'moderator']])}")
    print(f"  - Regular users: {len([u for u in created_users if u['role'] == 'user'])}")
    print(f"  - Verified: {len([u for u in created_users if u.get('verified', True)])}")
    print(f"  - Unverified: {len([u for u in created_users if not u.get('verified', True)])}")
    print(f"  - Active: {len([u for u in created_users if u.get('active', True)])}")
    print(f"  - Inactive: {len([u for u in created_users if not u.get('active', True)])}")
    
    if fail_count > 0:
        print(f"✗ Thất bại: {fail_count}")
    
    print(f"{'='*60}\n")
    
    # Print some sample credentials
    print("📋 MỘT SỐ TÀI KHOẢN MẪU:")
    print(f"{'='*60}")
    
    # Admin accounts
    admin_accounts = [u for u in created_users if u['role'] in ['admin', 'moderator']]
    if admin_accounts:
        print("\n🔑 ADMIN/MODERATOR ACCOUNTS:")
        for user in admin_accounts:
            print(f"  Email: {user['email']}")
            print(f"  Password: {user['password']}")
            print(f"  Role: {user['role']}")
            print()
    
    # Sample regular users
    regular_users = [u for u in created_users if u['role'] == 'user'][:5]
    if regular_users:
        print("👤 SAMPLE REGULAR USERS (5 đầu tiên):")
        for user in regular_users:
            status = []
            if user.get('verified'):
                status.append('✓ verified')
            else:
                status.append('✗ unverified')
            
            if user.get('active'):
                status.append('active')
            else:
                status.append('inactive')
            
            print(f"  Email: {user['email']}")
            print(f"  Username: {user['username']}")
            print(f"  Password: {user['password']}")
            print(f"  Status: {' | '.join(status)}")
            print()
    
    print(f"ℹ️  Tất cả users đều có password mặc định: 'password123'")
    print(f"{'='*60}\n")
    
    # Close connection
    cursor.close()
    connection.close()
    print("✓ Đã đóng kết nối database\n")
    
    return created_users

def clear_all_users(confirm=False):
    """Delete all users from database (careful!)"""
    if not confirm:
        print("⚠️  CẢNH BÁO: Lệnh này sẽ XÓA TẤT CẢ users trong database!")
        response = input("Bạn có chắc chắn muốn xóa? (yes/no): ")
        if response.lower() != 'yes':
            print("Đã hủy.")
            return
    
    connection = create_connection()
    cursor = connection.cursor()
    
    try:
        # Disable foreign key checks
        cursor.execute("SET FOREIGN_KEY_CHECKS = 0")
        
        # Delete all users
        cursor.execute("DELETE FROM users")
        deleted = cursor.rowcount
        
        # Reset auto increment
        cursor.execute("ALTER TABLE users AUTO_INCREMENT = 1")
        
        # Re-enable foreign key checks
        cursor.execute("SET FOREIGN_KEY_CHECKS = 1")
        
        connection.commit()
        print(f"✓ Đã xóa {deleted} users")
        
    except Error as e:
        print(f"✗ Lỗi: {e}")
        connection.rollback()
    
    finally:
        cursor.close()
        connection.close()

def count_users():
    """Count total users in database"""
    connection = create_connection()
    cursor = connection.cursor()
    
    try:
        cursor.execute("SELECT COUNT(*) FROM users")
        total = cursor.fetchone()[0]
        
        cursor.execute("SELECT COUNT(*) FROM users WHERE role = 'admin'")
        admins = cursor.fetchone()[0]
        
        cursor.execute("SELECT COUNT(*) FROM users WHERE role = 'moderator'")
        mods = cursor.fetchone()[0]
        
        cursor.execute("SELECT COUNT(*) FROM users WHERE role = 'user'")
        users = cursor.fetchone()[0]
        
        cursor.execute("SELECT COUNT(*) FROM users WHERE email_verified_at IS NOT NULL")
        verified = cursor.fetchone()[0]
        
        cursor.execute("SELECT COUNT(*) FROM users WHERE is_active = 1")
        active = cursor.fetchone()[0]
        
        print(f"\n{'='*60}")
        print(f"THỐNG KÊ USERS")
        print(f"{'='*60}")
        print(f"Tổng số users: {total}")
        print(f"  - Admins: {admins}")
        print(f"  - Moderators: {mods}")
        print(f"  - Regular users: {users}")
        print(f"  - Verified: {verified}")
        print(f"  - Active: {active}")
        print(f"{'='*60}\n")
        
    except Error as e:
        print(f"✗ Lỗi: {e}")
    
    finally:
        cursor.close()
        connection.close()

if __name__ == "__main__":
    # Check command line arguments
    if len(sys.argv) > 1:
        command = sys.argv[1]
        
        if command == "count":
            count_users()
        elif command == "clear":
            clear_all_users()
        elif command == "generate":
            # Parse arguments
            num_users = int(sys.argv[2]) if len(sys.argv) > 2 else 50
            num_admins = int(sys.argv[3]) if len(sys.argv) > 3 else 2
            verified_ratio = float(sys.argv[4]) if len(sys.argv) > 4 else 0.8
            active_ratio = float(sys.argv[5]) if len(sys.argv) > 5 else 0.95
            
            generate_users(num_users, num_admins, verified_ratio, active_ratio)
        else:
            print("Lệnh không hợp lệ!")
            print("\nCách sử dụng:")
            print("  python create_fake_users.py generate [num_users] [num_admins] [verified_ratio] [active_ratio]")
            print("  python create_fake_users.py count")
            print("  python create_fake_users.py clear")
    else:
        # Default: generate 50 users
        print("Sử dụng cấu hình mặc định...\n")
        generate_users(num_users=50, num_admins=2, verified_ratio=0.8, active_ratio=0.95)



