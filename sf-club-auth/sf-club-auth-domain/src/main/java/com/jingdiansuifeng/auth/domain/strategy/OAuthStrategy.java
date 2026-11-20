package com.jingdiansuifeng.auth.domain.strategy;

import com.jingdiansuifeng.auth.common.enums.OAuthPlatformEnum;
import com.jingdiansuifeng.auth.domain.entity.OAuthUserInfo;

/**
 * 统一第三方 OAuth 策略接口 (OAuthStrategy)
 */
public interface OAuthStrategy {

    /**
     * 当前策略支持的第三方平台
     */
    OAuthPlatformEnum getPlatform();

    /**
     * 生成第三方授权跳转 URL
     *
     * @param state 防 CSRF 随机标识
     * @return 完整的平台跳转授权链接
     */
    String getAuthorizeUrl(String state);

    /**
     * 根据授权码换取 access_token 并获取统一的用户信息
     *
     * @param code 授权回调临时 Code
     * @return 标准化用户信息
     */
    OAuthUserInfo getUserInfoByCode(String code);

}
