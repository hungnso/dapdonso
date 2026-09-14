param(
    [string]$OutputJar = "build\e72_latest_exact.jar",
    [string]$BaseJar = "e72_x1.jar",
    [string]$ManifestFile = "META-INF\current_manifest.mf"
)

$ErrorActionPreference = "Stop"
$projectDir = $PSScriptRoot
$classesDir = Join-Path $projectDir "build\classes"
$overlayDir = Join-Path $projectDir "latest_classes"
$baseJar = if ([System.IO.Path]::IsPathRooted($BaseJar)) { $BaseJar } else { Join-Path $projectDir $BaseJar }
$microJar = Join-Path $projectDir "MICRO.jar"
$outputPath = if ([System.IO.Path]::IsPathRooted($OutputJar)) { $OutputJar } else { Join-Path $projectDir $OutputJar }
$manifestPath = if ([System.IO.Path]::IsPathRooted($ManifestFile)) { $ManifestFile } else { Join-Path $projectDir $ManifestFile }

$javacCommand = Get-Command javac.exe -ErrorAction SilentlyContinue
if ($javacCommand) {
    $javac = $javacCommand.Source
} else {
    $jdkCandidates = @(
        "C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot\bin\javac.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-21*\bin\javac.exe",
        "C:\Program Files\Java\jdk-*\bin\javac.exe"
    )
    $javac = $null
    foreach ($candidate in $jdkCandidates) {
        $resolved = Get-ChildItem $candidate -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($resolved) { $javac = $resolved.FullName; break }
    }
}

if (!$javac -or !(Test-Path -LiteralPath $javac)) {
    throw "Khong tim thay javac. Hay cai JDK hoac them javac vao PATH."
}

$jarTool = Join-Path (Split-Path -Parent $javac) "jar.exe"
if (!(Test-Path -LiteralPath $jarTool)) { throw "Khong tim thay jar.exe trong JDK." }
if (!(Test-Path -LiteralPath $baseJar) -or !(Test-Path -LiteralPath $microJar)) { throw "Thieu e72_x1.jar hoac MICRO.jar." }
if (!(Test-Path -LiteralPath $manifestPath)) { throw "Thieu manifest: $manifestPath" }
if (!(Test-Path -LiteralPath $overlayDir)) { throw "Thieu latest_classes/." }

if (Test-Path -LiteralPath $classesDir) { Remove-Item -LiteralPath $classesDir -Recurse -Force }
New-Item -ItemType Directory -Path $classesDir -Force | Out-Null
New-Item -ItemType Directory -Path (Split-Path -Parent $outputPath) -Force | Out-Null

# Bootstrap hai helper reconnect cung nhau truoc khi compile Auto.java.
$bootstrapClassPath = $microJar + ";" + $overlayDir + ";" + $baseJar
$reconnectBootstrapSources = @(
    (Join-Path $projectDir "src\DailyReconnectRecovery.java"),
    (Join-Path $projectDir "src\DeathRouteReconnectRecovery.java")
)
& $javac --release 8 -encoding UTF-8 -cp $bootstrapClassPath -d $classesDir $reconnectBootstrapSources
if ($LASTEXITCODE -ne 0) { throw "Compile reconnect bootstrap that bai." }

# MICRO.jar khong co JSR-120. Giu SmsData.class tu base JAR.
$sources = Get-ChildItem (Join-Path $projectDir "src") -Filter "*.java" |
    Where-Object { $_.Name -ne "SmsData.java" -and $_.Name -ne "AutoDailyCoordinator.java" -and $_.Name -ne "DailyReconnectRecovery.java" -and $_.Name -ne "DeathRouteReconnectRecovery.java" } |
    ForEach-Object { $_.FullName }

$classPath = $microJar + ";" + $overlayDir + ";" + $baseJar + ";" + $classesDir
& $javac --release 8 -encoding UTF-8 -cp $classPath -d $classesDir $sources
if ($LASTEXITCODE -ne 0) { throw "Compile source that bai." }

# Coordinator goi cac API runtime moi trong NSOT_MOB/AutoDailyPanel. Compile
# rieng voi latest_classes dung truoc source classes de dung dung ABI runtime.
$dailyCoordinatorSource = Join-Path $projectDir "src\AutoDailyCoordinator.java"
if (Test-Path -LiteralPath $dailyCoordinatorSource) {
    $runtimeClassPath = $microJar + ";" + $overlayDir + ";" + $baseJar + ";" + $classesDir
    & $javac --release 8 -encoding UTF-8 -cp $runtimeClassPath -d $classesDir $dailyCoordinatorSource
    if ($LASTEXITCODE -ne 0) { throw "Compile AutoDailyCoordinator that bai." }
}

