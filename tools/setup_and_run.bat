@echo off
echo ========================================
echo SETUP VA CHAY FAKE USER GENERATOR
echo ========================================
echo.

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python khong duoc cai dat!
    echo Vui long cai dat Python tu https://www.python.org/
    pause
    exit /b 1
)

echo [INFO] Kiem tra va cai dat dependencies...
echo.

REM Install dependencies
python -m pip install --upgrade pip
python -m pip install -r requirements.txt

if errorlevel 1 (
    echo.
    echo [ERROR] Loi khi cai dat dependencies!
    pause
    exit /b 1
)

echo.
echo ========================================
echo CAI DAT THANH CONG!
echo ========================================
echo.
echo Chon tuy chon:
echo   1. Tao 50 users mac dinh (2 admin, 48 users)
echo   2. Tao so luong tuy chinh
echo   3. Xem thong ke users hien tai
echo   4. Xoa tat ca users
echo   5. Thoat
echo.

set /p choice="Nhap lua chon (1-5): "

if "%choice%"=="1" (
    echo.
    echo Dang tao 50 users...
    python create_fake_users.py generate 50 2 0.8 0.95
    pause
    exit /b 0
)

if "%choice%"=="2" (
    echo.
    set /p num_users="Nhap so luong users can tao: "
    set /p num_admins="Nhap so luong admin/mod (0-2): "
    echo.
    echo Dang tao %num_users% users va %num_admins% admin/mod...
    python create_fake_users.py generate %num_users% %num_admins% 0.8 0.95
    pause
    exit /b 0
)

if "%choice%"=="3" (
    echo.
    python create_fake_users.py count
    pause
    exit /b 0
)

if "%choice%"=="4" (
    echo.
    python create_fake_users.py clear
    pause
    exit /b 0
)

if "%choice%"=="5" (
    exit /b 0
)

echo Lua chon khong hop le!
pause



