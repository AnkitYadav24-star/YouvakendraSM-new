@echo off
if not exist "javafx-sdk-21.0.2" (
    echo JavaFX SDK not found. Running setup.ps1...
    powershell -ExecutionPolicy Bypass -File setup.ps1
)
if not exist "bin" mkdir bin
echo Compiling source files...
javac --module-path javafx-sdk-21.0.2\lib --add-modules javafx.controls,javafx.fxml -d bin *.java
if %errorlevel% neq 0 (
    echo Compilation failed.
    pause
    exit /b %errorlevel%
)
copy /y styles.css bin\styles.css >nul
if not exist "bin\assets" mkdir bin\assets
copy /y assets\* bin\assets >nul
echo Packaging application into YouvakendraSM.jar...
jar --create --file YouvakendraSM.jar --main-class Main -C bin .
echo Running application from JAR...
java --module-path javafx-sdk-21.0.2\lib --add-modules javafx.controls,javafx.fxml -jar YouvakendraSM.jar