$dailyReconnectSource = Join-Path $projectDir "src\DailyReconnectRecovery.java"
if (Test-Path -LiteralPath $dailyReconnectSource) {
    $runtimeClassPath = $microJar + ";" + $overlayDir + ";" + $baseJar + ";" + $classesDir
    & $javac --release 8 -encoding UTF-8 -cp $runtimeClassPath -d $classesDir $dailyReconnectSource
    if ($LASTEXITCODE -ne 0) { throw "Compile DailyReconnectRecovery that bai." }
}

# Overlay bytecode exact cua baseline runtime cuoi. Cac class ben duoi da duoc
# merge/cap nhat thanh source, nen khong lay class baseline cu de len source moi.
$sourceOwnedClasses = @(
    "AutoDailyPanel.class",
    "AutoDailyCoordinator.class",
    "AutoNvcLinhChiShop.class",
    "AutoVungDatMaQuai.class",
    "CombatSkillPolicy.class",
    "DeathRouteReconnectRecovery.class",
    "TaskAuto.class"
)
Get-ChildItem $overlayDir -Filter "*.class" |
    Where-Object { $sourceOwnedClasses -notcontains $_.Name } |
    ForEach-Object {
    Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $classesDir $_.Name) -Force
}

# Controller source hien chua ngang toan bo runtime exact. Va toi thieu dung
# nhanh het MP tren Controller.class runtime de giu cac patch bytecode khac.
$javaTool = Join-Path (Split-Path -Parent $javac) "java.exe"
$patcherSource = Join-Path $projectDir "tools\PatchControllerMpRecovery.java"
$patcherClasses = Join-Path $projectDir "build\patch-tools"
$asmCoreJar = Join-Path $projectDir "tools\lib\asm-9.7.1.jar"
$asmTreeJar = Join-Path $projectDir "tools\lib\asm-tree-9.7.1.jar"
$asmClassPath = $asmCoreJar + ";" + $asmTreeJar
New-Item -ItemType Directory -Path $patcherClasses -Force | Out-Null
& $javac --release 8 -encoding UTF-8 -cp $asmClassPath -d $patcherClasses $patcherSource
if ($LASTEXITCODE -ne 0) { throw "Compile Controller MP patcher that bai." }
$controllerClass = Join-Path $classesDir "Controller.class"
& $javaTool -cp ($patcherClasses + ";" + $asmClassPath) PatchControllerMpRecovery $controllerClass $controllerClass
if ($LASTEXITCODE -ne 0) { throw "Patch Controller MP recovery that bai." }

# Giu nguyen GameScr.class runtime va chi chen option "Chi nhat vu khi" vao
# bang Tu dong (dong footer cu), kem xu ly touch/phim qua helper source moi.
$gameScrPatcherSource = Join-Path $projectDir "tools\PatchGameScrEquipmentOption.java"
& $javac -cp $asmClassPath -d $patcherClasses $gameScrPatcherSource
if ($LASTEXITCODE -ne 0) { throw "Compile GameScr equipment option patcher that bai." }
$gameScrClass = Join-Path $classesDir "GameScr.class"
& $javaTool -cp ($patcherClasses + ";" + $asmClassPath) PatchGameScrEquipmentOption $gameScrClass $gameScrClass
if ($LASTEXITCODE -ne 0) { throw "Patch GameScr equipment option that bai." }

# Item Xoa dung packet bo vat pham xuong dat. Chan dung item ID do trong
# chinh policy nhat de client khong nhat lai ngay sau khi vua bo ra.
$itemDeletePickupPatcherSource = Join-Path $projectDir "tools\PatchItemAutoDeletePickup.java"
& $javac -cp $asmClassPath -d $patcherClasses $itemDeletePickupPatcherSource
if ($LASTEXITCODE -ne 0) { throw "Compile Item Xoa pickup patcher that bai." }
$nsotMobClass = Join-Path $classesDir "NSOT_MOB.class"
& $javaTool -cp ($patcherClasses + ";" + $asmClassPath) PatchItemAutoDeletePickup $nsotMobClass $nsotMobClass
if ($LASTEXITCODE -ne 0) { throw "Patch Item Xoa pickup policy that bai." }

