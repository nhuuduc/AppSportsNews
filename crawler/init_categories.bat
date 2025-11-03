@echo off
chcp 65001 >nul
echo ========================================
echo    KHỞI TẠO CATEGORIES
echo ========================================
echo.

cd /d "%~dp0"

echo [INFO] Đang khởi tạo categories...
echo.

python init_categories.py

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo [SUCCESS] Khởi tạo thành công!
    echo ========================================
    echo.
    echo Bạn có thể chạy crawler ngay bây giờ:
    echo   - Chạy run.bat để crawl thủ công
    echo   - Chạy run_scheduler.bat để crawl tự động
    echo.
) else (
    echo.
    echo ========================================
    echo [ERROR] Khởi tạo thất bại!
    echo ========================================
    echo.
    echo Vui lòng kiểm tra:
    echo 1. MySQL đang chạy
    echo 2. Database 'sports_news_db_v2' đã được tạo
    echo 3. Thông tin trong config.py đúng
    echo.
)

pause
