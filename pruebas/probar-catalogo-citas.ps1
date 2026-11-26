# Pruebas de la API de NutriVida: catálogo (servicios, categorías, nutricionistas) y citas.
# Uso (con el backend corriendo en otra terminal):
#   powershell -ExecutionPolicy Bypass -File .\pruebas\probar-catalogo-citas.ps1
# Pide la contraseña del ADMIN (no se guarda) y deja los resultados en pruebas\resultados-catalogo-citas.md

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

function NuevoRut {
    $cuerpo = Get-Random -Minimum 10000000 -Maximum 25000000
    $suma = 0; $mult = 2
    $digitos = $cuerpo.ToString().ToCharArray(); [array]::Reverse($digitos)
    foreach ($d in $digitos) { $suma += [int]::Parse($d) * $mult; $mult = if ($mult -eq 7) { 2 } else { $mult + 1 } }
    $dv = 11 - ($suma % 11)
    $dvTexto = switch ($dv) { 11 { "0" } 10 { "K" } default { "$dv" } }
    return "$cuerpo-$dvTexto"
}

$resultados = New-Object System.Collections.ArrayList
function Registrar($n, $caso, $esperado, $r, $ok, $detalle = "") {
    $msg = ""
    if ($r.datos -and -not ($r.datos -is [array]) -and $r.datos.PSObject.Properties["mensaje"]) { $msg = " - " + $r.datos.mensaje }
    $obtenido = "$($r.estado)$detalle$msg"
    $marca = if ($ok) { "OK" } else { "FALLA" }
    [void]$resultados.Add([pscustomobject]@{ N = $n; Caso = $caso; Esperado = $esperado; Obtenido = $obtenido; Resultado = $marca })
    $color = if ($ok) { "Green" } else { "Red" }
    Write-Host ("[{0}] {1}. {2} -> {3}" -f $marca, $n, $caso, $obtenido) -ForegroundColor $color
}

Write-Host "`nProbando $Api ...`n" -ForegroundColor Cyan
$sufijo = Get-Random -Maximum 99999

# Sesiones: ADMIN y dos pacientes nuevos
$tokA = (Llamar POST "/auth/login" @{ email = $adminEmail; password = $adminPass }).datos.accessToken
if (-not $tokA) { Write-Host "No se pudo iniciar sesión como ADMIN. Revisa la contraseña." -ForegroundColor Red; exit 1 }
$pac = @()
foreach ($i in 1..2) {
    $datos = @{ rut = (NuevoRut); nombre = "Paciente Citas $i"; email = "citas$i.$sufijo@correo.cl"; password = "Clave-prueba-2026" }
    [void](Llamar POST "/auth/register" $datos)
    $pac += @{ datos = $datos; token = (Llamar POST "/auth/login" @{ email = $datos.email; password = $datos.password }).datos.accessToken }
}
$tokP1 = $pac[0].token; $tokP2 = $pac[1].token

# ---------- Catálogo ----------
$r = Llamar GET "/servicios"
Registrar C1 "GET /api/v1/servicios sin iniciar sesión (público)" "200 con los servicios" $r ($r.estado -eq 200 -and @($r.datos).Count -ge 1) ", $(@($r.datos).Count) servicios"
$r = Llamar GET "/categorias"
Registrar C2 "GET /api/v1/categorias (público)" "200 con total de servicios por categoría" $r ($r.estado -eq 200 -and @($r.datos).Count -ge 1) ", $(@($r.datos).Count) categorías"
$r = Llamar GET "/nutricionistas"
Registrar C3 "GET /api/v1/nutricionistas (público)" "200 con días y jornada" $r ($r.estado -eq 200 -and @($r.datos).Count -ge 1) ", $(@($r.datos).Count) nutricionistas"

