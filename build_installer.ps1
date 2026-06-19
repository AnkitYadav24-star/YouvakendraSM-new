# 1. Compile Java files and build JAR
Write-Host "Compiling Java files..." -ForegroundColor Cyan
javac --module-path javafx-sdk-21.0.2\lib --add-modules javafx.controls,javafx.fxml -d bin *.java
if ($LASTEXITCODE -ne 0) {
    Write-Error "Java compilation failed."
    exit 1
}

if (-not (Test-Path bin/assets)) { New-Item -ItemType Directory -Path bin/assets | Out-Null }
Copy-Item styles.css bin/styles.css -Force
Copy-Item assets/* bin/assets/ -Force

Write-Host "Packaging JAR file..." -ForegroundColor Cyan
jar --create --file YouvakendraSM.jar --main-class Main -C bin .

# 2. Build JPackage application image
Write-Host "Building application image using jpackage..." -ForegroundColor Cyan
if (Test-Path dist) { Remove-Item -Recurse -Force dist }
if (Test-Path package_input) { Remove-Item -Recurse -Force package_input }
New-Item -ItemType Directory -Path package_input | Out-Null
Copy-Item YouvakendraSM.jar package_input/
Copy-Item styles.css package_input/
Copy-Item -Recurse assets package_input/

jpackage --type app-image --name YouvakendraSM --input package_input --main-jar YouvakendraSM.jar --main-class Main --icon assets/logo.ico --dest dist --module-path javafx-sdk-21.0.2\lib --add-modules javafx.controls,javafx.fxml
if ($LASTEXITCODE -ne 0) {
    Write-Error "jpackage build failed."
    exit 1
}

# Remove package_input temp folder
Remove-Item -Recurse -Force package_input

# 3. Create ZIP archive of the app image
Write-Host "Creating ZIP archive of the application image..." -ForegroundColor Cyan
if (Test-Path YouvakendraSM.zip) { Remove-Item YouvakendraSM.zip -Force }
Compress-Archive -Path dist/YouvakendraSM/* -DestinationPath YouvakendraSM.zip -Force

# 4. Compile Installer.cs to dist\setup.exe using csc
Write-Host "Compiling setup.exe using csc..." -ForegroundColor Cyan
$cscPath = "C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe"
& $cscPath /target:winexe /win32icon:assets\logo.ico /resource:YouvakendraSM.zip /r:System.Windows.Forms.dll,System.Drawing.dll,System.IO.Compression.FileSystem.dll /out:dist\setup.exe Installer.cs

if ($LASTEXITCODE -ne 0) {
    Write-Error "C# compilation failed."
    exit 1
}

# 5. Save ZIP and clean up temporary files
Write-Host "Saving standalone ZIP to dist folder..." -ForegroundColor Cyan
Move-Item YouvakendraSM.zip dist\YouvakendraSM.zip -Force
if (Test-Path Installer.cs) { Remove-Item Installer.cs -Force }

Write-Host "==========================================================" -ForegroundColor Green
Write-Host " Build Successful!" -ForegroundColor Green
Write-Host "   Launcher: dist\YouvakendraSM\YouvakendraSM.exe" -ForegroundColor Green
Write-Host "   Portable ZIP: dist\YouvakendraSM.zip" -ForegroundColor Green
Write-Host "   Installer: dist\setup.exe" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
