@echo off
chcp 65001 >nul
title ShopNow - Start All

echo ============================================
echo   ShopNow - Start All
echo ============================================
echo.
echo   This will start Redis, backend, and frontend.
echo   Three new windows will open - do NOT close them!
echo.
echo   After startup, visit: http://localhost:5173
echo   Admin panel: http://localhost:5173/admin/login
echo.
echo   Test accounts:
echo     Admin - admin / 11111111
echo     User  - user1 / 123456
echo ============================================
echo.

cd /d "%~dp0"

echo [1/3] Starting Redis...
REM Check if Redis is already running on port 6379
netstat -ano | findstr ":6379" | findstr "LISTENING" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo   Redis is already running on port 6379.
) else (
    echo   Starting Redis from redis-local\redis-server.exe...
    start "Redis" /d "%~dp0redis-local" cmd /k "redis-server.exe redis.windows.conf"
    echo   Redis started in new window.
)

echo [2/3] Starting backend...
start "ECommerce - Backend" /d "ecommerce-backend-java" cmd /k "mvn spring-boot:run"

timeout /t 10 /nobreak >nul

echo [3/3] Starting frontend...
start "ECommerce - Frontend" /d "ecommerce-frontend" cmd /k "npm run dev"

echo.
echo ============================================
echo   Wait 1-2 minutes then open the site.
echo.
echo   Backend ready when: Started EcommerceApplication
echo   Frontend ready when: Local: http://localhost:5173/
echo ============================================
echo.
echo   Press any key to close this window (services keep running).
pause >nul