$nuevo = @{ nombre = "Servicio de prueba $sufijo"; categoria = "Consultas"; tipo = "Consulta"; duracion = "30 min"; modalidad = "Online (video)"; precio = 22000; descripcion = "Creado por el script de pruebas." }
$r = Llamar POST "/servicios" $nuevo
Registrar C4 "POST /api/v1/servicios sin token" "401" $r ($r.estado -eq 401)
$r = Llamar POST "/servicios" $nuevo $tokP1
Registrar C5 "POST /api/v1/servicios con token de PACIENTE" "403" $r ($r.estado -eq 403)
$r = Llamar POST "/servicios" $nuevo $tokA
Registrar C6 "POST /api/v1/servicios con token de ADMIN" "201 con código SV0xx" $r ($r.estado -eq 201 -and $r.datos.codigo -like "SV*") ", código $($r.datos.codigo)"
$idServ = $r.datos.id
$malo = $nuevo.Clone(); $malo.precio = 0; $malo.categoria = "No existe"
$r = Llamar POST "/servicios" $malo $tokA
Registrar C7 "POST /api/v1/servicios con precio 0" "400 con error por campo" $r ($r.estado -eq 400)
$editado = $nuevo.Clone(); $editado.precio = 25000
$r = Llamar PUT "/servicios/$idServ" $editado $tokA
Registrar C8 "PUT /api/v1/servicios/{id} (ADMIN cambia el precio)" "200 con precio 25000" $r ($r.estado -eq 200 -and $r.datos.precio -eq 25000)
$r = Llamar DELETE "/servicios/$idServ" $null $tokA
Registrar C9 "DELETE /api/v1/servicios/{id} (ADMIN)" "204" $r ($r.estado -eq 204)
$r = Llamar GET "/servicios/$idServ"
Registrar C10 "GET del servicio eliminado" "404 (ya no está en el catálogo)" $r ($r.estado -eq 404)

$r = Llamar POST "/categorias" @{ nombre = "Consultas" } $tokA
Registrar C11 "POST /api/v1/categorias con nombre repetido" "409" $r ($r.estado -eq 409)
$r = Llamar POST "/categorias" @{ nombre = "Categoría prueba $sufijo" } $tokA
Registrar C12 "POST /api/v1/categorias nueva (ADMIN)" "201" $r ($r.estado -eq 201)
$idCat = $r.datos.id
$r = Llamar PUT "/categorias/$idCat" @{ nombre = "Categoría renombrada $sufijo" } $tokA
Registrar C13 "PUT /api/v1/categorias/{id} renombrar (ADMIN)" "200" $r ($r.estado -eq 200 -and $r.datos.nombre -like "Categoría renombrada*")
$idConsultas = (@((Llamar GET "/categorias").datos) | Where-Object { $_.nombre -eq "Consultas" }).id
$r = Llamar DELETE "/categorias/$idConsultas" $null $tokA
Registrar C14 "DELETE de una categoría con servicios" "409" $r ($r.estado -eq 409)
$r = Llamar DELETE "/categorias/$idCat" $null $tokA
Registrar C15 "DELETE de una categoría vacía" "204" $r ($r.estado -eq 204)

# ---------- Citas ----------
# Fecha futura al azar y bloque de 30 min dentro de la jornada de NUT003 (08:00 - 13:00)
$fecha = (Get-Date).AddDays((Get-Random -Minimum 30 -Maximum 700)).ToString("yyyy-MM-dd")
$horas = @("08:00","08:30","09:00","09:30","10:00","10:30","11:00","11:30","12:00","12:30")
$hora = $horas | Get-Random
$otraHora = ($horas | Where-Object { $_ -ne $hora }) | Get-Random
$p1 = $pac[0].datos
$cita = @{ nombre = $p1.nombre; rut = $p1.rut; email = $p1.email; telefono = "+56911112222"; servicioId = 1; nutricionistaCodigo = "NUT003"; fecha = $fecha; hora = $hora; motivo = "Prueba automática" }

