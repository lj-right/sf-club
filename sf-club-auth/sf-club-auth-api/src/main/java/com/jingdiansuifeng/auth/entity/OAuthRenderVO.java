package com.jingdiansuifeng.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 第三方 OAuth 跳转授权链接响应对象 (OAuthRenderVO)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthRenderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 平台类型
     */
    private String platform;

    /**
     * 跳转授权 URL (包含 AppID、RedirectURI、State 等)
     */
    private String authorizeUrl;

    /**
     * 防 CSRF 随机 State 凭据
     */
    private String state;

}
