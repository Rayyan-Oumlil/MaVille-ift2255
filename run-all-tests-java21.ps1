# Script PowerShell pour exécuter TOUS les tests avec Java 21
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Execution de TOUS les tests avec Java 21" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Vérifier si Java 21 est configuré
if (-not $env:JAVA_HOME) {
    Write-Host "ERREUR: JAVA_HOME n'est pas configure!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Veuillez configurer JAVA_HOME pour pointer vers Java 21:" -ForegroundColor Yellow
    Write-Host '  $env:JAVA_HOME = "C:\path\to\java21"' -ForegroundColor White
    Write-Host ""
    Write-Host "Ou trouvez Java 21 avec:" -ForegroundColor Yellow
    Write-Host '  Get-ChildItem "C:\Program Files\Java"' -ForegroundColor White
    Write-Host ""
    exit 1
}

Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green
Write-Host ""

# Vérifier que Java existe à cet emplacement
$javaExe = "$env:JAVA_HOME\bin\java.exe"
if (-not (Test-Path $javaExe)) {
    Write-Host "ERREUR: Java n'a pas ete trouve a: $javaExe" -ForegroundColor Red
    Write-Host ""
    Write-Host "Le chemin JAVA_HOME n'est pas valide." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Vous n'avez probablement pas Java 21 installe." -ForegroundColor Yellow
    Write-Host "Options:" -ForegroundColor Cyan
    Write-Host "  1. Installer Java 21 depuis: https://adoptium.net/temurin/releases/?version=21" -ForegroundColor White
    Write-Host "  2. Utiliser les 17 tests unitaires qui fonctionnent avec Java 25:" -ForegroundColor White
    Write-Host "     .\run-tests.ps1" -ForegroundColor Green
    Write-Host ""
    exit 1
}

# Vérifier la version de Java
try {
    $javaVersionOutput = & $javaExe -version 2>&1 | Out-String
    if ($javaVersionOutput -match "version ""21" -or $javaVersionOutput -match "version 21") {
        Write-Host "Java 21 detecte correctement!" -ForegroundColor Green
        Write-Host ""
    } else {
        Write-Host "ATTENTION: La version Java dans JAVA_HOME n'est pas Java 21!" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "Version detectee:" -ForegroundColor Yellow
        & $javaExe -version
        Write-Host ""
        $continue = Read-Host "Voulez-vous continuer quand meme? (O/N)"
        if ($continue -ne "O" -and $continue -ne "o") {
            exit 1
        }
    }
} catch {
    Write-Host "ERREUR: Impossible d'executer Java a: $javaExe" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}

Write-Host "Execution de tous les tests..." -ForegroundColor Yellow
Write-Host ""

mvn clean test

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Tests completes!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

