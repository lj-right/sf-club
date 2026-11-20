package com.jingdiansuifeng.auth.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 第三方 OAuth 登录请求参数 (OAuthLoginReqDTO)
 */
@Data
public class OAuthLoginReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 平台类型: FEISHU, GITHUB, ZHIHU
     */
    private String platform;

    /**
     * 平台授权返回的临时 Code
     */
    private String code;

    /**
     * 防 CSRF 随机 State 凭证
     */
    private String state;

}
