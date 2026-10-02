param(
    [string]$JarPath = 'build/e72_lite.jar',
    [int]$Seconds = 15,
    [string]$JavaPath = '',
    [string]$EmulatorPath = ''
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$jarFile = if ([IO.Path]::IsPathRooted($JarPath)) { $JarPath } else { Join-Path $projectRoot $JarPath }
$scratch = Join-Path $projectRoot 'build/lite-work'
$emulatorHome = Join-Path $scratch 'emulator-home'
New-Item -ItemType Directory -Path $emulatorHome -Force | Out-Null
$java = if ($JavaPath) { $JavaPath } else { (Get-Command java.exe -ErrorAction Stop).Source }
$micro = if ($EmulatorPath) { $EmulatorPath } else { Join-Path $projectRoot 'MICRO.jar' }
$probeClasses = Join-Path $scratch 'boot-probe-classes'
$marker = Join-Path $scratch 'boot-ready.txt'
New-Item -ItemType Directory -Path $probeClasses -Force | Out-Null
if (Test-Path -LiteralPath $marker) { Remove-Item -LiteralPath $marker -Force }
$javac = (Get-Command javac.exe -ErrorAction Stop).Source
& $javac -Xlint:-options --release 8 -encoding UTF-8 -cp $micro -d $probeClasses (Join-Path $PSScriptRoot 'LiteBootProbe.java')
if ($LASTEXITCODE -ne 0) { throw 'Boot probe compilation failed.' }
$stdout = Join-Path $scratch 'boot.log'
$stderr = Join-Path $scratch 'boot-error.log'
$arguments = @('-Djava.awt.headless=true', ('"-Duser.home={0}"' -f $emulatorHome),
    '-cp', ('"{0};{1}"' -f $probeClasses,$micro), 'LiteBootProbe', ('"{0}"' -f $jarFile), ('"{0}"' -f $marker))
$process = Start-Process -FilePath $java -ArgumentList $arguments -WindowStyle Hidden -PassThru -RedirectStandardOutput $stdout -RedirectStandardError $stderr
$bootExitCode = 0
try {
    $finished = $process.WaitForExit([Math]::Min($Seconds,45) * 1000)
    if (!$finished) {
        $process.Refresh()
        Write-Host "Headless boot remained alive: PID $($process.Id), CPU $($process.CPU)s, working set $($process.WorkingSet64) bytes"
    } else {
        $bootExitCode = $process.ExitCode
        Write-Host "Headless emulator exit: $($process.ExitCode)"
    }
} finally {
    if (!$process.HasExited) { Stop-Process -Id $process.Id -Force; $process.WaitForExit() }
    $process.Dispose()
}
Get-Content -LiteralPath $stdout -Tail 12
Get-Content -LiteralPath $stderr -Tail 12
$bootOutput = [IO.File]::ReadAllText($stdout)
$bootErrors = [IO.File]::ReadAllText($stderr)
if ($bootExitCode -ne 0) { throw "Headless boot failed with exit $bootExitCode" }
if (!(Test-Path -LiteralPath $marker) -or [IO.File]::ReadAllText($marker) -ne 'MIDLET_LOGIN_READY') {
    throw 'MIDlet did not initialize LoginScr and reach its start screen.'
}
if (($bootOutput + $bootErrors) -match 'Exception in thread|NoClassDefFoundError|ClassNotFoundException|ExceptionInInitializerError|NoSuchMethodError|UnsupportedClassVersionError') {
    throw 'Headless boot has a class loading or uncaught runtime error.'
}
if ($bootErrors -match 'NullPointerException') {
    Write-Host 'Boot reached MIDlet startup; legacy settings loader logged caught exceptions with empty in-memory RMS. Server gameplay remains untested.'
} else {
    Write-Host 'Headless startup smoke check PASS; server gameplay remains untested.'
}
