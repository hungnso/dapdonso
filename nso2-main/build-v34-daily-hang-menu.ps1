param(
    [string]$OutputJar = "build\e72_v34_daily_hang_menu_select.jar",
    [string]$BaselineJar = "dist\e72_v33_daily_hang_first.jar"
)

$ErrorActionPreference = "Stop"
$projectDir = $PSScriptRoot
$baseline = if ([System.IO.Path]::IsPathRooted($BaselineJar)) { $BaselineJar } else { Join-Path $projectDir $BaselineJar }
$microJar = Join-Path $projectDir "MICRO.jar"
$outputPath = if ([System.IO.Path]::IsPathRooted($OutputJar)) { $OutputJar } else { Join-Path $projectDir $OutputJar }
$classesDir = Join-Path $projectDir "build\v34-daily-hang-menu-classes"

$javacCommand = Get-Command javac.exe -ErrorAction SilentlyContinue
if ($javacCommand) {
    $javac = $javacCommand.Source
} else {
    $jdkCandidates = @(
        "C:\Program Files\Eclipse Adoptium\jdk-*\bin\javac.exe",
        "C:\Program Files\Java\jdk-*\bin\javac.exe"
    )
    $javac = $null
    foreach ($candidate in $jdkCandidates) {
        $resolved = Get-ChildItem $candidate -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($resolved) { $javac = $resolved.FullName; break }
    }
}
if (!$javac -or !(Test-Path -LiteralPath $javac)) { throw "Khong tim thay javac.exe" }
$jarTool = Join-Path (Split-Path -Parent $javac) "jar.exe"
if (!(Test-Path -LiteralPath $jarTool)) { throw "Khong tim thay jar.exe" }
if (!(Test-Path -LiteralPath $baseline)) { throw "Thieu baseline v33: $baseline" }
if (!(Test-Path -LiteralPath $microJar)) { throw "Thieu MICRO.jar" }

if (Test-Path -LiteralPath $classesDir) { Remove-Item $classesDir -Recurse -Force }
New-Item -ItemType Directory -Path $classesDir -Force | Out-Null
New-Item -ItemType Directory -Path (Split-Path -Parent $outputPath) -Force | Out-Null

$source = Join-Path $projectDir "src\AutoDailyCoordinator.java"
$classPath = $baseline + ";" + $microJar
& $javac --release 8 -encoding UTF-8 -cp $classPath -d $classesDir $source
if ($LASTEXITCODE -ne 0) { throw "Compile v34 Daily Hang menu that bai" }

Copy-Item -LiteralPath $baseline -Destination $outputPath -Force
Push-Location $classesDir
try {
    & $jarTool uf $outputPath AutoDailyCoordinator.class
    if ($LASTEXITCODE -ne 0) { throw "Dong goi JAR v34 that bai" }
} finally {
    Pop-Location
}

Write-Host "Build v34 thanh cong: $outputPath"
Write-Host "Fix: menu Hang trung gian se tu chon Hang dong -> Cap theo level, khong dung im o menu Cap 65."
