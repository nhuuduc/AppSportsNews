@echo off
chcp 65001 >nul
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║          COMPREHENSIVE BACKEND API TESTING                    ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Install test dependencies
echo [INFO] Đang cài đặt dependencies...
python -m pip install -r test_requirements.txt >nul 2>&1

echo.
echo Chọn loại test:
echo   1. Test tất cả endpoints (Public + Authenticated)
echo   2. Test chỉ Public endpoints (không cần login)
echo   3. Test chỉ Authenticated endpoints (cần login)
echo   4. Test với user cụ thể
echo   5. Thoát
echo.

set /p choice="Nhập lựa chọn (1-5): "

if "%choice%"=="1" (
    echo.
    echo 🚀 Đang chạy tất cả tests...
    echo.
    python test_backend_features.py all
    goto :end
)

if "%choice%"=="2" (
    echo.
    echo 🌐 Đang test public endpoints...
    echo.
    python test_backend_features.py public
    goto :end
)

if "%choice%"=="3" (
    echo.
    echo 🔒 Đang test authenticated endpoints...
    echo.
    python test_backend_features.py auth
    goto :end
)

if "%choice%"=="4" (
    echo.
    set /p email="Nhập email: "
    set /p password="Nhập password (Enter = password123): "
    if "%password%"=="" set password=password123
    echo.
    echo 🔒 Đang test với user %email%...
    echo.
    python test_backend_features.py auth %email% %password%
    goto :end
)

if "%choice%"=="5" (
    exit /b 0
)

echo Lựa chọn không hợp lệ!

:end
echo.
pause

