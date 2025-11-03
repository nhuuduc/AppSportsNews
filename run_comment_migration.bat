@echo off
echo ================================================
echo Comment Like Migration - Add liked_user_ids column
echo ================================================
echo.

REM Tìm MySQL từ XAMPP hoặc cài đặt riêng
set MYSQL_PATH=
if exist "C:\xampp\mysql\bin\mysql.exe" (
    set MYSQL_PATH=C:\xampp\mysql\bin\mysql.exe
) else if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
    set MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe
) else if exist "C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe" (
    set MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe
)

if "%MYSQL_PATH%"=="" (
    echo ERROR: MySQL not found!
    echo Please check MySQL installation path.
    echo.
    echo Common paths:
    echo - C:\xampp\mysql\bin\mysql.exe
    echo - C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe
    echo - C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe
    echo.
    pause
    exit /b 1
)

echo MySQL found at: %MYSQL_PATH%
echo.
echo Running migration...
echo.

REM Chạy migration (sẽ hỏi password)
"%MYSQL_PATH%" -u root -p sports_news_db_v2 < api\database\add_comment_likes_column.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ================================================
    echo Migration completed successfully!
    echo ================================================
    echo.
    echo Changes applied:
    echo - Added 'liked_user_ids' column to 'comments' table
    echo - Added index 'idx_like_count' for better performance
    echo.
    echo Next steps:
    echo 1. The API is ready to handle comment likes
    echo 2. Build and run your Android app
    echo 3. Test liking comments
    echo.
) else (
    echo.
    echo ================================================
    echo ERROR: Migration failed!
    echo ================================================
    echo Please check the error message above.
    echo.
)

pause

