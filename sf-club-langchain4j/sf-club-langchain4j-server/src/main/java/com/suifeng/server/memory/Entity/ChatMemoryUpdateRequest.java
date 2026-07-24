package com.suifeng.server.memory.Entity;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;

@Data
public class ChatMemoryUpdateRequest {

    @JsonRawValue
    private String messages;
}
