param([string]$JarPath = 'build/e72_lite.jar', [string]$BaseJar = 'e72_x1.jar')
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Add-Type -AssemblyName System.IO.Compression.FileSystem
$jarFile = if ([IO.Path]::IsPathRooted($JarPath)) { $JarPath } else { Join-Path $projectRoot $JarPath }
$archive = [IO.Compression.ZipFile]::OpenRead($jarFile)
try {
    $removed = @('AutoTanSat','AutoDanhVong','AutoDanhVongPanel','DanhVongQuest','AutoAttack',
        'AutoAttackPk','AutoPkAm','AutoHSXa','Class_af','AutoHangDong','MenuHangDong','PK_AM_PANEL','ToolCuoc','AutoNhayPanel')
    foreach ($entry in $archive.Entries) {
        foreach ($name in $removed) {
            if ($entry.FullName -match ('^' + [regex]::Escape($name) + '(\$[^/]*)?\.class$')) {
                throw "Unused auto still packaged: $($entry.FullName)"
            }
        }
    }
    foreach ($required in @('GameMidlet.class','SmsData.class','NSOT_MOB.class','AutoVungDatMaQuai.class',
            'AutoNhiemVuChinh.class','AutoDailyCoordinator.class','TaskAuto.class','TaskTaThuAuto.class',
            'DailyHangPolicy.class','AutoWorkerLifecycle.class','AutoResumeStack.class','Sender.class','nsotien_1.png')) {
        if (!$archive.GetEntry($required)) { throw "Missing runtime entry: $required" }
    }
    $duplicateNames = @($archive.Entries | Group-Object FullName | Where-Object Count -gt 1)
    if ($duplicateNames.Count -gt 0) { throw 'Duplicate JAR entries.' }
    $reader = New-Object IO.StreamReader($archive.GetEntry('META-INF/MANIFEST.MF').Open())
    try { $manifest = $reader.ReadToEnd() } finally { $reader.Dispose() }
    foreach ($line in @('MIDlet-1: AUTO50_X1,/nsotien_1.png,GameMidlet','MIDlet-Name: AUTO50_X1','NST-GameID: AUTO50_X1_490')) {
        if ($manifest -notmatch ('(?m)^'+[regex]::Escape($line)+'\r?$')) { throw "Invalid manifest: $line" }
    }
    $baseFile = if ([IO.Path]::IsPathRooted($BaseJar)) { $BaseJar } else { Join-Path $projectRoot $BaseJar }
    $baseArchive = [IO.Compression.ZipFile]::OpenRead($baseFile)
    $hash = [Security.Cryptography.SHA256]::Create()
    try {
        foreach ($entry in $baseArchive.Entries) {
            $name = $entry.FullName
            if ($name.EndsWith('/') -or $name.EndsWith('.class') -or $name -eq 'META-INF/MANIFEST.MF' -or
                    $name -match '^META-INF/[^/]+\.(SF|RSA|DSA)$') { continue }
            $retained = $archive.GetEntry($name)
            if (!$retained -or $retained.Length -ne $entry.Length) { throw "Missing or changed game resource: $name" }
            $originalStream = $entry.Open(); $retainedStream = $retained.Open()
            try {
                $originalHash = [Convert]::ToBase64String($hash.ComputeHash($originalStream))
                $retainedHash = [Convert]::ToBase64String($hash.ComputeHash($retainedStream))
                if ($originalHash -ne $retainedHash) { throw "Changed game resource: $name" }
            } finally { $originalStream.Dispose(); $retainedStream.Dispose() }
        }
    } finally { $hash.Dispose(); $baseArchive.Dispose() }
    Write-Host "Lite JAR PASS: $jarFile ($($archive.Entries.Count) entries)"
} finally { $archive.Dispose() }
