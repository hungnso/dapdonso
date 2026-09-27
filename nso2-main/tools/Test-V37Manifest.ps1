param(
    [string]$JarPath = "build\e72_v37_full_source_merged.jar"
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.IO.Compression.FileSystem

$resolvedJar = Resolve-Path -LiteralPath $JarPath
$archive = [System.IO.Compression.ZipFile]::OpenRead($resolvedJar)
try {
    $entry = $archive.GetEntry("META-INF/MANIFEST.MF")
    if ($null -eq $entry) { throw "JAR khong co META-INF/MANIFEST.MF." }

    $reader = [System.IO.StreamReader]::new($entry.Open())
    try { $manifest = $reader.ReadToEnd() } finally { $reader.Dispose() }
} finally {
    $archive.Dispose()
}

foreach ($requiredLine in @(
    "MIDlet-1: AUTO50_X1,/nsotien_1.png,GameMidlet",
    "MIDlet-Name: AUTO50_X1",
    "NST-GameID: AUTO50_X1_490"
)) {
    if ($manifest -notmatch ("(?m)^" + [regex]::Escape($requiredLine) + "\r?$")) {
        throw "Manifest khong co truong QLTK bat buoc: $requiredLine"
    }
}

Write-Host "QLTK manifest hop le: $resolvedJar"
