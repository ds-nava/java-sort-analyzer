@echo off
setlocal enabledelayedexpansion

for %%I in ("%~dp0..") do set "ROOT_DIR=%%~fI"
set "DIST_DIR=%ROOT_DIR%\dist"
set "JAR_FILE=%DIST_DIR%\java-sort-analyzer.jar"

where mvn >nul 2>nul
if %errorlevel%==0 (
  echo [1/3] Compilando e empacotando com Maven ^(FlatLaf + fonte Inter^)...
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
  goto :sucesso
)

echo Maven nao encontrado. Usando compilacao direta com JDK ^(javac + jar^)...

set "DEPS_DIR=%ROOT_DIR%\.deps"
set "FLATLAF_JAR=%DEPS_DIR%\flatlaf-3.5.4.jar"
set "INTER_JAR=%DEPS_DIR%\flatlaf-fonts-inter-4.1.jar"
set "BUILD_MAIN=%ROOT_DIR%\build\classes"
set "BUILD_TEST=%ROOT_DIR%\build\test-classes"
set "BUILD_FAT=%ROOT_DIR%\build\fatjar"

if not exist "%DEPS_DIR%" mkdir "%DEPS_DIR%"

if not exist "%FLATLAF_JAR%" (
  echo [0/4] Baixando flatlaf-3.5.4.jar do Maven Central...
  curl.exe -fsSL -o "%FLATLAF_JAR%" "https://repo1.maven.org/maven2/com/formdev/flatlaf/3.5.4/flatlaf-3.5.4.jar"
  if errorlevel 1 (
    powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/formdev/flatlaf/3.5.4/flatlaf-3.5.4.jar' -OutFile '%FLATLAF_JAR%'"
    if errorlevel 1 exit /b 1
  )
)

if not exist "%INTER_JAR%" (
  echo [0/4] Baixando flatlaf-fonts-inter-4.1.jar do Maven Central...
  curl.exe -fsSL -o "%INTER_JAR%" "https://repo1.maven.org/maven2/com/formdev/flatlaf-fonts-inter/4.1/flatlaf-fonts-inter-4.1.jar"
  if errorlevel 1 (
    powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/formdev/flatlaf-fonts-inter/4.1/flatlaf-fonts-inter-4.1.jar' -OutFile '%INTER_JAR%'"
    if errorlevel 1 exit /b 1
  )
)

echo [1/4] Limpando diretorios de build...
if exist "%ROOT_DIR%\build" rmdir /s /q "%ROOT_DIR%\build"
if exist "%DIST_DIR%" rmdir /s /q "%DIST_DIR%"
mkdir "%BUILD_MAIN%"
mkdir "%BUILD_TEST%"
mkdir "%BUILD_FAT%"
mkdir "%DIST_DIR%"

set "DEPS_CP=%FLATLAF_JAR%;%INTER_JAR%"

echo [2/4] Compilando codigo-fonte principal ^(Java 17^)...
(for /f "delims=" %%F in ('dir /s /b "%ROOT_DIR%\src\main\java\*.java"') do (
  set "P=%%F"
  echo "!P:\=/!"
)) > "%ROOT_DIR%\build\sources.txt"
javac --release 17 -encoding UTF-8 -cp "%DEPS_CP%" -d "%BUILD_MAIN%" @"%ROOT_DIR%\build\sources.txt"
if errorlevel 1 exit /b 1

echo [3/4] Compilando e executando suite de testes...
(for /f "delims=" %%F in ('dir /s /b "%ROOT_DIR%\src\test\java\*.java"') do (
  set "P=%%F"
  echo "!P:\=/!"
)) > "%ROOT_DIR%\build\test-sources.txt"
javac --release 17 -encoding UTF-8 -cp "%BUILD_MAIN%;%DEPS_CP%" -d "%BUILD_TEST%" @"%ROOT_DIR%\build\test-sources.txt"
if errorlevel 1 exit /b 1
java -Djava.awt.headless=true -cp "%BUILD_MAIN%;%BUILD_TEST%;%DEPS_CP%" com.sortanalyzer.SuiteTestes
if errorlevel 1 exit /b 1

echo [4/4] Gerando JAR executavel autocontido ^(com FlatLaf + Inter^)...
pushd "%BUILD_FAT%"
jar --extract --file "%FLATLAF_JAR%"
if errorlevel 1 ( popd & exit /b 1 )
jar --extract --file "%INTER_JAR%"
if errorlevel 1 ( popd & exit /b 1 )
if exist "META-INF\MANIFEST.MF" del /q "META-INF\MANIFEST.MF"
if exist "META-INF\*.SF" del /q "META-INF\*.SF"
if exist "META-INF\*.DSA" del /q "META-INF\*.DSA"
if exist "META-INF\*.RSA" del /q "META-INF\*.RSA"
xcopy /E /I /Q /Y "%BUILD_MAIN%\*" "%BUILD_FAT%\" >nul
if errorlevel 1 ( popd & exit /b 1 )
popd
jar --create --file "%JAR_FILE%" --main-class com.sortanalyzer.Main -C "%BUILD_FAT%" .
if errorlevel 1 exit /b 1

:sucesso
echo.
echo Build concluido com sucesso!
echo JAR executavel: %JAR_FILE%
echo Para executar: java -jar "%JAR_FILE%"
