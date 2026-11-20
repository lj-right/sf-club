$old = Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -like "*AuthApplication*" -and $_.Name -like "*java*" }
if ($old) {
    Stop-Process -Id $old.ProcessId -Force -ErrorAction SilentlyContinue
}
Start-Sleep -Seconds 2
$cp = (Get-Content -Path "D:\Work\sf-club\auth_cp.txt" -Raw).Trim()
$p = Start-Process -FilePath "D:\Software\JDK\openjdk-8\bin\java.exe" -ArgumentList "-XX:TieredStopAtLevel=1 -noverify -Dspring.output.ansi.enabled=always -Dfile.encoding=UTF-8 -classpath `"$cp`" com.jingdiansuifeng.auth.AuthApplication" -WorkingDirectory "D:\Work\sf-club\sf-club-auth\sf-club-auth-starter" -PassThru
Write-Output "Started Auth with PID: $($p.Id)"
