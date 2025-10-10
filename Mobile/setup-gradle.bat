@echo off
echo Downloading Gradle Wrapper...

powershell -Command "& {
    try {
        Write-Host 'Downloading gradle-wrapper.jar...'
        $url = 'https://github.com/gradle/gradle/raw/v8.4.0/gradle/wrapper/gradle-wrapper.jar'
        $output = 'gradle\wrapper\gradle-wrapper.jar'
        Invoke-WebRequest -Uri $url -OutFile $output -UseBasicParsing
        Write-Host 'Gradle wrapper downloaded successfully!' -ForegroundColor Green
        Write-Host 'Now you can run: gradlew build' -ForegroundColor Yellow
    } catch {
        Write-Host 'Error downloading gradle wrapper:' $_.Exception.Message -ForegroundColor Red
        Write-Host 'Please manually download from: https://github.com/gradle/gradle/raw/v8.4.0/gradle/wrapper/gradle-wrapper.jar' -ForegroundColor Yellow
    }
}"

pause