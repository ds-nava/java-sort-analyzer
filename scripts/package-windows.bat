@echo off
setlocal

for %%I in ("%~dp0..") do set "ROOT_DIR=%%~fI"
call "%ROOT_DIR%\scripts\build.bat"
if errorlevel 1 exit /b 1

set "INPUT_DIR=%ROOT_DIR%\build\jpackage-input"
set "OUT_DIR=%ROOT_DIR%\dist\package-windows"

if exist "%INPUT_DIR%" rmdir /s /q "%INPUT_DIR%"
mkdir "%INPUT_DIR%"
copy /y "%ROOT_DIR%\dist\java-sort-analyzer.jar" "%INPUT_DIR%\java-sort-analyzer.jar" >nul
if errorlevel 1 exit /b 1

if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
mkdir "%OUT_DIR%"

REM Gera pasta executavel portatil (.exe + JRE embutido) sem exigir WiX Toolset.
REM Para gerar instalador unico (.exe/.msi), troque --type app-image por --type exe (requer WiX Toolset 3.x).
echo Empacotando aplicativo Windows com Java embutido via jpackage...
jpackage ^
  --type app-image ^
  --name "JavaSortAnalyzer" ^
  --app-version "1.0.0" ^
  --vendor "Java Sort Analyzer" ^
  --description "Sistema para Analise de Performance e Visualizacao de Algoritmos de Ordenacao" ^
  --input "%INPUT_DIR%" ^
  --main-jar "java-sort-analyzer.jar" ^
  --main-class "com.sortanalyzer.Main" ^
  --dest "%OUT_DIR%"

if errorlevel 1 exit /b 1
echo.
echo Pacote Windows criado em: %OUT_DIR%\JavaSortAnalyzer\JavaSortAnalyzer.exe
