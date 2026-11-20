package com.jingdiansuifeng.auth.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 标准化第三方用户信息对象 (OAuthUserInfo)
 * 抹平飞书、GitHub、知乎等各开放平台返回的用户属性差异
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthUserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 来源平台
     */
    private String platform;

    /**
     * 平台唯一用户 ID
     */
    private String openId;

    /**
     * 跨应用统一 ID (如有)
     */
    private String unionId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像 URL
     */
    private String avatar;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 平台原始返回 JSON 字符串
     */
    private String rawUserInfo;

}
