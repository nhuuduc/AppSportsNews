@echo off
chcp 65001 >nul
echo ========================================
echo    SPORTS NEWS CRAWLER SCHEDULER
echo    Tự động crawl theo lịch
echo ========================================
echo.

cd /d "%~dp0"

echo Đang kiểm tra Python...
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Python chưa được cài đặt!
    pause
    exit /b 1
)

echo Đang kiểm tra dependencies...
pip show schedule >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] Đang cài đặt dependencies...
    pip install -r requirements.txt
)

echo.
echo Bắt đầu scheduler...
echo Press Ctrl+C để dừng
echo.

python scheduler.py

pause

