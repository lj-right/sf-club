package com.suifeng.server.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一通用返回体 (与微服务 Result 格式完全对齐)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonResult<T> implements Serializable {

    private Boolean success;
    private Integer code;
    private String message;
    private T data;

    public static <T> CommonResult<T> ok(T data) {
        return CommonResult.<T>builder()
                .success(true)
                .code(200)
                .message("操作成功")
                .data(data)
                .build();
    }

    public static <T> CommonResult<T> fail(String message) {
        return CommonResult.<T>builder()
                .success(false)
                .code(500)
                .message(message)
                .data(null)
                .build();
    }
}
