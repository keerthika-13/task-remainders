@echo off
echo Compiling Task Reminder backend...
cd /d "%~dp0"

if not exist "target\classes" mkdir "target\classes"

echo Copying resources...
xcopy /Y /Q "src\main\resources\*" "target\classes\" >nul

echo Compiling Java sources...
javac -cp "lib/mysql-connector-j-8.4.0.jar" -d "target/classes" -encoding UTF-8 "@srclist.txt"

if %ERRORLEVEL% == 0 (
    echo.
    echo [OK] Compilation successful!
) else (
    echo.
    echo [ERROR] Compilation failed. See errors above.
)
pause
