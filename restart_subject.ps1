$proc = Get-NetTCPConnection -LocalPort 3010 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
if ($proc) {
    Write-Output "Stopping process on port 3010 (PID: $proc)..."
    Stop-Process -Id $proc -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
}
Write-Output "Starting sf-club-subject via run_subject.bat..."
$p = Start-Process -FilePath "cmd.exe" -ArgumentList "/c D:\Work\sf-club\run_subject.bat" -WindowStyle Hidden -PassThru
Write-Output "Started run_subject.bat with PID: $($p.Id)"
