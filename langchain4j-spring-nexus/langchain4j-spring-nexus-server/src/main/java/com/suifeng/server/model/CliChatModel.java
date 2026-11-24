package com.suifeng.server.model;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 专为 Gemini CLI (agy / gemini) 及第三方 CLI 定制的命令行 ChatModel 驱动器
 */
@Slf4j
@Builder
public class CliChatModel implements ChatModel {

    @Builder.Default
    private String cliCommand = "agy";

    @Builder.Default
    private String modelName = "gemini-3.8-flash-high";

    @Builder.Default
    private int timeoutSeconds = 120;

    /**
     * 自动降级备用 ChatModel (当本地 CLI 超时、异常或不可用时，自动无缝切换，彻底杜绝超时中断)
     */
    private ChatModel fallbackChatModel;

    @Override
    public ChatResponse doChat(ChatRequest chatRequest) {
        String userPrompt = "";
        List<ChatMessage> messages = chatRequest.messages();
        for (ChatMessage msg : messages) {
            if (msg instanceof UserMessage um) {
                userPrompt = um.singleText();
            }
        }

        if (!StringUtils.hasText(userPrompt)) {
            userPrompt = messages.isEmpty() ? "Hello" : messages.get(messages.size() - 1).toString();
        }

        log.info(">>> [Gemini / 第三方 CLI 调用] 命令: {}, 模型: {}, 提示词长度: {}",
                cliCommand, modelName, userPrompt.length());

        String output = executeCliProcess(userPrompt);
        log.info("<<< [Gemini / 第三方 CLI 输出] 响应内容长度: {}", output.length());

        return ChatResponse.builder()
                .aiMessage(AiMessage.from(output))
                .build();
    }

    private String executeCliProcess(String prompt) {
        String resolvedCommand = resolveExecutableCommand(cliCommand);
        List<String> commandList = new ArrayList<>();

        boolean isGeminiOrAgy = resolvedCommand.toLowerCase().contains("agy")
                || resolvedCommand.toLowerCase().contains("gemini");

        if (isGeminiOrAgy) {
            // Gemini CLI (agy) 调用方式: agy --dangerously-skip-permissions [--model <modelName>]
            // 提示词通过 stdin 传入，避免 Windows 命令行双引号转义解析失败及参数长度超限
            commandList.add(resolvedCommand);
            commandList.add("--dangerously-skip-permissions");
            String safeModel = normalizeAgyModelName(modelName);
            if (StringUtils.hasText(safeModel)) {
                commandList.add("--model");
                commandList.add(safeModel);
            }
        } else if (resolvedCommand.toLowerCase().contains("ollama")) {
            // Ollama 调用方式: ollama run <modelName>
            commandList.add(resolvedCommand);
            commandList.add("run");
            commandList.add(StringUtils.hasText(modelName) ? modelName.trim() : "qwen:7b");
        } else {
            // 通用第三方 CLI 调用
            commandList.add(resolvedCommand);
        }

        log.info("[CLI Process] 准备启动进程: {}, 参数: {}", commandList.get(0), commandList);
        ProcessBuilder pb = new ProcessBuilder(commandList);
        pb.directory(new File(System.getProperty("user.home")));
        pb.redirectErrorStream(true);
        StringBuilder output = new StringBuilder();

        try {
            Process process = pb.start();

            // 统一通过 stdin 流将完整提示词（含双引号、换行、复杂项目经验）写入进程并显式关闭流触发 EOF
            try (OutputStream os = process.getOutputStream();
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                writer.write(prompt);
                writer.flush();
            } catch (Exception e) {
                log.warn("[CLI Process] 写入 stdin 异常: {}", e.getMessage());
            }

            // 在独立异步线程中持续排空读取 stdout/stderr，彻底杜绝 Windows 管道缓冲区死锁
            CompletableFuture<Void> readFuture = CompletableFuture.runAsync(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (Exception ignored) {
                }
            });

            boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                log.warn("[CLI Process] CLI 命令执行超时 ({}秒)，已强制终止", timeoutSeconds);
                if (fallbackChatModel != null) {
                    log.info("[CLI Process] >>> 检测到 CLI 超时，立即无缝降级至备用 API 模型输出，保证面试高可用！");
                    try {
                        return fallbackChatModel.chat(prompt);
                    } catch (Exception ex) {
                        log.error("[CLI Process] 备用模型降级调用失败: {}", ex.getMessage());
                    }
                }
                return "抱歉，面试官正在梳理深度技术考题与解析，请稍等或重新作答。";
            }
            try {
                readFuture.get(5, TimeUnit.SECONDS);
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            log.error("[CLI Process] 执行失败: {}", e.getMessage(), e);
            if (fallbackChatModel != null) {
                log.info("[CLI Process] >>> 检测到 CLI 执行异常，立即无缝降级至备用 API 模型输出！");
                try {
                    return fallbackChatModel.chat(prompt);
                } catch (Exception ex) {
                    log.error("[CLI Process] 备用模型降级调用失败: {}", ex.getMessage());
                }
            }
            return "【AI 面试交互异常】" + e.getMessage() + "\n请确认服务配置或大模型连接正常。";
        }

        String resultText = output.toString().trim();
        // 过滤掉可能的进度日志行
        if (resultText.startsWith("Fetching available models...")) {
            int idx = resultText.indexOf("\n");
            if (idx > -1) {
                resultText = resultText.substring(idx + 1).trim();
            }
        }

        if (!StringUtils.hasText(resultText) && fallbackChatModel != null) {
            log.warn("[CLI Process] CLI 输出为空，自动无缝降级至备用 API 模型...");
            try {
                return fallbackChatModel.chat(prompt);
            } catch (Exception ex) {
                log.error("[CLI Process] 备用模型降级调用失败: {}", ex.getMessage());
            }
        }
        return resultText;
    }

    /**
     * 智能定位 CLI 可执行文件完整路径
     */
    private String resolveExecutableCommand(String cmd) {
        if (!StringUtils.hasText(cmd)) {
            cmd = "agy";
        }
        String cleanCmd = cmd.trim();

        // 如果是 agy，优先检测已知默认安装路径
        if ("agy".equalsIgnoreCase(cleanCmd) || "agy.exe".equalsIgnoreCase(cleanCmd)) {
            String defaultAgyPath = "C:\\Users\\25217\\AppData\\Local\\agy\\bin\\agy.exe";
            if (new File(defaultAgyPath).exists()) {
                return defaultAgyPath;
            }
        }
        return cleanCmd;
    }

    /**
     * 自动对齐与纠错 Gemini CLI (agy) 支持的官方模型名称，杜绝模型选型报错
     */
    private String normalizeAgyModelName(String model) {
        if (!StringUtils.hasText(model) || "default".equalsIgnoreCase(model)) {
            return "gemini-3.8-flash-high";
        }
        String lower = model.trim().toLowerCase();
        if (lower.contains("sonnet")) {
            return "claude-sonnet-4-6";
        }
        if (lower.contains("opus")) {
            return "claude-opus-4-6-thinking";
        }
        if (lower.contains("gpt") || lower.contains("oss")) {
            return "gpt-oss-120b-medium";
        }
        if (lower.contains("low")) {
            return "gemini-3.8-flash-low";
        }
        if (lower.contains("medium")) {
            return "gemini-3.8-flash-medium";
        }
        // 针对 gemini-3.8-flash-high, gemini-2.5-flash, gemini-1.5 等各种别名，统一对齐至官方最新高算力模型
        return "gemini-3.8-flash-high";
    }
}
