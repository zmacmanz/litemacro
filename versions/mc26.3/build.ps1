$ErrorActionPreference = 'Stop'

$gradleVersion = '9.4.1'
$toolsDir = Join-Path $PSScriptRoot '.gradle-tools'
$gradleDir = Join-Path $toolsDir "gradle-$gradleVersion"
$gradleBat = Join-Path $gradleDir 'bin\gradle.bat'

if (-not (Test-Path -LiteralPath $gradleBat)) {
    New-Item -ItemType Directory -Force -Path $toolsDir | Out-Null
    $zipPath = Join-Path $toolsDir "gradle-$gradleVersion-bin.zip"

    if (-not (Test-Path -LiteralPath $zipPath)) {
        Invoke-WebRequest `
            -UseBasicParsing `
            -Uri "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip" `
            -OutFile $zipPath
    }

    Expand-Archive -LiteralPath $zipPath -DestinationPath $toolsDir -Force
}

& $gradleBat @args
exit $LASTEXITCODE
