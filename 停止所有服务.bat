@echo off
chcp 65001 >nul
title ShopNow - Stop All

echo ============================================
echo   ShopNow - Stop All Services
echo ============================================
echo.

cd /d "%~dp0"

:: ====== Redis (port 6379) ======
echo [1/3] Stopping Redis (port 6379)...
set FOUND=
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":6379" ^| findstr "LISTENING"') do (
    set FOUND=1
    taskkill /F /PID %%a >nul 2>&1
    echo   Killed PID %%a
)
if not defined FOUND echo   No Redis process found.

:: ====== Backend (port 8080) ======
echo [2/3] Stopping Backend (port 8080)...
set FOUND=
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":8080" ^| findstr "LISTENING"') do (
    set FOUND=1
    taskkill /F /PID %%a >nul 2>&1
    echo   Killed PID %%a
)
if not defined FOUND echo   No backend process found.

:: ====== Frontend (port 5173) ======
echo [3/3] Stopping Frontend (port 5173)...
set FOUND=
for /f "tokens=5" %%a in ('netstat -ano 2^>nul ^| findstr ":5173" ^| findstr "LISTENING"') do (
    set FOUND=1
    taskkill /F /PID %%a >nul 2>&1
    echo   Killed PID %%a
)
if not defined FOUND echo   No frontend process found.

echo.
echo ============================================
echo   All services stopped.
echo ============================================
echo.
echo   Press any key to close this window.
pause >nul
