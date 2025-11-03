@echo off
echo ========================================
echo TEST FAKE USERS WITH API
echo ========================================
echo.

REM Install test dependencies
echo [INFO] Cai dat dependencies...
python -m pip install -r test_requirements.txt >nul 2>&1

echo Chon loai test:
echo   1. Test admin/moderator accounts
echo   2. Test random users (5 users)
echo   3. Test tat ca
echo   4. Thoat
echo.

set /p choice="Nhap lua chon (1-4): "

if "%choice%"=="1" (
    python test_users.py admin
    pause
    exit /b 0
)

if "%choice%"=="2" (
    python test_users.py users
    pause
    exit /b 0
)

if "%choice%"=="3" (
    python test_users.py all
    pause
    exit /b 0
)

if "%choice%"=="4" (
    exit /b 0
)

echo Lua chon khong hop le!
pause



