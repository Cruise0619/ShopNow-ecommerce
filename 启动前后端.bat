@echo off
chcp 65001 >nul
title ShopNow - Start All

echo ============================================
echo   ShopNow - Start All
echo ============================================
echo.
echo   This will start both backend and frontend.
echo   Two new windows will open - do NOT close them!
echo.
echo   Backend window : ECommerce - Backend
echo   Frontend window: ECommerce - Frontend
echo.
echo   After startup, visit: http://localhost:5173
echo   Admin panel: http://localhost:5173/admin/login
echo.
echo   Test accounts:
echo     Admin - admin / admin123
echo     User  - user1 / 123456
echo ============================================
echo.

cd /d "%~dp0"

echo [1/2] Starting backend...
start "ECommerce - Backend" /d "ecommerce-backend-java" cmd /k "mvn spring-boot:run"

echo [2/2] Starting frontend...
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
