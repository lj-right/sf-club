package com.suifeng.server.controller;

import com.suifeng.server.memory.ChatMemoryService;
import com.suifeng.server.memory.Entity.ChatMemoryDto;
import com.suifeng.server.memory.Entity.ChatMemoryUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat-memory")
public class ChatMemoryController {

    private final ChatMemoryService chatMemoryService;

    @GetMapping
    public List<ChatMemoryDto> list() {
        return chatMemoryService.listAll();
    }

    @GetMapping("/{memoryId}")
    public ChatMemoryDto getById(@PathVariable Long memoryId) {
        return chatMemoryService.getById(memoryId);
    }

    @PutMapping("/{memoryId}")
    public ChatMemoryDto upsert(@PathVariable Long memoryId, @RequestBody ChatMemoryUpdateRequest request) {
        return chatMemoryService.upsert(memoryId, request.getMessages());
    }

    @DeleteMapping("/{memoryId}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long memoryId) {
        chatMemoryService.delete(memoryId);
        return ResponseEntity.ok(Map.of("memoryId", memoryId, "deleted", true));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ChatMemoryService.ChatMemoryNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ChatMemoryService.ChatMemoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }
}
