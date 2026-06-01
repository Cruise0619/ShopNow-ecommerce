@echo off
chcp 65001 >nul
title ECommerce - Frontend (Vue3)

echo ============================================
echo   ShopNow - Frontend
echo ============================================
echo.

cd /d "%~dp0ecommerce-frontend"

echo [Check] Node.js...
node -v 2>nul
if %errorlevel% neq 0 (
    echo [Error] Node.js not found! Please install Node.js.
    echo         https://nodejs.org/ (LTS version)
    pause
    exit /b 1
)

echo [Check] Dependencies...
if not exist "node_modules" (
    echo [Install] Installing dependencies, please wait...
    call npm install
    if %errorlevel% neq 0 (
        echo [Error] npm install failed. Check your network and try again.
        pause
        exit /b 1
    )
    echo [Done] Dependencies installed!
)

echo.
echo [Start] Starting Vue3 dev server...
echo         Look for "Local: http://localhost:5173/" - that means success!
echo.

npm run dev

echo.
echo ============================================
echo   Frontend stopped.
echo ============================================
pause
