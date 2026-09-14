param(
    [string]$OutputJar = "build\e72_v35_nv3_keep_local_mob.jar",
    [string]$BaselineJar = "build\e72_v34_daily_hang_menu_select.jar"
)

$ErrorActionPreference = "Stop"
$projectDir = $PSScriptRoot
$baseline = if ([System.IO.Path]::IsPathRooted($BaselineJar)) { $BaselineJar } else { Join-Path $projectDir $BaselineJar }
$microJar = Join-Path $projectDir "MICRO.jar"
$outputPath = if ([System.IO.Path]::IsPathRooted($OutputJar)) { $OutputJar } else { Join-Path $projectDir $OutputJar }
$classesDir = Join-Path $projectDir "build\v35-nv3-classes"

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
if (!(Test-Path -LiteralPath $baseline)) { throw "Thieu baseline v34: $baseline" }
if (!(Test-Path -LiteralPath $microJar)) { throw "Thieu MICRO.jar" }

if (Test-Path -LiteralPath $classesDir) { Remove-Item $classesDir -Recurse -Force }
New-Item -ItemType Directory -Path $classesDir -Force | Out-Null
New-Item -ItemType Directory -Path (Split-Path -Parent $outputPath) -Force | Out-Null

$source = Join-Path $projectDir "src\AutoNhiemVuChinh.java"
$classPath = $baseline + ";" + $microJar
& $javac --release 8 -encoding UTF-8 -cp $classPath -d $classesDir $source
if ($LASTEXITCODE -ne 0) { throw "Compile v35 AutoNhiemVuChinh that bai" }

Copy-Item -LiteralPath $baseline -Destination $outputPath -Force
Push-Location $classesDir
try {
    & $jarTool uf $outputPath AutoNhiemVuChinh.class
    if ($LASTEXITCODE -ne 0) { throw "Dong goi JAR v35 that bai" }
} finally {
    Pop-Location
}

Write-Host "Build v35 thanh cong: $outputPath"
Write-Host "Fix: NV3 buoc 3 uu tien danh Oc sen tren map hien tai, khong next-map sai khi map dang co spawn nhiem vu."
