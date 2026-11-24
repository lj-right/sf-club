@echo off
chcp 65001 > nul
echo ========================================================
echo   烤面筋 - LangChain4j AI Agent 智能微服务启动脚本
echo ========================================================
cd /d "%~dp0langchain4j-spring-nexus\langchain4j-spring-nexus-server"
set JAVA_HOME=D:\Software\JDK\openjdk-21
set PATH=%JAVA_HOME%\bin;%PATH%
echo [INFO] 当前环境: OpenJDK 21
echo [INFO] 正在启动 langchain4j-spring-nexus-server (端口: 8012)...

if exist "target\langchain4j-spring-nexus-server-0.0.1-SNAPSHOT.jar" (
    echo [INFO] 发现已编译 JAR 包，正在直接运行...
    "%JAVA_HOME%\bin\java.exe" -jar "target\langchain4j-spring-nexus-server-0.0.1-SNAPSHOT.jar"
) else (
    echo [INFO] 未检测到 JAR 包，正在通过 Maven 启动...
    call "D:\Software\maven\apache-maven-3.9.16\bin\mvn.cmd" spring-boot:run
)
pause
