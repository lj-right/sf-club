package com.jingdiansuifeng.auth.domain.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.jingdiansuifeng.auth.domain.entity.OAuthLoginBO;
import com.jingdiansuifeng.auth.domain.entity.OAuthRenderBO;

/**
 * 第三方 OAuth 认证领域服务接口 (OAuthDomainService)
 */
public interface OAuthDomainService {

    /**
     * 获取指定平台的授权跳转链接（生成并缓存随机 State 用于防 CSRF 跨站请求伪造）
     *
     * @param platformCode 平台标识: FEISHU, GITHUB, ZHIHU
     * @return 包含 authorizeUrl 与 state 的 BO
     */
    OAuthRenderBO renderAuthorize(String platformCode);

    /**
     * 统一使用 code 换取 token 并完成用户登录与自动建号/账号绑定
     *
     * @param loginBO 包含平台、Code、State
     * @return Sa-Token 会话凭据
     */
    SaTokenInfo doOAuthLogin(OAuthLoginBO loginBO);

}
