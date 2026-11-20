package com.jingdiansuifeng.auth.common.enums;

import lombok.Getter;

/**
 * 第三方 OAuth 平台类型枚举
 */
@Getter
public enum OAuthPlatformEnum {

    FEISHU("FEISHU", "飞书"),
    GITHUB("GITHUB", "GitHub"),
    ZHIHU("ZHIHU", "知乎");

    private final String code;
    private final String desc;

    OAuthPlatformEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OAuthPlatformEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (OAuthPlatformEnum value : values()) {
            if (value.getCode().equalsIgnoreCase(code.trim())) {
                return value;
            }
        }
        return null;
    }

}
