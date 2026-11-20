package com.jingdiansuifeng.auth.infra.basic.mapper;

import com.jingdiansuifeng.auth.infra.basic.entity.AuthUserSocial;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 第三方社交账号绑定数据库访问层 (AuthUserSocialDao)
 */
public interface AuthUserSocialDao {

    AuthUserSocial queryById(Long id);

    AuthUserSocial queryByPlatformAndOpenId(@Param("platform") String platform, @Param("openId") String openId);

    List<AuthUserSocial> queryByUserId(@Param("userId") Long userId);

    List<AuthUserSocial> queryAllByLimit(AuthUserSocial authUserSocial);

    int insert(AuthUserSocial authUserSocial);

    int update(AuthUserSocial authUserSocial);

    int deleteById(Long id);

}
