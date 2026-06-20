@echo off
if not exist "javafx-sdk-21.0.2" (
    echo JavaFX SDK not found. Running setup.ps1...
    powershell -ExecutionPolicy Bypass -File setup.ps1
)
if not exist "lib" (
    echo Library dependencies not found. Downloading...
    powershell -ExecutionPolicy Bypass -File download_libs.ps1
)
if not exist "bin" mkdir bin
echo Compiling source files...
javac --module-path javafx-sdk-21.0.2\lib --add-modules javafx.controls,javafx.fxml --class-path "lib\*" -d bin *.java
if %errorlevel% neq 0 (
    echo Compilation failed.
    pause
    exit /b %errorlevel%
)
copy /y styles.css bin\styles.css >nul
if not exist "bin\assets" mkdir bin\assets
copy /y assets\* bin\assets >nul

echo Generating manifest with classpath...
echo Manifest-Version: 1.0>> manifest.txt
echo Main-Class: Main>> manifest.txt
echo Class-Path: lib/google-api-services-sheets-v4-rev20220927-2.0.0.jar>> manifest.txt
echo   lib/google-api-client-2.2.0.jar>> manifest.txt
echo   lib/google-oauth-client-1.34.1.jar>> manifest.txt
echo   lib/google-http-client-1.43.3.jar>> manifest.txt
echo   lib/google-http-client-gson-1.43.3.jar>> manifest.txt
echo   lib/gson-2.10.1.jar>> manifest.txt
echo   lib/google-auth-library-credentials-1.19.0.jar>> manifest.txt
echo   lib/google-auth-library-oauth2-http-1.19.0.jar>> manifest.txt
echo   lib/guava-32.1.2-jre.jar>> manifest.txt
echo   lib/failureaccess-1.0.1.jar>> manifest.txt
echo   lib/opencensus-api-0.31.1.jar>> manifest.txt
echo   lib/opencensus-contrib-http-util-0.31.1.jar>> manifest.txt
echo   lib/httpclient-4.5.14.jar>> manifest.txt
echo   lib/httpcore-4.4.16.jar>> manifest.txt
echo   lib/commons-logging-1.2.jar>> manifest.txt
echo   lib/commons-codec-1.15.jar>> manifest.txt
echo   lib/grpc-context-1.53.0.jar>> manifest.txt
echo.>> manifest.txt

echo Packaging application into YouvakendraSM.jar...
jar --create --file YouvakendraSM.jar --manifest manifest.txt -C bin .
del manifest.txt

echo Running application from JAR...
java --module-path javafx-sdk-21.0.2\lib --add-modules javafx.controls,javafx.fxml --class-path "YouvakendraSM.jar;lib\*" Main