# Daily tam dung auto hien tai (VDMQ/1-70), chay chuoi con, roi pop ve dung
# instance cu thay vi xoa mat trang thai auto luc bat dau.
$dailyResumePatcherSource = Join-Path $projectDir "tools\PatchDailyResumeFlow.java"
& $javac -cp $asmClassPath -d $patcherClasses $dailyResumePatcherSource
if ($LASTEXITCODE -ne 0) { throw "Compile Daily resume-flow patcher that bai." }
& $javaTool -cp ($patcherClasses + ";" + $asmClassPath) PatchDailyResumeFlow $nsotMobClass
if ($LASTEXITCODE -ne 0) { throw "Patch Daily resume flow that bai." }

# Runtime Ta Thu co them cac route fix chua merge het vao source; chi thay
# maintainFood de dung chung luong mua/dung thuc an moi ma van giu cac fix do.
$taThuFoodPatcherSource = Join-Path $projectDir "tools\PatchTaThuFoodRecovery.java"
& $javac -cp $asmClassPath -d $patcherClasses $taThuFoodPatcherSource
if ($LASTEXITCODE -ne 0) { throw "Compile Ta Thu food patcher that bai." }
$taThuClass = Join-Path $classesDir "TaskTaThuAuto.class"
& $javaTool -cp ($patcherClasses + ";" + $asmClassPath) PatchTaThuFoodRecovery $taThuClass
if ($LASTEXITCODE -ne 0) { throw "Patch Ta Thu food recovery that bai." }

# Neu mat ket noi trong chuoi Daily/Ta Thu, ghi nhan truoc khi Controller dung
# thread va khoi phuc dung chuoi Daily ngay truoc luc GameScr bat lai Auto.
$dailyReconnectPatcherSource = Join-Path $projectDir "tools\PatchDailyReconnectHooks.java"
& $javac -cp $asmClassPath -d $patcherClasses $dailyReconnectPatcherSource
if ($LASTEXITCODE -ne 0) { throw "Compile Daily reconnect patcher that bai." }
& $javaTool -cp ($patcherClasses + ";" + $asmClassPath) PatchDailyReconnectHooks $controllerClass $gameScrClass
if ($LASTEXITCODE -ne 0) { throw "Patch Daily reconnect hooks that bai." }

# Auto Shinwa: mo menu tren mon dang chon, treo tuan tu va chi gui mon tiep
# theo ACK command 102 cua server. Runtime GameScr/Controller duoc hook bang ASM
# de giu nguyen cac patch bytecode hien co.
$shinwaPatcherSource = Join-Path $projectDir "tools\PatchAutoShinwaSale.java"
& $javac -cp $asmClassPath -d $patcherClasses $shinwaPatcherSource
if ($LASTEXITCODE -ne 0) { throw "Compile Auto Shinwa patcher that bai." }
& $javaTool -cp ($patcherClasses + ";" + $asmClassPath) PatchAutoShinwaSale $gameScrClass $controllerClass
if ($LASTEXITCODE -ne 0) { throw "Patch Auto Shinwa that bai." }

# Watchdog rieng cho chuoi NV Hang Ngay/Ta Thu. Neu dang combat khong co
# EXP/damage/task progress, hoac dang route/menu khong di/chuyen map trong 3
# phut, tu reconnect. Truong hop 0 HP bi ket dung timeout ngan hon (1 phut).
$dailyStallPatcherSource = Join-Path $projectDir "tools\PatchDailyStallRecovery.java"
& $javac -cp $asmClassPath -d $patcherClasses $dailyStallPatcherSource
if ($LASTEXITCODE -ne 0) { throw "Compile Daily stall-recovery patcher that bai." }
& $javaTool -cp ($patcherClasses + ";" + $asmClassPath) PatchDailyStallRecovery $nsotMobClass
if ($LASTEXITCODE -ne 0) { throw "Patch Daily stall recovery that bai." }

Copy-Item -LiteralPath $baseJar -Destination $outputPath -Force
& $jarTool ufm $outputPath $manifestPath
if ($LASTEXITCODE -ne 0) { throw "Cap nhat manifest JAR that bai." }

Push-Location $classesDir
try {
    & $jarTool uf $outputPath .
    if ($LASTEXITCODE -ne 0) { throw "Dong goi JAR that bai." }
} finally {
    Pop-Location
}

Write-Host "Build latest-exact thanh cong: $outputPath"
Write-Host "Luu y: class trong latest_classes/ se override source Java cung ten."
Write-Host "Test PC: powershell -ExecutionPolicy Bypass -File .\run-emulator.ps1 -GameJar `"$OutputJar`""
