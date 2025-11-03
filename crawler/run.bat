@echo off
chcp 65001 >nul
echo ========================================
echo    SPORTS NEWS CRAWLER
echo ========================================
echo.

cd /d "%~dp0"

echo Đang kiểm tra Python...
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Python chưa được cài đặt!
    echo Vui lòng cài đặt Python 3.7+ từ https://www.python.org/
    pause
    exit /b 1
)

echo Đang kiểm tra dependencies...
pip show requests >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] Đang cài đặt dependencies...
    pip install -r requirements.txt
)

echo.
echo Bắt đầu crawl...
echo.

python crawler.py

echo.
echo ========================================
echo    Hoàn thành!
echo ========================================
pause

