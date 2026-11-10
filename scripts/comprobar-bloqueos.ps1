# Ejecutar desde la raíz con Java 21 y Maven disponibles.
# Provoca fallos locales controlados y restaura siempre el código original.
$ErrorActionPreference = 'Stop'
$raizProyecto = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $raizProyecto
$rutaCalculadora = Join-Path $raizProyecto 'src/main/java/pronosticos/CalculadoraPronosticos.java'
$original = [System.IO.File]::ReadAllText($rutaCalculadora)
$utf8 = New-Object System.Text.UTF8Encoding($false)
New-Item -ItemType Directory -Force docs/evidencias | Out-Null

function Invocar-Maven {
    param([string[]]$Argumentos, [string]$Log, [bool]$DebeFallar, [string]$Patron)
    # Windows PowerShell representa stderr como ErrorRecord; decide por el código de salida.
    $ErrorActionPreference = 'Continue'
    & mvn -B -ntp @Argumentos 2>&1 | Out-File -Encoding utf8 $Log
    $codigo = $LASTEXITCODE
    if ($DebeFallar) {
        if ($codigo -eq 0 -or -not (Select-String -Path $Log -Pattern $Patron -Quiet)) {
            throw "No se observó el fallo esperado. Revisar $Log"
        }
    } elseif ($codigo -ne 0) {
        throw "Falló la verificación final. Revisar $Log"
    }
    Get-Content -LiteralPath $Log -Tail 12
}

try {
    $conImportacion = $original.Replace('import java.util.List;', "import java.util.List;`nimport java.util.Map;")
    [System.IO.File]::WriteAllText($rutaCalculadora, $conImportacion, $utf8)
    Invocar-Maven -Argumentos @('checkstyle:check') -Log 'docs/evidencias/06-checkstyle-rojo.log' `
        -DebeFallar $true -Patron 'UnusedImports'
} finally {
    [System.IO.File]::WriteAllText($rutaCalculadora, $original, $utf8)
}

try {
    if (-not $original.Contains('return errorPorcentual >= umbral;')) {
        throw 'No se encontró la regla de alerta esperada; no se modifica el código.'
    }
    $mutacion = $original.Replace('return errorPorcentual >= umbral;', 'return errorPorcentual > umbral;')
    [System.IO.File]::WriteAllText($rutaCalculadora, $mutacion, $utf8)
    Invocar-Maven -Argumentos @('test', '-Dtest=AlertasTest,RunCucumberTest') `
        -Log 'docs/evidencias/07-alertas-rojo.log' -DebeFallar $true -Patron 'Failures: [1-9]'
    foreach ($patron in @('AlertasTest.alertaAlIgualarUmbral', 'El error es igual al umbral')) {
        if (-not (Select-String -Path docs/evidencias/07-alertas-rojo.log -Pattern $patron -Quiet)) {
            throw "Falta evidencia del caso esperado: $patron"
        }
    }
} finally {
    [System.IO.File]::WriteAllText($rutaCalculadora, $original, $utf8)
}

# clean evita que los datos de cobertura anteriores oculten la caída de cobertura.
Invocar-Maven -Argumentos @('clean', 'verify', '-Dtest=MadTest') `
    -Log 'docs/evidencias/08-cobertura-rojo.log' -DebeFallar $true -Patron 'coverage checks have not been met'
Invocar-Maven -Argumentos @('clean', 'verify') -Log 'docs/evidencias/09-verificacion-final.log' `
    -DebeFallar $false -Patron ''
Write-Output 'Bloqueos comprobados y código restaurado; verificación final en verde.'
