@echo off
echo Dang don dep project de phat hanh...

REM Xoa thu muc target
if exist "target" (
    echo Dang xoa thu muc target...
    rmdir /s /q "target"
)

REM Xoa file IDE
if exist ".idea" (
    echo Dang xoa thu muc .idea...
    rmdir /s /q ".idea"
)
if exist ".vscode" (
    echo Dang xoa thu muc .vscode...
    rmdir /s /q ".vscode"
)
if exist "*.iml" (
    echo Dang xoa file .iml...
    del /q "*.iml"
)

REM Xoa thu muc uploads
if exist "uploads" (
    echo Dang xoa thu muc uploads...
    rmdir /s /q "uploads"
)

REM Xoa log files
if exist "logs" (
    echo Dang xoa thu muc logs...
    rmdir /s /q "logs"
)
if exist "*.log" (
    echo Dang xoa file log...
    del /q "*.log"
)

REM Xoa temp files
if exist "*.tmp" (
    echo Dang xoa file temp...
    del /q "*.tmp"
)
if exist "temp" (
    echo Dang xoa thu muc temp...
    rmdir /s /q "temp"
)

REM Xoa file config local
if exist "src\main\resources\application-local.yml" (
    echo Dang xoa file config local...
    del /q "src\main\resources\application-local.yml"
)
if exist "src\main\resources\application-production.yml" (
    echo Dang xoa file config production...
    del /q "src\main\resources\application-production.yml"
)
if exist "src\main\resources\application-dev.yml" (
    echo Dang xoa file config dev...
    del /q "src\main\resources\application-dev.yml"
)

REM Xoa file backup
if exist "*.bak" (
    echo Dang xoa file backup...
    del /q "*.bak"
)

echo Don dep hoan thanh!
echo.
echo Luu y: Truoc khi zip, hay dam bao:
echo - Da tao file README.md voi huong dan day du
echo - Da tao file application-template.yml de huong dan setup
echo - Da commit tat ca thay doi quan trong
echo.
pause
