@echo off
setlocal enabledelayedexpansion

for %%I in ("%~dp0..") do set "ROOT_DIR=%%~fI"
set "DIST_DIR=%ROOT_DIR%\dist"
set "JAR_FILE=%DIST_DIR%\java-sort-analyzer.jar"

echo [1/3] Compilando e empacotando com Maven (inclui FlatLaf + fonte Inter)...
pushd "%ROOT_DIR%"
call mvn -q clean package
if errorlevel 1 (
  popd
  exit /b 1
)

echo [2/3] Executando suite de testes...
java -Djava.awt.headless=true -cp "%ROOT_DIR%\target\classes;%ROOT_DIR%\target\test-classes" com.sortanalyzer.SuiteTestes
if errorlevel 1 (
  popd
  exit /b 1
)

echo [3/3] Copiando JAR executavel para dist\...
if not exist "%DIST_DIR%" mkdir "%DIST_DIR%"
copy /Y "%ROOT_DIR%\target\java-sort-analyzer.jar" "%JAR_FILE%" >nul
popd

echo.
echo Build concluido com sucesso!
echo JAR executavel: %JAR_FILE%
echo Para executar: java -jar "%JAR_FILE%"
