# Pruebas de la API de NutriVida (usuarios, roles y JWT).
# Uso (con el backend corriendo en otra terminal):
#   powershell -ExecutionPolicy Bypass -File .\pruebas\probar-api.ps1
# Pide la contraseña del ADMIN (no se guarda en ningún archivo) y deja los
# resultados en pruebas\resultados.md para copiarlos a la tabla del README.

param([string]$Api = "http://localhost:8080/api/v1")

$adminEmail = if ($env:ADMIN_EMAIL) { $env:ADMIN_EMAIL } else { "admin@nutrivida.cl" }
$adminPass = $env:ADMIN_PASSWORD
if (-not $adminPass) { $adminPass = Read-Host "Contraseña del ADMIN ($adminEmail)" }

function Llamar($metodo, $ruta, $cuerpo = $null, $token = $null) {
    $h = @{ Accept = "application/json" }
    if ($token) { $h.Authorization = "Bearer $token" }
    $p = @{ Method = $metodo; Uri = "$Api$ruta"; Headers = $h; UseBasicParsing = $true }
    if ($null -ne $cuerpo) {
        $p.ContentType = "application/json; charset=utf-8"
        $p.Body = [Text.Encoding]::UTF8.GetBytes(($cuerpo | ConvertTo-Json -Compress))
    }
    try {
        $r = Invoke-WebRequest @p
        $texto = [Text.Encoding]::UTF8.GetString($r.RawContentStream.ToArray())
        $datos = if ($texto) { $texto | ConvertFrom-Json } else { $null }
        return @{ estado = [int]$r.StatusCode; datos = $datos }
    } catch {
        $estado = 0
        if ($_.Exception.Response) { $estado = [int]$_.Exception.Response.StatusCode }
        $datos = $null
        if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
            try { $datos = $_.ErrorDetails.Message | ConvertFrom-Json } catch { }
        }
        return @{ estado = $estado; datos = $datos }
    }
}

# RUT válido al azar (dígito verificador módulo 11)
function NuevoRut {
    $cuerpo = Get-Random -Minimum 10000000 -Maximum 25000000
    $suma = 0; $mult = 2
    foreach ($c in ($cuerpo.ToString().ToCharArray() | Sort-Object { $i-- } )) { }
    $digitos = $cuerpo.ToString().ToCharArray(); [array]::Reverse($digitos)
    foreach ($d in $digitos) { $suma += [int]::Parse($d) * $mult; $mult = if ($mult -eq 7) { 2 } else { $mult + 1 } }
    $dv = 11 - ($suma % 11)
    $dvTexto = switch ($dv) { 11 { "0" } 10 { "K" } default { "$dv" } }
    return "$cuerpo-$dvTexto"
}

$resultados = New-Object System.Collections.ArrayList
function Registrar($n, $caso, $esperado, $r, $ok, $detalle = "") {
    $msg = if ($r.datos -and $r.datos.mensaje) { " - " + $r.datos.mensaje } else { "" }
    $obtenido = "$($r.estado)$msg$detalle"
    $marca = if ($ok) { "OK" } else { "FALLA" }
    [void]$resultados.Add([pscustomobject]@{ N = $n; Caso = $caso; Esperado = $esperado; Obtenido = $obtenido; Resultado = $marca })
    $color = if ($ok) { "Green" } else { "Red" }
    Write-Host ("[{0}] {1}. {2} -> {3}" -f $marca, $n, $caso, $obtenido) -ForegroundColor $color
}

$sufijo = Get-Random -Maximum 99999
$p1 = @{ rut = (NuevoRut); nombre = "Paciente Prueba Uno"; email = "paciente1.$sufijo@correo.cl"; telefono = "+56911112222"; password = "Clave-prueba-2026" }
$p2 = @{ rut = (NuevoRut); nombre = "Paciente Prueba Dos"; email = "paciente2.$sufijo@correo.cl"; password = "Clave-prueba-2026"; rol = "ADMIN" }

