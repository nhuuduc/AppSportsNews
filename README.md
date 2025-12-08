# 📱 Ứng Dụng Tin Tức Thể Thao

Ứng dụng Android đọc tin tức thể thao với giao diện hiện đại, được xây dựng bằng Kotlin và Jetpack Compose.

## 📋 Mục Lục

1. [Giới Thiệu](#giới-thiệu)
2. [Yêu Cầu Hệ Thống](#yêu-cầu-hệ-thống)
3. [Cấu Trúc Dự Án](#cấu-trúc-dự-án)
4. [Hướng Dẫn Cài Đặt](#hướng-dẫn-cài-đặt)
   - [Bước 1: Cài Đặt Phần Mềm Cần Thiết](#bước-1-cài-đặt-phần-mềm-cần-thiết)
   - [Bước 2: Cài Đặt Database (MySQL)](#bước-2-cài-đặt-database-mysql)
   - [Bước 3: Cài Đặt Backend API (PHP)](#bước-3-cài-đặt-backend-api-php)
   - [Bước 4: Cài Đặt Crawler (Python)](#bước-4-cài-đặt-crawler-python)
   - [Bước 5: Cài Đặt và Chạy Ứng Dụng Android](#bước-5-cài-đặt-và-chạy-ứng-dụng-android)
5. [Cách Sử Dụng](#cách-sử-dụng)
6. [Xử Lý Lỗi Thường Gặp](#xử-lý-lỗi-thường-gặp)

---

## 🎯 Giới Thiệu

Dự án này bao gồm:
- **Ứng dụng Android**: Ứng dụng đọc tin tức thể thao trên điện thoại Android
- **Backend API**: Server PHP cung cấp dữ liệu cho ứng dụng
- **Database**: MySQL lưu trữ tin tức và dữ liệu người dùng
- **Crawler**: Tool Python tự động thu thập tin tức từ các trang web

---

## 💻 Yêu Cầu Hệ Thống

### Để Chạy Ứng Dụng Android:
- **Hệ điều hành**: Windows 10/11, macOS, hoặc Linux
- **Android Studio**: Bản mới nhất (khuyến nghị Arctic Fox trở lên)
- **JDK**: Java Development Kit 11 hoặc cao hơn
- **RAM**: Tối thiểu 8GB (khuyến nghị 16GB)
- **Dung lượng ổ cứng**: Tối thiểu 10GB trống

### Để Chạy Backend API:
- **Web Server**: XAMPP, WAMP, hoặc bất kỳ server PHP nào
- **PHP**: Phiên bản 7.4 trở lên
- **MySQL**: Phiên bản 5.7 trở lên hoặc MariaDB 10.3 trở lên
- **phpMyAdmin**: (Tùy chọn, để quản lý database dễ dàng hơn)

### Để Chạy Crawler:
- **Python**: Phiên bản 3.8 trở lên
- **pip**: Package manager của Python

---

## 📁 Cấu Trúc Dự Án

```
AppSportsNews/
├── app/                    # Ứng dụng Android (Kotlin)
├── api/                    # Backend API (PHP)
│   ├── config/            # Cấu hình database, email, etc.
│   ├── controllers/       # Xử lý các request từ app
│   ├── models/            # Mô hình dữ liệu
│   └── routes.php         # Định tuyến API
├── crawler/               # Tool thu thập tin tức (Python)
└── uploads/              # Thư mục lưu ảnh (nếu cần)
```

---

## 🚀 Hướng Dẫn Cài Đặt

### Bước 1: Cài Đặt Phần Mềm Cần Thiết

#### 1.1. Cài Đặt Android Studio

1. Truy cập: https://developer.android.com/studio
2. Tải về bản cài đặt phù hợp với hệ điều hành của bạn
3. Chạy file cài đặt và làm theo hướng dẫn
4. Khi cài đặt, đảm bảo chọn:
   - ✅ Android SDK
   - ✅ Android SDK Platform
   - ✅ Android Virtual Device (AVD)
   - ✅ Performance (Intel HAXM) - nếu dùng Windows

#### 1.2. Cài Đặt XAMPP (Cho Backend API)

1. Truy cập: https://www.apachefriends.org/
2. Tải về XAMPP cho Windows
3. Chạy file cài đặt
4. Chọn cài đặt:
   - ✅ Apache
   - ✅ MySQL
   - ✅ PHP
   - ✅ phpMyAdmin
5. Cài đặt vào thư mục mặc định (thường là `C:\xampp`)

#### 1.3. Cài Đặt Python (Cho Crawler)

1. Truy cập: https://www.python.org/downloads/
2. Tải về Python 3.8 hoặc cao hơn
3. Chạy file cài đặt
4. **QUAN TRỌNG**: Đánh dấu chọn "Add Python to PATH" khi cài đặt
5. Hoàn tất cài đặt

---

### Bước 2: Cài Đặt Database (MySQL)

#### 2.1. Khởi Động MySQL

1. Mở **XAMPP Control Panel**
2. Nhấn nút **Start** bên cạnh **MySQL**
3. Đợi cho đến khi nút chuyển sang màu xanh (đang chạy)

#### 2.2. Tạo Database

1. Mở trình duyệt web
2. Truy cập: `http://localhost/phpmyadmin`
3. Nhấn vào tab **SQL** ở phía trên
4. Chạy các lệnh sau (copy và paste vào, rồi nhấn **Go**):

```sql
CREATE DATABASE IF NOT EXISTS sportsnews CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE sportsnews;

-- Tạo bảng users
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    avatar VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tạo bảng categories
CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tạo bảng articles
CREATE TABLE IF NOT EXISTS articles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    slug VARCHAR(500) UNIQUE NOT NULL,
    content TEXT NOT NULL,
    excerpt TEXT,
    featured_image VARCHAR(500),
    category_id INT,
    author_id INT,
    view_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    status ENUM('draft', 'published') DEFAULT 'published',
    published_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (author_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tạo user mặc định cho crawler
INSERT INTO users (id, username, email, password, full_name) 
VALUES (1, 'crawler_bot', 'crawler@example.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Crawler Bot')
ON DUPLICATE KEY UPDATE id=id;

-- Thêm một số categories mẫu
INSERT INTO categories (id, name, slug) VALUES
(1, 'Bóng Đá', 'bong-da'),
(2, 'Bóng Rổ', 'bong-ro'),
(3, 'Quần Vợt', 'quan-vot'),
(4, 'Võ Thuật', 'vo-thuat'),
(5, 'Đua Xe', 'dua-xe')
ON DUPLICATE KEY UPDATE id=id;
```

5. Nếu thành công, bạn sẽ thấy thông báo "Your SQL query has been executed successfully"

#### 2.3. Cấu Hình Database trong API

1. Mở file `api/config/database.php` bằng Notepad hoặc bất kỳ trình soạn thảo nào
2. Tìm và sửa các thông tin sau cho phù hợp với cài đặt của bạn:

```php
private $host = "localhost";        // Thường là "localhost"
private $db_name = "sportsnews";    // Tên database bạn vừa tạo
private $username = "root";          // Mặc định của XAMPP là "root"
private $password = "";              // Mặc định của XAMPP là rỗng (để trống)
```

3. Lưu file lại

---

### Bước 3: Cài Đặt Backend API (PHP)

#### 3.1. Copy Thư Mục API vào XAMPP

1. Mở thư mục XAMPP (thường là `C:\xampp`)
2. Mở thư mục `htdocs`
3. Tạo thư mục mới tên `sportsnews` (hoặc tên bạn muốn)
4. Copy toàn bộ thư mục `api` từ dự án vào `C:\xampp\htdocs\sportsnews\api`

**Cấu trúc sẽ như sau:**
```
C:\xampp\htdocs\sportsnews\
└── api\
    ├── config\
    ├── controllers\
    ├── models\
    └── index.php
```

#### 3.2. Khởi Động Apache

1. Mở **XAMPP Control Panel**
2. Nhấn nút **Start** bên cạnh **Apache**
3. Đợi cho đến khi nút chuyển sang màu xanh

#### 3.3. Kiểm Tra API Hoạt Động

1. Mở trình duyệt web
2. Truy cập: `http://localhost/sportsnews/api/`
3. Nếu thấy phản hồi JSON (có thể là thông báo lỗi hoặc thông tin API), nghĩa là API đã hoạt động

#### 3.4. Cấu Hình .htaccess (Nếu Cần)

Nếu API không hoạt động với URL sạch, bạn có thể cần file `.htaccess`. Tạo file `.htaccess` trong thư mục `api` với nội dung:

```apache
RewriteEngine On
RewriteCond %{REQUEST_FILENAME} !-f
RewriteCond %{REQUEST_FILENAME} !-d
RewriteRule ^(.*)$ index.php [QSA,L]
```

---

### Bước 4: Cài Đặt Crawler (Python)

#### 4.1. Cài Đặt Thư Viện Python

1. Mở **Command Prompt** (Windows) hoặc **Terminal** (Mac/Linux)
2. Di chuyển đến thư mục `crawler` trong dự án:

```bash
cd D:\Android\AppSportsNews\crawler
```

3. Cài đặt các thư viện cần thiết:

```bash
pip install requests beautifulsoup4 mysql-connector-python selenium
```

**Lưu ý**: Nếu lệnh `pip` không hoạt động, thử dùng `pip3` hoặc `python -m pip`

#### 4.2. Cấu Hình Crawler

1. Mở file `crawler/config.py` bằng Notepad hoặc trình soạn thảo
2. Sửa thông tin database cho phù hợp:

```python
DB_CONFIG = {
    'host': 'localhost',           # Thường là "localhost"
    'user': 'root',                 # Mặc định của XAMPP là "root"
    'password': '',                 # Mặc định của XAMPP là rỗng
    'database': 'sportsnews',      # Tên database bạn đã tạo
    'charset': 'utf8mb4'
}
```

3. Sửa API URL nếu cần:

```python
API_BASE_URL = 'http://localhost/sportsnews/api'  # URL của API bạn vừa cài
```

4. Lưu file lại

#### 4.3. Chạy Crawler (Tùy Chọn)

Bạn có thể chạy crawler để thu thập tin tức:

```bash
cd D:\Android\AppSportsNews\crawler
python crawler.py
```

---

### Bước 5: Cài Đặt và Chạy Ứng Dụng Android

#### 5.1. Mở Dự Án trong Android Studio

1. Khởi động **Android Studio**
2. Chọn **Open an Existing Project**
3. Duyệt đến thư mục `D:\Android\AppSportsNews`
4. Chọn thư mục `AppSportsNews` và nhấn **OK**
5. Đợi Android Studio đồng bộ dự án (có thể mất vài phút lần đầu)

#### 5.2. Cấu Hình API URL trong App

1. Trong Android Studio, tìm file chứa cấu hình API URL (thường là file `Constants.kt` hoặc `ApiConfig.kt`)
2. Sửa URL API cho phù hợp:

```kotlin
const val BASE_URL = "http://10.0.2.2/sportsnews/api/"  // Cho Android Emulator
// hoặc
const val BASE_URL = "http://192.168.1.XXX/sportsnews/api/"  // Cho thiết bị thật (thay XXX bằng IP máy tính)
```

**Lưu ý**:
- `10.0.2.2` là địa chỉ IP đặc biệt để Android Emulator truy cập localhost của máy tính
- Nếu chạy trên điện thoại thật, bạn cần dùng IP thật của máy tính (tìm bằng lệnh `ipconfig` trong Command Prompt)

#### 5.3. Tạo Android Virtual Device (AVD) - Nếu Chưa Có

1. Trong Android Studio, nhấn vào biểu tượng **Device Manager** (hoặc Tools > Device Manager)
2. Nhấn **Create Device**
3. Chọn một thiết bị (ví dụ: Pixel 5)
4. Chọn hệ điều hành Android (khuyến nghị API 24 trở lên)
5. Nhấn **Finish**

#### 5.4. Chạy Ứng Dụng

1. Đảm bảo AVD đã được khởi động hoặc điện thoại Android đã kết nối qua USB
2. Trong Android Studio, nhấn nút **Run** (▶️) hoặc nhấn phím **Shift + F10**
3. Chọn thiết bị bạn muốn chạy
4. Đợi ứng dụng được build và cài đặt (có thể mất vài phút lần đầu)

---

## 📱 Cách Sử Dụng

### Sử Dụng Ứng Dụng Android

1. Mở ứng dụng trên điện thoại/emulator
2. Ứng dụng sẽ tự động tải tin tức từ API
3. Bạn có thể:
   - Xem danh sách tin tức theo danh mục
   - Đọc chi tiết tin tức
   - Tìm kiếm tin tức
   - Xem video thể thao (nếu có)

### Chạy Crawler Để Cập Nhật Tin Tức

1. Mở Command Prompt/Terminal
2. Di chuyển đến thư mục crawler:

```bash
cd D:\Android\AppSportsNews\crawler
```

3. Chạy crawler:

```bash
python crawler.py
```

4. Crawler sẽ tự động thu thập tin tức và lưu vào database

---

## ⚠️ Xử Lý Lỗi Thường Gặp

### Lỗi: "Cannot connect to database"

**Nguyên nhân**: MySQL chưa được khởi động hoặc thông tin kết nối sai

**Giải pháp**:
1. Kiểm tra XAMPP Control Panel, đảm bảo MySQL đang chạy (nút màu xanh)
2. Kiểm tra lại thông tin trong `api/config/database.php`
3. Kiểm tra database `sportsnews` đã được tạo chưa

### Lỗi: "API not found" hoặc "404 Not Found"

**Nguyên nhân**: Apache chưa chạy hoặc đường dẫn API sai

**Giải pháp**:
1. Kiểm tra XAMPP Control Panel, đảm bảo Apache đang chạy
2. Kiểm tra file `api/index.php` có tồn tại không
3. Thử truy cập `http://localhost/sportsnews/api/` trên trình duyệt

### Lỗi: "App không kết nối được API"

**Nguyên nhân**: URL API sai hoặc firewall chặn

**Giải pháp**:
1. Nếu dùng Emulator: Đảm bảo URL là `http://10.0.2.2/sportsnews/api/`
2. Nếu dùng điện thoại thật:
   - Tìm IP máy tính bằng lệnh `ipconfig` (Windows) hoặc `ifconfig` (Mac/Linux)
   - Đảm bảo điện thoại và máy tính cùng mạng WiFi
   - Sử dụng IP thật trong URL: `http://192.168.1.XXX/sportsnews/api/`
3. Tắt Windows Firewall tạm thời để kiểm tra

### Lỗi: "Gradle sync failed"

**Nguyên nhân**: Thiếu dependencies hoặc kết nối internet

**Giải pháp**:
1. Đảm bảo có kết nối internet
2. Trong Android Studio, chọn **File > Sync Project with Gradle Files**
3. Nếu vẫn lỗi, chọn **File > Invalidate Caches / Restart**

### Lỗi: "pip is not recognized"

**Nguyên nhân**: Python chưa được thêm vào PATH

**Giải pháp**:
1. Gỡ cài đặt Python
2. Cài đặt lại Python và **đánh dấu chọn "Add Python to PATH"**
3. Hoặc thêm Python vào PATH thủ công

### Lỗi: "Module not found" khi chạy crawler

**Nguyên nhân**: Chưa cài đặt thư viện Python

**Giải pháp**:
```bash
pip install requests beautifulsoup4 mysql-connector-python selenium
```

---

## 📞 Hỗ Trợ

Nếu gặp vấn đề không được liệt kê ở trên, vui lòng:
1. Kiểm tra file log trong thư mục `api/logs/` và `crawler/logs/`
2. Kiểm tra console của Android Studio để xem lỗi chi tiết
3. Đảm bảo tất cả các service (Apache, MySQL) đang chạy

---

## 📝 Lưu Ý Quan Trọng

1. **Bảo mật**: File `api/config/database.php` chứa thông tin nhạy cảm. Không commit file này lên Git công khai.

2. **Firewall**: Nếu chạy app trên điện thoại thật, có thể cần tắt Windows Firewall hoặc thêm exception cho Apache.

3. **Port**: Đảm bảo port 80 (Apache) và 3306 (MySQL) không bị ứng dụng khác sử dụng.

4. **Backup**: Thường xuyên backup database để tránh mất dữ liệu.

---

**Chúc bạn thành công! 🎉**

