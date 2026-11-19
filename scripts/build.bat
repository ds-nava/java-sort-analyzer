@echo off
setlocal enabledelayedexpansion

for %%I in ("%~dp0..") do set "ROOT_DIR=%%~fI"
set "BUILD_MAIN=%ROOT_DIR%\build\classes"
set "BUILD_TEST=%ROOT_DIR%\build\test-classes"
set "DIST_DIR=%ROOT_DIR%\dist"
set "JAR_FILE=%DIST_DIR%\java-sort-analyzer.jar"

echo [1/4] Limpando diretorios de build...
if exist "%ROOT_DIR%\build" rmdir /s /q "%ROOT_DIR%\build"
if exist "%DIST_DIR%" rmdir /s /q "%DIST_DIR%"
mkdir "%BUILD_MAIN%"
mkdir "%BUILD_TEST%"
mkdir "%DIST_DIR%"

echo [2/4] Compilando codigo-fonte principal (Java 17)...
(for /f "delims=" %%F in ('dir /s /b "%ROOT_DIR%\src\main\java\*.java"') do (
  set "P=%%F"
  echo "!P:\=/!"
)) > "%ROOT_DIR%\build\sources.txt"
javac --release 17 -encoding UTF-8 -d "%BUILD_MAIN%" @"%ROOT_DIR%\build\sources.txt"
if errorlevel 1 exit /b 1

echo [3/4] Compilando e executando suite de testes...
(for /f "delims=" %%F in ('dir /s /b "%ROOT_DIR%\src\test\java\*.java"') do (
  set "P=%%F"
  echo "!P:\=/!"
)) > "%ROOT_DIR%\build\test-sources.txt"
javac --release 17 -encoding UTF-8 -cp "%BUILD_MAIN%" -d "%BUILD_TEST%" @"%ROOT_DIR%\build\test-sources.txt"
if errorlevel 1 exit /b 1
java -Djava.awt.headless=true -cp "%BUILD_MAIN%;%BUILD_TEST%" com.sortanalyzer.SuiteTestes
if errorlevel 1 exit /b 1

echo [4/4] Gerando JAR executavel...
jar --create --file "%JAR_FILE%" --main-class com.sortanalyzer.Main -C "%BUILD_MAIN%" .
if errorlevel 1 exit /b 1

echo.
echo Build concluido com sucesso!
echo JAR executavel: %JAR_FILE%
echo Para executar: java -jar "%JAR_FILE%"
