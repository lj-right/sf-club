package com.suifeng.server.memory.Entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMemoryRecord {

    private Long memoryId;

    private String messages;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    private Short isDeleted;
}
