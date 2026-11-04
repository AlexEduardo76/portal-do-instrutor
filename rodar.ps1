# Roda o Portal do Instrutor localmente, sem PostgreSQL e sem Docker.
# Uso: clique com o botao direito > "Executar com PowerShell" ou rode: .\rodar.ps1

$ErrorActionPreference = "Stop"
$raiz = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $raiz

function AcharJava21 {
    $candidatos = @()

    if ($env:JAVA_HOME) {
        $candidatos += $env:JAVA_HOME
    }

    $pastas = @(
        "$env:USERPROFILE\.jdks",
        "$env:USERPROFILE\.gradle\jdks",
        "C:\Program Files\Java",
        "C:\Program Files\Eclipse Adoptium",
        "C:\Program Files\Microsoft"
    )

    foreach ($pasta in $pastas) {
        if (Test-Path $pasta) {
            $candidatos += Get-ChildItem -Path $pasta -Directory -ErrorAction SilentlyContinue |
                Where-Object { Test-Path (Join-Path $_.FullName "bin\java.exe") } |
                ForEach-Object { $_.FullName }
        }
    }

    foreach ($casa in $candidatos) {
        $java = Join-Path $casa "bin\java.exe"
        if (-not (Test-Path $java)) { continue }
        $versao = (& $java -version 2>&1 | Select-Object -First 1) -join ""
        if ($versao -match '"(?<major>\d+)') {
            if ([int]$Matches.major -ge 21) { return $casa }
        }
    }

    return $null
}

Write-Host ""
Write-Host "Portal do Instrutor - modo local" -ForegroundColor Cyan
Write-Host "--------------------------------" -ForegroundColor DarkGray

if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    throw "Node.js/npm nao encontrado. Instale o Node.js 20+ em https://nodejs.org e tente novamente."
}

$javaHome = AcharJava21
if (-not $javaHome) {
    throw "JDK 21 nao encontrado. Instale o JDK 21 (Temurin ou Microsoft) e tente novamente."
}
$env:JAVA_HOME = $javaHome
Write-Host "Java: $javaHome" -ForegroundColor DarkGray

Write-Host "[1/2] Preparando a interface mobile..." -ForegroundColor Yellow
Push-Location (Join-Path $raiz "mobile")
try {
    if (-not (Test-Path "node_modules")) {
        Write-Host "      Instalando dependencias (npm ci)..." -ForegroundColor DarkGray
        npm ci
        if ($LASTEXITCODE -ne 0) { throw "Falha ao instalar dependencias do mobile." }
    }
    npm run build
    if ($LASTEXITCODE -ne 0) { throw "Falha ao gerar o bundle mobile." }
} finally {
    Pop-Location
}

Write-Host "[2/2] Iniciando a aplicacao em http://localhost:8080" -ForegroundColor Yellow
Write-Host "      Login demo: instrutor / senac123" -ForegroundColor DarkGray
Write-Host "      Pressione Ctrl+C para parar." -ForegroundColor DarkGray

Start-Process -WindowStyle Hidden powershell -ArgumentList @(
    "-NoProfile",
    "-Command",
    "while (-not (Test-NetConnection -ComputerName localhost -Port 8080 -InformationLevel Quiet -WarningAction SilentlyContinue)) { Start-Sleep -Seconds 1 }; Start-Process 'http://localhost:8080'"
) | Out-Null

$env:SPRING_PROFILES_ACTIVE = "local"
$env:PORT = "8080"

Push-Location (Join-Path $raiz "backend")
try {
    .\gradlew.bat bootRun --console=plain
} finally {
    Pop-Location
}
