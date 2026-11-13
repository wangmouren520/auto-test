@echo off
chcp 65001 >nul
cd /d "%~dp0"

rem ================= environment =================
if not defined JAVA_HOME set "JAVA_HOME=C:\Users\HaiXu\Desktop\JDK_21"
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] JDK not found, edit JAVA_HOME in this file
    pause & exit /b 1
)

set "MVN=D:\Program Files\JetBrains\IntelliJ IDEA 2024.2.3\plugins\maven\lib\maven3\bin\mvn.cmd"
if not exist "%MVN%" (
    echo [ERROR] Maven not found, edit MVN in this file
    pause & exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"
rem keep console output as UTF-8, otherwise Chinese turns into garbage
set "MAVEN_OPTS=-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8"

rem ================= run =================
echo.
echo === internship system: register, then login, then apply ===
echo === Firefox opens one window per case                     ===
echo === each step is slowed to 1 second so you can watch      ===
echo === want it faster?  add  -Dslow=0                        ===
echo === want it slower?  add  -Dslow=2000                    ===
echo.
call "%MVN%" test %*
echo.
echo === done, check the "Tests run" line above ===
pause
