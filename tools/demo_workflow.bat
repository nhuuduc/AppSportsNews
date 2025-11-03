@echo off
chcp 65001 >nul
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║          FAKE USER GENERATOR - DEMO WORKFLOW                  ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo Workflow này sẽ:
echo   1. Cài đặt dependencies
echo   2. Tạo 20 fake users (2 admin, 18 users)
echo   3. Hiển thị thống kê
echo   4. Test users với API
echo.
pause
echo.

echo ╔════════════════════════════════════════════════════════════════╗
echo ║ BƯỚC 1: CÀI ĐẶT DEPENDENCIES                                  ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
python -m pip install --upgrade pip --quiet
python -m pip install -r requirements.txt --quiet
python -m pip install -r test_requirements.txt --quiet
echo ✓ Dependencies đã được cài đặt
echo.
pause

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║ BƯỚC 2: TẠO FAKE USERS                                        ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
python create_fake_users.py generate 20 2 0.8 0.95
echo.
pause

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║ BƯỚC 3: HIỂN THỊ THỐNG KÊ                                     ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
python create_fake_users.py count
echo.
pause

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║ BƯỚC 4: TEST USERS VỚI API                                    ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo ⚠️  LƯU Ý: Đảm bảo backend API đang chạy ở http://localhost/api
echo.
set /p continue="Tiếp tục test? (y/n): "
if /i "%continue%" NEQ "y" goto :end
echo.
python test_users.py all
echo.

:end
echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║ DEMO HOÀN TẤT!                                                ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
echo Bạn có thể:
echo   - Chạy setup_and_run.bat để tạo thêm users
echo   - Chạy run_test.bat để test lại
echo   - Sử dụng trực tiếp: python create_fake_users.py
echo.
pause



