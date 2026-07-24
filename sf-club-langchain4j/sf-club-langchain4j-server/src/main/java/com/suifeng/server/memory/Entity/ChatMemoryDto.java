package com.suifeng.server.memory.Entity;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMemoryDto {

    private Long memoryId;

    @JsonRawValue
    private String messages;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    private Short isDeleted;
}
