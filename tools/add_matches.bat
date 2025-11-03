@echo off
chcp 65001 > nul
echo ========================================
echo   THÊM TRẬN ĐẤU MẪU VÀO DATABASE
echo ========================================
echo.

cd /d "%~dp0"

php add_sample_matches.php

echo.
pause

























