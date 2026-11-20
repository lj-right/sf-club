package com.jingdiansuifeng.auth.infra.basic.service.impl;

import com.jingdiansuifeng.auth.infra.basic.entity.AuthUserSocial;
import com.jingdiansuifeng.auth.infra.basic.mapper.AuthUserSocialDao;
import com.jingdiansuifeng.auth.infra.basic.service.AuthUserSocialService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 第三方社交账号绑定服务实现 (AuthUserSocialServiceImpl)
 */
@Service("authUserSocialService")
public class AuthUserSocialServiceImpl implements AuthUserSocialService {

    @Resource
    private AuthUserSocialDao authUserSocialDao;

    @Override
    public AuthUserSocial queryById(Long id) {
        return this.authUserSocialDao.queryById(id);
    }

    @Override
    public AuthUserSocial queryByPlatformAndOpenId(String platform, String openId) {
        return this.authUserSocialDao.queryByPlatformAndOpenId(platform, openId);
    }

    @Override
    public List<AuthUserSocial> queryByUserId(Long userId) {
        return this.authUserSocialDao.queryByUserId(userId);
    }

    @Override
    public List<AuthUserSocial> queryAllByLimit(AuthUserSocial authUserSocial) {
        return this.authUserSocialDao.queryAllByLimit(authUserSocial);
    }

    @Override
    public int insert(AuthUserSocial authUserSocial) {
        return this.authUserSocialDao.insert(authUserSocial);
    }

    @Override
    public int update(AuthUserSocial authUserSocial) {
        return this.authUserSocialDao.update(authUserSocial);
    }

    @Override
    public boolean deleteById(Long id) {
        return this.authUserSocialDao.deleteById(id) > 0;
    }

}
