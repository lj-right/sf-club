package com.jingdiansuifeng.auth.infra.basic.service;

import com.jingdiansuifeng.auth.infra.basic.entity.AuthUserSocial;

import java.util.List;

/**
 * 第三方社交账号绑定服务接口 (AuthUserSocialService)
 */
public interface AuthUserSocialService {

    AuthUserSocial queryById(Long id);

    AuthUserSocial queryByPlatformAndOpenId(String platform, String openId);

    List<AuthUserSocial> queryByUserId(Long userId);

    List<AuthUserSocial> queryAllByLimit(AuthUserSocial authUserSocial);

    int insert(AuthUserSocial authUserSocial);

    int update(AuthUserSocial authUserSocial);

    boolean deleteById(Long id);

}
