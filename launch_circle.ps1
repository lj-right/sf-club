Set-Location -Path "D:\Work\sf-club\sf-club-circle\sf-club-circle-server"
$cp = (Get-Content "D:\Work\sf-club\circle_cp.txt" -Raw).Trim()
& "D:\Software\JDK\openjdk-8\bin\java.exe" -XX:TieredStopAtLevel=1 -noverify "-Dspring.output.ansi.enabled=always" "-Dfile.encoding=UTF-8" -classpath $cp com.suifeng.circle.server.CircleApplication