Write-Host "`nProbando $Api ...`n" -ForegroundColor Cyan

# 1. Registro válido
$r = Llamar POST "/auth/register" $p1
Registrar 1 "Registro con datos válidos" "201, rol PACIENTE" $r ($r.estado -eq 201 -and $r.datos.rol -eq "PACIENTE") ", rol $($r.datos.rol)"
$idP1 = $r.datos.id

# 2. Correo y RUT repetidos
$r = Llamar POST "/auth/register" $p1
Registrar "2a" "Registro con correo repetido" "409" $r ($r.estado -eq 409)
$rutRep = $p1.Clone(); $rutRep.email = "otro.$sufijo@correo.cl"
$r = Llamar POST "/auth/register" $rutRep
Registrar "2b" "Registro con RUT repetido" "409" $r ($r.estado -eq 409)

# 3. Datos inválidos
$mal = @{ rut = "12.345.678-9"; nombre = "Mal"; email = "x.$sufijo@correo.cl"; password = "123" }
$r = Llamar POST "/auth/register" $mal
$campos = if ($r.datos.errores) { " (" + (($r.datos.errores.PSObject.Properties | ForEach-Object { $_.Name }) -join ", ") + ")" } else { "" }
Registrar 3 "Registro con RUT inválido y contraseña corta" "400 con error por campo" $r ($r.estado -eq 400) $campos

# 4. Intentar registrarse como ADMIN
$r = Llamar POST "/auth/register" $p2
Registrar 4 "Registro enviando rol ADMIN" "201 con rol PACIENTE" $r ($r.estado -eq 201 -and $r.datos.rol -eq "PACIENTE") ", rol $($r.datos.rol)"
$idP2 = $r.datos.id

# 5. Login correcto
$r = Llamar POST "/auth/login" @{ email = $p1.email; password = $p1.password }
Registrar 5 "Login correcto" "200 con accessToken y expiresIn" $r ($r.estado -eq 200 -and $r.datos.accessToken) ", expiresIn $($r.datos.expiresIn)"
$tokP = $r.datos.accessToken

# 6. Login incorrecto (mismo mensaje para clave mala y correo inexistente)
$r1 = Llamar POST "/auth/login" @{ email = $p1.email; password = "Clave-incorrecta" }
$r2 = Llamar POST "/auth/login" @{ email = "noexiste.$sufijo@correo.cl"; password = "Clave-incorrecta" }
Registrar 6 "Login con clave incorrecta / correo inexistente" "401 con el mismo mensaje" $r1 ($r1.estado -eq 401 -and $r2.estado -eq 401 -and $r1.datos.mensaje -eq $r2.datos.mensaje)

# 7. Sin token / token alterado
$r = Llamar GET "/auth/me"
Registrar "7a" "GET /auth/me sin token" "401" $r ($r.estado -eq 401)
$r = Llamar GET "/auth/me" $null ($tokP.Substring(0, $tokP.Length - 3) + "abc")
Registrar "7b" "GET /auth/me con token alterado" "401" $r ($r.estado -eq 401)
$r = Llamar GET "/auth/me" $null $tokP
Registrar "7c" "GET /auth/me con token válido" "200" $r ($r.estado -eq 200) ", $($r.datos.email)"

# Login del ADMIN
$r = Llamar POST "/auth/login" @{ email = $adminEmail; password = $adminPass }
$tokA = $r.datos.accessToken
$idA = $r.datos.usuario.id
if (-not $tokA) { Write-Host "`nNo se pudo iniciar sesión como ADMIN ($($r.estado)). Revisa la contraseña." -ForegroundColor Red }

# 8. Rol insuficiente
$r = Llamar GET "/usuarios" $null $tokP
Registrar 8 "GET /usuarios con token de PACIENTE" "403" $r ($r.estado -eq 403)
$r = Llamar GET "/usuarios" $null $tokA
Registrar "8b" "GET /usuarios con token de ADMIN" "200 con la lista" $r ($r.estado -eq 200) ", $(@($r.datos).Count) usuarios"

