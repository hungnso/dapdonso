param(
    # Keep the launcher aligned with the current source build.  The old
    # e72_auto_nvc.jar artifact is no longer produced by this repository.
    [string]$GameJar = "build\e72_v37_quick31_fashion_mask.jar"
)

$ErrorActionPreference = "Stop"
$projectDir = $PSScriptRoot
$microJar = Join-Path $projectDir "MICRO.jar"
$gamePath = if ([System.IO.Path]::IsPathRooted($GameJar)) {
    $GameJar
} else {
    Join-Path $projectDir $GameJar
}

$javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
if ($javaCommand) {
    $java = $javaCommand.Source
} else {
    $candidates = @(
        (Join-Path $projectDir "build\jdk-portable\PFiles64\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe"),
        "C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot\bin\java.exe",
        "D:\game\TOOL_QLTK\jre\bin\java.exe"
    )
    $java = $candidates | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
}

if (!(Test-Path -LiteralPath $java)) {
    throw "Khong tim thay java.exe."
}
if (!(Test-Path -LiteralPath $gamePath)) {
    throw "Khong tim thay JAR game: $gamePath. Hay chay build.ps1 truoc."
}

$logDir = Join-Path $projectDir "build"
New-Item -ItemType Directory -Path $logDir -Force | Out-Null
$logPath = Join-Path $logDir "emulator.log"
$errorLogPath = Join-Path $logDir "emulator-error.log"

try {
    if (Test-Path -LiteralPath $logPath) { Remove-Item -LiteralPath $logPath -Force -ErrorAction Stop }
    if (Test-Path -LiteralPath $errorLogPath) { Remove-Item -LiteralPath $errorLogPath -Force -ErrorAction Stop }
} catch {
    $stamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $logPath = Join-Path $logDir "emulator-$stamp.log"
    $errorLogPath = Join-Path $logDir "emulator-$stamp-error.log"
    Write-Host "Log cu dang bi khoa, dung file moi: $logPath"
}

Write-Host "Log game: $logPath"
Write-Host "Log loi:  $errorLogPath"

$arguments = @("-jar", "`"$microJar`"", "`"$gamePath`"")
$process = Start-Process -FilePath $java `
    -ArgumentList $arguments `
    -RedirectStandardOutput $logPath `
    -RedirectStandardError $errorLogPath `
    -PassThru

Write-Host "MicroEmulator PID: $($process.Id)"
$process.WaitForExit()

if ($process.ExitCode -ne 0) {
    Write-Host "MicroEmulator ket thuc voi ma loi $($process.ExitCode)."
    if (Test-Path -LiteralPath $errorLogPath) {
        Get-Content -LiteralPath $errorLogPath -Tail 100
    }
    exit $process.ExitCode
}
