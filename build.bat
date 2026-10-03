@echo off
echo ====================================================================
echo  Compiling "sixseven" Personal Expense Manager (3-Member Division)
echo ====================================================================

if not exist out mkdir out

javac -encoding UTF-8 -cp ".;lib\ojdbc8.jar" -d out module_db_dao\*.java module_core_logic\*.java module_ui\*.java Main.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed! Check syntax errors above.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [SUCCESS] Build succeeded! All 3 modules compiled cleanly into 'out\'
echo Launching sixseven Personal Expense Manager...
echo.

java -cp "out;lib\ojdbc8.jar" Main
