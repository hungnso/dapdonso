param(
    [string]$ClassesDir = 'build/v37-full-source-classes',
    [string[]]$TestNames = @()
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$classesPath = if ([IO.Path]::IsPathRooted($ClassesDir)) { $ClassesDir } else { Join-Path $projectRoot $ClassesDir }
$testClasses = Join-Path $projectRoot 'build/lite-tests'
New-Item -ItemType Directory -Path $testClasses -Force | Out-Null
$microPath = Join-Path $projectRoot 'MICRO.jar'
$javac = (Get-Command javac.exe -ErrorAction Stop).Source
$java = Join-Path (Split-Path -Parent $javac) 'java.exe'
$allTests = @(Get-ChildItem (Join-Path $projectRoot 'tests') -Filter '*Test.java')
$allTests += @(Get-ChildItem $PSScriptRoot -Filter '*Test.java')
if ($TestNames.Count -gt 0) { $allTests = @($allTests | Where-Object { $TestNames -contains $_.BaseName }) }
if ($allTests.Count -eq 0) { throw 'No tests selected.' }
$compileClasspath = $classesPath + [IO.Path]::PathSeparator + $microPath
$sources = @($allTests | ForEach-Object FullName)
& $javac -Xlint:-options --release 8 -encoding UTF-8 -cp $compileClasspath -d $testClasses $sources
if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed.' }
$runtimeClasspath = $testClasses + [IO.Path]::PathSeparator + $compileClasspath
foreach ($test in $allTests) {
    & $java '-Djava.awt.headless=true' -cp $runtimeClasspath $test.BaseName
    if ($LASTEXITCODE -ne 0) { throw "Test failed: $($test.BaseName)" }
    Write-Host "PASS $($test.BaseName)"
}
Write-Host "Passed $($allTests.Count) test programs."
