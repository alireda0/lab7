@echo off
title Skill Forge - Learning Platform

REM Get the directory where this batch file is located
cd /d "%~dp0"

REM Navigate to the dist folder
cd dist

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo Java is not installed or not in PATH!
    echo Please install Java to run this application.
    pause
    exit /b
)

REM Check if JAR file exists
if not exist "SkillForge.jar" (
    echo SkillForge.jar not found!
    echo Please build the project first in NetBeans.
    pause
    exit /b
)

REM Run the application
echo Starting Skill Forge...
start javaw -jar SkillForge.jar

REM Exit without waiting
exit