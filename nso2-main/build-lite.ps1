param(
    [string]$OutputJar = 'build/e72_lite.jar',
    [string]$BaseJar = 'e72_x1.jar',
    [string]$ManifestFile = 'META-INF/current_manifest.mf'
)
$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath($PSScriptRoot)
function ProjectPath([string]$path) {
    if ([IO.Path]::IsPathRooted($path)) { return [IO.Path]::GetFullPath($path) }
    return [IO.Path]::GetFullPath((Join-Path $projectRoot $path))
}
function AssertWorkspacePath([string]$path) {
    if (!$path.StartsWith($projectRoot + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Build target outside workspace: $path"
    }
}
$basePath = ProjectPath $BaseJar
$microPath = ProjectPath 'MICRO.jar'
$manifestPath = ProjectPath $ManifestFile
$outputPath = ProjectPath $OutputJar
$classesPath = ProjectPath 'build/lite-classes'
$supportPath = ProjectPath 'build/lite-support'
foreach ($path in @($classesPath,$supportPath,$outputPath)) { AssertWorkspacePath $path }
foreach ($path in @($basePath,$microPath,$manifestPath)) {
    if (!(Test-Path -LiteralPath $path)) { throw "Missing build input: $path" }
    if ($outputPath.Equals($path,[StringComparison]::OrdinalIgnoreCase)) { throw 'Output must not overwrite build inputs.' }
}
$javac = (Get-Command javac.exe -ErrorAction Stop).Source
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
foreach ($path in @($classesPath,$supportPath)) {
    AssertWorkspacePath $path
    if (Test-Path -LiteralPath $path) { Remove-Item -LiteralPath $path -Recurse -Force }
    New-Item -ItemType Directory -Path $path -Force | Out-Null
}

# Compile against the Java ME API and only the one source-external class.
# Old root classes must not hide missing references to removed automation.
$baseArchive = [IO.Compression.ZipFile]::OpenRead($basePath)
try {
    $sms = $baseArchive.GetEntry('SmsData.class')
    if (!$sms) { throw 'Base JAR has no SmsData.class (JSR-120 support).' }
    [IO.Compression.ZipFileExtensions]::ExtractToFile($sms,(Join-Path $supportPath 'SmsData.class'),$true)
} finally { $baseArchive.Dispose() }
$sourceFiles = @(Get-ChildItem (Join-Path $projectRoot 'src') -Filter '*.java' |
    Where-Object Name -ne 'SmsData.java' | ForEach-Object FullName)
$compileClasspath = $microPath + [IO.Path]::PathSeparator + $supportPath
& $javac -Xlint:-options --release 8 -encoding UTF-8 -cp $compileClasspath -d $classesPath $sourceFiles
if ($LASTEXITCODE -ne 0) { throw 'Lite source compilation failed.' }

New-Item -ItemType Directory -Path (Split-Path -Parent $outputPath) -Force | Out-Null
$temporaryPath = $outputPath + '.tmp'
AssertWorkspacePath $temporaryPath
if (Test-Path -LiteralPath $temporaryPath) { Remove-Item -LiteralPath $temporaryPath -Force }
$baseArchive = [IO.Compression.ZipFile]::OpenRead($basePath)
$outputArchive = [IO.Compression.ZipFile]::Open($temporaryPath,[IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($entry in $baseArchive.Entries) {
        $name = $entry.FullName
        if ($name.EndsWith('/')) { continue }
        if ($name -eq 'META-INF/MANIFEST.MF' -or $name -match '^META-INF/[^/]+\.(SF|RSA|DSA)$') { continue }
        if ($name.EndsWith('.class') -and !$name.Contains('/') -and $name -ne 'SmsData.class') { continue }
        $copy = $outputArchive.CreateEntry($name,[IO.Compression.CompressionLevel]::Optimal)
        $inputStream = $entry.Open()
        $outputStream = $copy.Open()
        try { $inputStream.CopyTo($outputStream) } finally { $inputStream.Dispose(); $outputStream.Dispose() }
    }
    $manifestEntry = $outputArchive.CreateEntry('META-INF/MANIFEST.MF',[IO.Compression.CompressionLevel]::Optimal)
    $manifestBytes = [IO.File]::ReadAllBytes($manifestPath)
    $manifestStream = $manifestEntry.Open()
    try { $manifestStream.Write($manifestBytes,0,$manifestBytes.Length) } finally { $manifestStream.Dispose() }
    foreach ($file in Get-ChildItem $classesPath -Filter '*.class' -Recurse) {
        $name = $file.FullName.Substring($classesPath.Length+1).Replace('\','/')
        [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($outputArchive,$file.FullName,$name,[IO.Compression.CompressionLevel]::Optimal) | Out-Null
    }
} finally { $baseArchive.Dispose(); $outputArchive.Dispose() }
& (Join-Path $projectRoot 'tools/Test-LiteJar.ps1') -JarPath $temporaryPath -BaseJar $basePath
# QLTK exits with Java code 2 if the account bridge contract is missing.
& (Join-Path $projectRoot 'tools/Test-LiteRuntime.ps1') -ClassesDir $temporaryPath -TestNames QltkContractTest
Move-Item -LiteralPath $temporaryPath -Destination $outputPath -Force
$jarFile = Get-Item -LiteralPath $outputPath
Write-Host "Lite build successful: $outputPath ($($jarFile.Length) bytes)"
