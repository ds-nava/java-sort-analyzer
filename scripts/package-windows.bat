@echo off
setlocal

set ROOT_DIR=%~dp0..
call "%ROOT_DIR%\scripts\build.bat"
if errorlevel 1 exit /b 1

set OUT_DIR=%ROOT_DIR%\dist\package-windows
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
  --input "%ROOT_DIR%\dist" ^
  --main-jar "java-sort-analyzer.jar" ^
  --main-class "com.sortanalyzer.Main" ^
  --dest "%OUT_DIR%"

if errorlevel 1 exit /b 1
echo.
echo Pacote Windows criado em: %OUT_DIR%\JavaSortAnalyzer\JavaSortAnalyzer.exe
