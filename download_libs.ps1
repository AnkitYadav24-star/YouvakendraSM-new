$libDir = "lib"
if (-not (Test-Path $libDir)) {
    New-Item -ItemType Directory -Path $libDir | Out-Null
}

$libraries = @(
    @{
        Name = "google-api-services-sheets-v4-rev20220927-2.0.0.jar"
        Url = "https://repo1.maven.org/maven2/com/google/apis/google-api-services-sheets/v4-rev20220927-2.0.0/google-api-services-sheets-v4-rev20220927-2.0.0.jar"
    },
    @{
        Name = "google-api-client-2.2.0.jar"
        Url = "https://repo1.maven.org/maven2/com/google/api-client/google-api-client/2.2.0/google-api-client-2.2.0.jar"
    },
    @{
        Name = "google-oauth-client-1.34.1.jar"
        Url = "https://repo1.maven.org/maven2/com/google/oauth-client/google-oauth-client/1.34.1/google-oauth-client-1.34.1.jar"
    },
    @{
        Name = "google-http-client-1.43.3.jar"
        Url = "https://repo1.maven.org/maven2/com/google/http-client/google-http-client/1.43.3/google-http-client-1.43.3.jar"
    },
    @{
        Name = "google-http-client-gson-1.43.3.jar"
        Url = "https://repo1.maven.org/maven2/com/google/http-client/google-http-client-gson/1.43.3/google-http-client-gson-1.43.3.jar"
    },
    @{
        Name = "gson-2.10.1.jar"
        Url = "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar"
    },
    @{
        Name = "google-auth-library-credentials-1.19.0.jar"
        Url = "https://repo1.maven.org/maven2/com/google/auth/google-auth-library-credentials/1.19.0/google-auth-library-credentials-1.19.0.jar"
    },
    @{
        Name = "google-auth-library-oauth2-http-1.19.0.jar"
        Url = "https://repo1.maven.org/maven2/com/google/auth/google-auth-library-oauth2-http/1.19.0/google-auth-library-oauth2-http-1.19.0.jar"
    },
    @{
        Name = "guava-32.1.2-jre.jar"
        Url = "https://repo1.maven.org/maven2/com/google/guava/guava/32.1.2-jre/guava-32.1.2-jre.jar"
    },
    @{
        Name = "failureaccess-1.0.1.jar"
        Url = "https://repo1.maven.org/maven2/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar"
    },
    @{
        Name = "opencensus-api-0.31.1.jar"
        Url = "https://repo1.maven.org/maven2/io/opencensus/opencensus-api/0.31.1/opencensus-api-0.31.1.jar"
    },
    @{
        Name = "opencensus-contrib-http-util-0.31.1.jar"
        Url = "https://repo1.maven.org/maven2/io/opencensus/opencensus-contrib-http-util/0.31.1/opencensus-contrib-http-util-0.31.1.jar"
    },
    @{
        Name = "httpclient-4.5.14.jar"
        Url = "https://repo1.maven.org/maven2/org/apache/httpcomponents/httpclient/4.5.14/httpclient-4.5.14.jar"
    },
    @{
        Name = "httpcore-4.4.16.jar"
        Url = "https://repo1.maven.org/maven2/org/apache/httpcomponents/httpcore/4.4.16/httpcore-4.4.16.jar"
    },
    @{
        Name = "commons-logging-1.2.jar"
        Url = "https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar"
    },
    @{
        Name = "commons-codec-1.15.jar"
        Url = "https://repo1.maven.org/maven2/commons-codec/commons-codec/1.15/commons-codec-1.15.jar"
    },
    @{
        Name = "grpc-context-1.53.0.jar"
        Url = "https://repo1.maven.org/maven2/io/grpc/grpc-context/1.53.0/grpc-context-1.53.0.jar"
    }
)

Write-Host "Checking/Downloading dependencies to $libDir..." -ForegroundColor Cyan

[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

foreach ($lib in $libraries) {
    $dest = Join-Path $libDir $lib.Name
    if (-not (Test-Path $dest)) {
        Write-Host "Downloading $($lib.Name)..." -ForegroundColor Yellow
        try {
            Invoke-WebRequest -Uri $lib.Url -OutFile $dest -TimeoutSec 60
            Write-Host "Successfully downloaded $($lib.Name)" -ForegroundColor Green
        } catch {
            Write-Error "Failed to download $($lib.Name) from $($lib.Url): $_"
            exit 1
        }
    } else {
        Write-Host "$($lib.Name) already exists." -ForegroundColor Gray
    }
}

Write-Host "All libraries checked/downloaded successfully." -ForegroundColor Green
