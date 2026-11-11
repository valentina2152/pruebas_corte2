# Comprobacion reversible de la regla EmptyStatement. Ejecutar con Java 21.
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath (Split-Path -Parent $PSScriptRoot)
$ruta = Join-Path (Get-Location) 'src/main/java/pronosticos/CalculadoraPronosticos.java'
$original = [IO.File]::ReadAllText($ruta)
$utf8 = New-Object System.Text.UTF8Encoding($false)
$firma = 'public double mse(List<Double> real, List<Double> pronostico) {'
if (-not $original.Contains($firma)) { throw 'No se encontro mse; no se modifica el archivo.' }
try {
    [IO.File]::WriteAllText($ruta, $original.Replace($firma, "$firma`n        ;"), $utf8)
    $ErrorActionPreference = 'Continue'
    & mvn -B -ntp checkstyle:check 2>&1 | Out-File -Encoding utf8 docs/evidencias/regla-propia-rojo.log
    $codigo = $LASTEXITCODE
    $ErrorActionPreference = 'Stop'
    if ($codigo -eq 0 -or -not (Select-String -Path docs/evidencias/regla-propia-rojo.log -Pattern 'EmptyStatement' -Quiet)) {
        throw 'No se observo el fallo de EmptyStatement esperado.'
    }
} finally {
    [IO.File]::WriteAllText($ruta, $original, $utf8)
}
$ErrorActionPreference = 'Continue'
& mvn -B -ntp checkstyle:check 2>&1 | Out-File -Encoding utf8 docs/evidencias/regla-propia-verde.log
if ($LASTEXITCODE -ne 0) { throw 'Checkstyle sigue fallando despues de restaurar.' }
Write-Output 'EmptyStatement detecto la sentencia vacia; codigo restaurado sin infracciones.'
