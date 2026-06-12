# ============================================================
#  GRUP 4 - Nokta Poligon Icinde mi? (Paralel Cozum)
#  Derleme + Calistirma scripti (Windows / PowerShell)
# ============================================================
#  Kullanim:  PowerShell'de bu klasorde ->  .\calistir.ps1
# ============================================================

$ErrorActionPreference = "Stop"
$kok = $PSScriptRoot
$src = Join-Path $kok "src\NoktaPoligonIcinde.java"
$out = Join-Path $kok "out"

# 1) javac'i bul (once PATH, sonra bilinen JDK klasorleri)
$javac = (Get-Command javac -ErrorAction SilentlyContinue).Source
$java  = (Get-Command java  -ErrorAction SilentlyContinue).Source
if (-not $javac) {
    $aday = Get-ChildItem "C:\Program Files\Java","C:\Program Files (x86)\Java" `
        -Recurse -Filter javac.exe -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($aday) {
        $javac = $aday.FullName
        $java  = Join-Path (Split-Path $javac) "java.exe"
    }
}
if (-not $javac) { Write-Error "javac (JDK) bulunamadi. Lutfen bir JDK kurun."; exit 1 }

Write-Host "JDK : $javac" -ForegroundColor Cyan

# 2) Derle (Turkce karakterler icin UTF-8)
New-Item -ItemType Directory -Force $out | Out-Null
& $javac -encoding UTF-8 -d $out $src
if (-not $?) { Write-Error "Derleme basarisiz."; exit 1 }
Write-Host "Derleme tamam.`n" -ForegroundColor Green

# 3) Calistir
& $java -cp $out NoktaPoligonIcinde
