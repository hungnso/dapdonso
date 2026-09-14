param(
    [string]$OutputJar = "build\e72_v33_daily_hang_first.jar",
    [string]$BaselineJar = "dist\e72_v32_vdmq_ground_only.jar"
)

$ErrorActionPreference = "Stop"
$projectDir = $PSScriptRoot
$baseline = if ([System.IO.Path]::IsPathRooted($BaselineJar)) { $BaselineJar } else { Join-Path $projectDir $BaselineJar }
$microJar = Join-Path $projectDir "MICRO.jar"
$outputPath = if ([System.IO.Path]::IsPathRooted($OutputJar)) { $OutputJar } else { Join-Path $projectDir $OutputJar }
$classesDir = Join-Path $projectDir "build\v33-daily-hang-classes"

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
if (!(Test-Path -LiteralPath $baseline)) { throw "Thieu baseline v32: $baseline" }
if (!(Test-Path -LiteralPath $microJar)) { throw "Thieu MICRO.jar" }

if (Test-Path -LiteralPath $classesDir) { Remove-Item $classesDir -Recurse -Force }
New-Item -ItemType Directory -Path $classesDir -Force | Out-Null
New-Item -ItemType Directory -Path (Split-Path -Parent $outputPath) -Force | Out-Null

$sources = @(
    (Join-Path $projectDir "src\AutoDailyCoordinator.java"),
    (Join-Path $projectDir "src\DailyReconnectRecovery.java"),
    (Join-Path $projectDir "src\DailyStallRecovery.java")
)
$classPath = $baseline + ";" + $microJar
& $javac --release 8 -encoding UTF-8 -cp $classPath -d $classesDir $sources
if ($LASTEXITCODE -ne 0) { throw "Compile v33 Daily Hang that bai" }

Copy-Item -LiteralPath $baseline -Destination $outputPath -Force
Push-Location $classesDir
try {
    & $jarTool uf $outputPath AutoDailyCoordinator.class DailyReconnectRecovery.class DailyStallRecovery.class
    if ($LASTEXITCODE -ne 0) { throw "Dong goi JAR v33 that bai" }
} finally {
    Pop-Location
}

Write-Host "Build v33 thanh cong: $outputPath"
Write-Host "Flow: Hang theo cap -> reconnect thoat Hang -> NV Hang Ngay -> Ta Thu -> resume auto truoc do."
