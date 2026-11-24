$proc = Get-NetTCPConnection -LocalPort 8012 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
if ($proc) {
    Write-Output "Stopping process on port 8012 (PID: $proc)..."
    Stop-Process -Id $proc -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
}

$jarPath = "D:\Work\sf-club\langchain4j-spring-nexus\langchain4j-spring-nexus-server\target\langchain4j-spring-nexus-server-0.0.1-SNAPSHOT.jar"
$javaExe = "D:\Software\JDK\openjdk-21\bin\java.exe"
$logDir = "D:\Work\sf-club\logs"
if (!(Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir | Out-Null
}
$logFile = "$logDir\langchain4j.log"

Write-Output "Starting langchain4j-spring-nexus-server in background..."
$p = Start-Process -FilePath $javaExe -ArgumentList "-jar `"$jarPath`"" -WorkingDirectory "D:\Work\sf-club" -RedirectStandardOutput $logFile -RedirectStandardError "$logDir\langchain4j_err.log" -WindowStyle Hidden -PassThru
Write-Output "Started langchain4j-spring-nexus-server with PID: $($p.Id), logging to $logFile"
