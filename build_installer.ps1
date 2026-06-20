# 1. Compile Java files and build JAR
Write-Host "Compiling Java files..." -ForegroundColor Cyan
javac --module-path javafx-sdk-21.0.2\lib --add-modules javafx.controls,javafx.fxml --class-path "lib\*" -d bin *.java
if ($LASTEXITCODE -ne 0) {
    Write-Error "Java compilation failed."
    exit 1
}

if (-not (Test-Path bin/assets)) { New-Item -ItemType Directory -Path bin/assets | Out-Null }
Copy-Item styles.css bin/styles.css -Force
Copy-Item assets/* bin/assets/ -Force

Write-Host "Packaging JAR file..." -ForegroundColor Cyan
$manifestContent = @"
Manifest-Version: 1.0
Main-Class: Main
Class-Path: lib/google-api-services-sheets-v4-rev20220927-2.0.0.jar
 lib/google-api-client-2.2.0.jar
 lib/google-oauth-client-1.34.1.jar
 lib/google-http-client-1.43.3.jar
 lib/google-http-client-gson-1.43.3.jar
 lib/gson-2.10.1.jar
 lib/google-auth-library-credentials-1.19.0.jar
 lib/google-auth-library-oauth2-http-1.19.0.jar
 lib/guava-32.1.2-jre.jar
 lib/failureaccess-1.0.1.jar
 lib/opencensus-api-0.31.1.jar
 lib/opencensus-contrib-http-util-0.31.1.jar
 lib/httpclient-4.5.14.jar
 lib/httpcore-4.4.16.jar
 lib/commons-logging-1.2.jar
 lib/commons-codec-1.15.jar
 lib/grpc-context-1.53.0.jar

"@
$manifestContent | Out-File -FilePath manifest.txt -Encoding ascii -Force
jar --create --file YouvakendraSM.jar --manifest manifest.txt -C bin .
Remove-Item manifest.txt -Force

# 2. Build JPackage application image
Write-Host "Building application image using jpackage..." -ForegroundColor Cyan
if (Test-Path dist) { Remove-Item -Recurse -Force dist }
if (Test-Path package_input) { Remove-Item -Recurse -Force package_input }
New-Item -ItemType Directory -Path package_input | Out-Null
Copy-Item YouvakendraSM.jar package_input/
Copy-Item styles.css package_input/
Copy-Item -Recurse assets package_input/
New-Item -ItemType Directory -Path package_input/lib | Out-Null
Copy-Item lib/* package_input/lib/ -Force
Copy-Item YouvakendraSM/credencial.json package_input/ -Force -ErrorAction SilentlyContinue

jpackage --type app-image --name YouvakendraSM --input package_input --main-jar YouvakendraSM.jar --main-class Main --icon assets/logo.ico --dest dist --module-path javafx-sdk-21.0.2\lib --add-modules javafx.controls,javafx.fxml,java.logging,java.management,java.naming,java.sql,java.net.http,java.security.jgss,java.instrument
if ($LASTEXITCODE -ne 0) {
    Write-Error "jpackage build failed."
    exit 1
}

# Copy JavaFX native DLLs to the bundled runtime bin folder so that the JVM doesn't fail to load/launch on clean PCs.
Write-Host "Copying JavaFX native DLLs to runtime bin folder..." -ForegroundColor Cyan
Copy-Item javafx-sdk-21.0.2\bin\*.dll dist\YouvakendraSM\runtime\bin\ -Force
Copy-Item YouvakendraSM/credencial.json dist/YouvakendraSM/ -Force -ErrorAction SilentlyContinue

# Remove package_input temp folder
Remove-Item -Recurse -Force package_input

# 3. Create ZIP archive of the app image using jar tool (to enforce forward slash separators in zip entries for backward compatibility with older updaters)
Write-Host "Creating ZIP archive of the application image..." -ForegroundColor Cyan
if (Test-Path YouvakendraSM.zip) { Remove-Item YouvakendraSM.zip -Force }
jar -c -M -f YouvakendraSM.zip -C dist/YouvakendraSM .

# 4. Compile Installer.cs to dist\setup.exe using csc
Write-Host "Compiling setup.exe using csc..." -ForegroundColor Cyan
$cscPath = "C:\Windows\Microsoft.NET\Framework64\v4.0.30319\csc.exe"
& $cscPath /target:winexe /win32icon:assets\logo.ico /resource:YouvakendraSM.zip /r:System.Windows.Forms.dll,System.Drawing.dll,System.IO.Compression.dll,System.IO.Compression.FileSystem.dll /out:dist\setup.exe Installer.cs

if ($LASTEXITCODE -ne 0) {
    Write-Error "C# compilation failed."
    exit 1
}

# 5. Save ZIP and clean up temporary files
Write-Host "Saving standalone ZIP to dist folder..." -ForegroundColor Cyan
Move-Item YouvakendraSM.zip dist\YouvakendraSM.zip -Force

Write-Host "==========================================================" -ForegroundColor Green
Write-Host " Build Successful!" -ForegroundColor Green
Write-Host "   Launcher: dist\YouvakendraSM\YouvakendraSM.exe" -ForegroundColor Green
Write-Host "   Portable ZIP: dist\YouvakendraSM.zip" -ForegroundColor Green
Write-Host "   Installer: dist\setup.exe" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
