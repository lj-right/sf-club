package com.suifeng.server.mcp;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@Slf4j
public class McpConfig {

    @Value("${bigmodel.api-key:}")
    private String apiKey;

    @Bean
    @ConditionalOnProperty(name = "bigmodel.api-key")
    public McpToolProvider mcpToolProvider() {
        if (!StringUtils.hasText(apiKey)) {
            log.warn("未配置 bigmodel.api-key，跳过初始化 McpToolProvider");
            return null;
        }
        try {
            // 和 MCP 服务通讯
            McpTransport transport = new HttpMcpTransport.Builder()
                    .sseUrl("https://open.bigmodel.cn/api/mcp/web_search/sse?Authorization=" + apiKey)
                    .logRequests(true)
                    .logResponses(true)
                    .build();
            // 创建 MCP 客户端
            McpClient mcpClient = new DefaultMcpClient.Builder()
                    .key("yupiMcpClient")
                    .transport(transport)
                    .build();
            // 从 MCP 客户端获取工具
            return McpToolProvider.builder()
                    .mcpClients(mcpClient)
                    .build();
        } catch (Exception e) {
            log.warn("初始化 MCP 客户端异常: {}", e.getMessage());
            return null;
        }
    }
}
