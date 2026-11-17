@echo off
echo Starting Task Reminder backend...
cd /d "%~dp0"

java -cp "target/classes;lib/mysql-connector-j-8.4.0.jar" com.taskreminder.Main
pause
