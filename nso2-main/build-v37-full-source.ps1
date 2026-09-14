param(
    [string]$OutputJar = "build\e72_v37_full_source_merged.jar",
    [string]$BaseJar = "e72_x1.jar",
    [string]$ManifestFile = "META-INF\current_manifest.mf"
)

$ErrorActionPreference = "Stop"
$projectDir = $PSScriptRoot
$classesDir = Join-Path $projectDir "build\v37-full-source-classes"
$baseJarPath = if ([System.IO.Path]::IsPathRooted($BaseJar)) { $BaseJar } else { Join-Path $projectDir $BaseJar }
$microJar = Join-Path $projectDir "MICRO.jar"
$outputPath = if ([System.IO.Path]::IsPathRooted($OutputJar)) { $OutputJar } else { Join-Path $projectDir $OutputJar }
$manifestPath = if ([System.IO.Path]::IsPathRooted($ManifestFile)) { $ManifestFile } else { Join-Path $projectDir $ManifestFile }

$javacCommand = Get-Command javac.exe -ErrorAction SilentlyContinue
if ($javacCommand) {
    $javac = $javacCommand.Source
} else {
    $javacCommand = Get-Command javac -ErrorAction SilentlyContinue
    if ($javacCommand) {
        $javac = $javacCommand.Source
    } else {
        $javac = "C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot\bin\javac.exe"
    }
}
if (!(Test-Path -LiteralPath $javac)) { throw "Khong tim thay javac. Hay cai JDK hoac them javac vao PATH." }

$jarName = if ($IsWindows -or $javac.EndsWith(".exe")) { "jar.exe" } else { "jar" }
$jarTool = Join-Path (Split-Path -Parent $javac) $jarName
if (!(Test-Path -LiteralPath $jarTool)) {
    $jarCmd = Get-Command jar -ErrorAction SilentlyContinue
    if ($jarCmd) { $jarTool = $jarCmd.Source } else { throw "Khong tim thay jar tool." }
}

if (!(Test-Path -LiteralPath $baseJarPath) -or !(Test-Path -LiteralPath $microJar)) {
    throw "Thieu e72_x1.jar hoac MICRO.jar."
}
if (!(Test-Path -LiteralPath $manifestPath)) { throw "Thieu manifest: $manifestPath" }

if (Test-Path -LiteralPath $classesDir) { Remove-Item -LiteralPath $classesDir -Recurse -Force }
New-Item -ItemType Directory -Path $classesDir -Force | Out-Null
New-Item -ItemType Directory -Path (Split-Path -Parent $outputPath) -Force | Out-Null

$sources = Get-ChildItem (Join-Path $projectDir "src") -Filter "*.java" |
    Where-Object { $_.Name -ne "SmsData.java" } |
    ForEach-Object { $_.FullName }

$classPath = $microJar + [System.IO.Path]::PathSeparator + $baseJarPath
& $javac --release 8 -encoding UTF-8 -cp $classPath -d $classesDir $sources
if ($LASTEXITCODE -ne 0) { throw "Compile source v37 that bai." }

Copy-Item -LiteralPath $baseJarPath -Destination $outputPath -Force
& $jarTool ufm $outputPath $manifestPath
if ($LASTEXITCODE -ne 0) { throw "Cap nhat manifest JAR that bai." }

Push-Location $classesDir
try {
    & $jarTool uf $outputPath .
    if ($LASTEXITCODE -ne 0) { throw "Dong goi JAR that bai." }
} finally {
    Pop-Location
}

Write-Host "Build source-clean v37 thanh cong: $outputPath"
Write-Host "Khong overlay latest_classes va khong ASM patch runtime."
