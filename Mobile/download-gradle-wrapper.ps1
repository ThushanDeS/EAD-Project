$url = "https://github.com/gradle/gradle/raw/v8.4.0/gradle/wrapper/gradle-wrapper.jar"
$output = "gradle\wrapper\gradle-wrapper.jar"
Invoke-WebRequest -Uri $url -OutFile $output
Write-Host "Gradle wrapper downloaded successfully!"