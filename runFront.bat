@echo off
cd /d "%~dp0frontend"
echo Starting frontend server on http://localhost:3000
node server.js
pause