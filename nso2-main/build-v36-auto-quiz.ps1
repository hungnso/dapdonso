param(
    [string]$OutputJar = "build\e72_v36_auto_quiz_answer.jar",
    [string]$BaselineJar = "build\e72_v35_nv3_keep_local_mob.jar"
)

$ErrorActionPreference = "Stop"
$projectDir = $PSScriptRoot
$baseline = if ([System.IO.Path]::IsPathRooted($BaselineJar)) { $BaselineJar } else { Join-Path $projectDir $BaselineJar }
$microJar = Join-Path $projectDir "MICRO.jar"
$outputPath = if ([System.IO.Path]::IsPathRooted($OutputJar)) { $OutputJar } else { Join-Path $projectDir $OutputJar }
$classesDir = Join-Path $projectDir "build\v36-quiz-classes"

$javacCommand = Get-Command javac.exe -ErrorAction SilentlyContinue
if ($javacCommand) { $javac = $javacCommand.Source } else { throw "Khong tim thay javac.exe" }
$jarTool = Join-Path (Split-Path -Parent $javac) "jar.exe"
if (!(Test-Path -LiteralPath $jarTool)) { throw "Khong tim thay jar.exe" }
if (!(Test-Path -LiteralPath $baseline)) { throw "Thieu baseline v35: $baseline" }
if (!(Test-Path -LiteralPath $microJar)) { throw "Thieu MICRO.jar" }

if (Test-Path -LiteralPath $classesDir) { Remove-Item $classesDir -Recurse -Force }
New-Item -ItemType Directory -Path $classesDir -Force | Out-Null
New-Item -ItemType Directory -Path (Split-Path -Parent $outputPath) -Force | Out-Null

$source = Join-Path $projectDir "src\Menu.java"
$classPath = $baseline + ";" + $microJar
& $javac --release 8 -encoding UTF-8 -cp $classPath -d $classesDir $source
if ($LASTEXITCODE -ne 0) { throw "Compile v36 Menu that bai" }

Copy-Item -LiteralPath $baseline -Destination $outputPath -Force
Push-Location $classesDir
try {
    & $jarTool uf $outputPath Menu.class
    if ($LASTEXITCODE -ne 0) { throw "Dong goi JAR v36 that bai" }
} finally {
    Pop-Location
}

Write-Host "Build v36 thanh cong: $outputPath"
Write-Host "Fix: tutorial quiz Tabemono tu chon dap an Kiem + Kunai + Dao thay vi dung o menu dap an."
