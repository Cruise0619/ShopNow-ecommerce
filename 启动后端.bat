@echo off
chcp 65001 >nul
title ECommerce - Backend (Spring Boot)

echo ============================================
echo   ShopNow - Backend
echo ============================================
echo.

cd /d "%~dp0ecommerce-backend-java"

echo [Check] Java...
java -version 2>nul
if %errorlevel% neq 0 (
    echo [Error] Java not found! Please install JDK 17.
    echo         https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

echo [Check] Maven...
mvn -v 2>nul | findstr "Apache Maven" >nul
if %errorlevel% neq 0 (
    echo [Error] Maven not found! Please install Maven.
    echo         https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo [Check] application.yml...
if not exist "src\main\resources\application.yml" (
    echo [Error] application.yml not found!
    pause
    exit /b 1
)

echo.
echo [Start] Starting Spring Boot...
echo         First run may take 3-5 minutes to download dependencies.
echo         Look for "Started EcommerceApplication" - that means success!
echo.

mvn spring-boot:run

echo.
echo ============================================
echo   Backend stopped.
echo ============================================
pause