$r = Llamar POST "/citas" $cita
Registrar A1 "POST /api/v1/citas sin token" "401" $r ($r.estado -eq 401)
$r = Llamar POST "/citas" $cita $tokP1
Registrar A2 "PACIENTE 1 solicita una cita" "201 en estado Pendiente" $r ($r.estado -eq 201 -and $r.datos.estado -eq "Pendiente") ", cita #$($r.datos.id) $fecha $hora"
$idCita = $r.datos.id
$c2 = $cita.Clone(); $c2.nombre = $pac[1].datos.nombre; $c2.rut = $pac[1].datos.rut; $c2.email = $pac[1].datos.email
$r = Llamar POST "/citas" $c2 $tokP2
Registrar A3 "PACIENTE 2 pide el MISMO horario" "409 horario tomado" $r ($r.estado -eq 409)
$pasada = $cita.Clone(); $pasada.fecha = (Get-Date).AddDays(-3).ToString("yyyy-MM-dd")
$r = Llamar POST "/citas" $pasada $tokP1
Registrar A4 "Cita con fecha pasada" "400" $r ($r.estado -eq 400)
$fuera = $cita.Clone(); $fuera.hora = "15:00"
$r = Llamar POST "/citas" $fuera $tokP1
Registrar A5 "Hora fuera de la jornada del nutricionista" "400" $r ($r.estado -eq 400)
$bloque = $cita.Clone(); $bloque.hora = "09:15"
$r = Llamar POST "/citas" $bloque $tokP1
Registrar A6 "Hora que no es bloque de 30 min (09:15)" "400" $r ($r.estado -eq 400)

$r = Llamar GET "/citas" $null $tokP2
Registrar A7 "PACIENTE 2 lista sus citas" "200 sin la cita del paciente 1" $r ($r.estado -eq 200 -and -not (@($r.datos) | Where-Object { $_.id -eq $idCita })) ", $(@($r.datos).Count) citas"
$r = Llamar GET "/citas" $null $tokA
Registrar A8 "ADMIN lista todas las citas" "200 incluye la cita del paciente 1" $r ($r.estado -eq 200 -and (@($r.datos) | Where-Object { $_.id -eq $idCita })) ", $(@($r.datos).Count) citas"
$r = Llamar GET "/citas/$idCita" $null $tokP2
Registrar A9 "PACIENTE 2 abre /citas/{id} del paciente 1" "403" $r ($r.estado -eq 403)
$r = Llamar PATCH "/citas/$idCita/cancelar" $null $tokP2
Registrar A10 "PACIENTE 2 intenta cancelar la cita ajena" "403" $r ($r.estado -eq 403)

$r = Llamar PATCH "/citas/$idCita/reprogramar" @{ fecha = $fecha; hora = $otraHora } $tokP1
Registrar A11 "PACIENTE 1 reprograma su cita" "200 en estado Reprogramada" $r ($r.estado -eq 200 -and $r.datos.estado -eq "Reprogramada") ", nueva hora $otraHora"
$r = Llamar PATCH "/citas/$idCita/estado" @{ estado = "Confirmada" } $tokP1
Registrar A12 "PACIENTE intenta cambiar el estado" "403" $r ($r.estado -eq 403)
$r = Llamar PATCH "/citas/$idCita/estado" @{ estado = "Confirmada" } $tokA
Registrar A13 "ADMIN confirma la cita" "200 en estado Confirmada" $r ($r.estado -eq 200 -and $r.datos.estado -eq "Confirmada")
$r = Llamar PATCH "/citas/$idCita/cancelar" $null $tokP1
Registrar A14 "PACIENTE 1 cancela su cita" "200 en estado Cancelada" $r ($r.estado -eq 200 -and $r.datos.estado -eq "Cancelada")
$r = Llamar PATCH "/citas/$idCita/cancelar" $null $tokP1
Registrar A15 "Cancelar una cita ya cancelada" "409" $r ($r.estado -eq 409)
$c2.hora = $otraHora
$r = Llamar POST "/citas" $c2 $tokP2
Registrar A16 "PACIENTE 2 toma el horario que se liberó" "201" $r ($r.estado -eq 201)

$ok = @($resultados | Where-Object { $_.Resultado -eq "OK" }).Count
Write-Host "`n$ok de $($resultados.Count) pruebas OK`n" -ForegroundColor Cyan

$md = @("# Resultados de pruebas: catálogo y citas", "", "Fecha: $(Get-Date -Format 'yyyy-MM-dd HH:mm') - API: $Api", "",
        "| # | Caso | Resultado esperado | Resultado obtenido | |", "| :--- | :--- | :--- | :--- | :---: |")
foreach ($x in $resultados) { $md += "| $($x.N) | $($x.Caso) | $($x.Esperado) | $($x.Obtenido) | $($x.Resultado) |" }
$archivo = Join-Path $PSScriptRoot "resultados-catalogo-citas.md"
[IO.File]::WriteAllLines($archivo, $md, (New-Object Text.UTF8Encoding $false))
Write-Host "Resultados guardados en $archivo"