# 9. Datos de otra persona (cambiar el id en la URL)
$r = Llamar GET "/usuarios/$idA" $null $tokP
Registrar 9 "PACIENTE consulta /usuarios/{id} de otra persona" "403" $r ($r.estado -eq 403)
$r = Llamar PUT "/usuarios/$idA" @{ nombre = "Hackeo"; email = "hackeo.$sufijo@correo.cl" } $tokP
Registrar "9b" "PACIENTE modifica la cuenta de otra persona" "403" $r ($r.estado -eq 403)
$r = Llamar PUT "/usuarios/$idP1" @{ nombre = "Paciente Prueba Uno Editado"; email = $p1.email; telefono = "+56933334444" } $tokP
Registrar "9c" "PACIENTE modifica su propia cuenta" "200" $r ($r.estado -eq 200) ", nombre: $($r.datos.nombre)"

# 10. Persistencia (el ADMIN se creó en un arranque anterior y sigue en la base)
$r = Llamar GET "/auth/me" $null $tokA
Registrar 10 "Login del ADMIN guardado en PostgreSQL (repetir tras reiniciar)" "200" $r ($r.estado -eq 200)

# 11-13. Roles
$r = Llamar PATCH "/usuarios/$idP2/rol" @{ rol = "NUTRICIONISTA" } $tokA
Registrar 11 "ADMIN cambia el rol de un paciente a NUTRICIONISTA" "200 con el rol nuevo" $r ($r.estado -eq 200 -and $r.datos.rol -eq "NUTRICIONISTA") ", rol $($r.datos.rol)"
$r = Llamar PATCH "/usuarios/$idP2/rol" @{ rol = "ADMIN" } $tokP
Registrar 12 "PACIENTE intenta cambiar un rol" "403" $r ($r.estado -eq 403)
$r = Llamar PATCH "/usuarios/$idA/rol" @{ rol = "PACIENTE" } $tokA
Registrar 13 "ADMIN intenta quitarse su propio rol" "409" $r ($r.estado -eq 409)

# Extra: alta con rol y desactivación por el ADMIN
$nut = @{ rut = (NuevoRut); nombre = "Nutricionista Prueba"; email = "nutri.$sufijo@nutrivida.cl"; password = "Clave-prueba-2026"; rol = "NUTRICIONISTA" }
$r = Llamar POST "/usuarios" $nut $tokA
Registrar "15" "ADMIN crea un usuario NUTRICIONISTA" "201 con rol NUTRICIONISTA" $r ($r.estado -eq 201 -and $r.datos.rol -eq "NUTRICIONISTA")
$idN = $r.datos.id
$r = Llamar DELETE "/usuarios/$idN" $null $tokA
Registrar "16" "ADMIN desactiva un usuario" "204" $r ($r.estado -eq 204)
$r = Llamar POST "/auth/login" @{ email = $nut.email; password = $nut.password }
Registrar "17" "Usuario desactivado intenta iniciar sesión" "401" $r ($r.estado -eq 401)

# Resumen y archivo para el README
$ok = @($resultados | Where-Object { $_.Resultado -eq "OK" }).Count
Write-Host "`n$ok de $($resultados.Count) pruebas OK`n" -ForegroundColor Cyan

$md = @("# Resultados de pruebas de la API", "", "Fecha: $(Get-Date -Format 'yyyy-MM-dd HH:mm') - API: $Api", "",
        "| # | Caso | Resultado esperado | Resultado obtenido | |", "| :--- | :--- | :--- | :--- | :---: |")
foreach ($x in $resultados) { $md += "| $($x.N) | $($x.Caso) | $($x.Esperado) | $($x.Obtenido) | $($x.Resultado) |" }
$archivo = Join-Path $PSScriptRoot "resultados.md"
[IO.File]::WriteAllLines($archivo, $md, (New-Object Text.UTF8Encoding $false))
Write-Host "Resultados guardados en $archivo"
