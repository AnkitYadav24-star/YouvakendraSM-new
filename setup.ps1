$Url = "https://download2.gluonhq.com/openjfx/21.0.2/openjfx-21.0.2_windows-x64_bin-sdk.zip"
$ZipFile = "javafx-sdk-21.0.2.zip"
$ExtractPath = "."

if (-not (Test-Path "javafx-sdk-21.0.2")) {
    Write-Host "JavaFX SDK not found. Downloading..."
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Invoke-WebRequest -Uri $Url -OutFile $ZipFile
    Write-Host "Download complete. Extracting..."
    Expand-Archive -Path $ZipFile -DestinationPath $ExtractPath -Force
    Remove-Item $ZipFile
    Write-Host "JavaFX SDK setup complete."
} else {
    Write-Host "JavaFX SDK already exists. Skipping download."
}
