package com.jingdiansuifeng.auth.domain.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.google.common.base.Preconditions;
import com.jingdiansuifeng.auth.common.enums.OAuthPlatformEnum;
import com.jingdiansuifeng.auth.domain.entity.AuthUserBO;
import com.jingdiansuifeng.auth.domain.entity.OAuthLoginBO;
import com.jingdiansuifeng.auth.domain.entity.OAuthRenderBO;
import com.jingdiansuifeng.auth.domain.entity.OAuthUserInfo;
import com.jingdiansuifeng.auth.domain.redis.RedisUtil;
import com.jingdiansuifeng.auth.domain.service.AuthUserDomainService;
import com.jingdiansuifeng.auth.domain.service.OAuthDomainService;
import com.jingdiansuifeng.auth.domain.strategy.OAuthStrategy;
import com.jingdiansuifeng.auth.domain.strategy.OAuthStrategyFactory;
import com.jingdiansuifeng.auth.infra.basic.entity.AuthUser;
import com.jingdiansuifeng.auth.infra.basic.entity.AuthUserSocial;
import com.jingdiansuifeng.auth.infra.basic.service.AuthUserService;
import com.jingdiansuifeng.auth.infra.basic.service.AuthUserSocialService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 第三方 OAuth 认证领域服务实现 (OAuthDomainServiceImpl)
 */
@Service("oauthDomainService")
@Slf4j
public class OAuthDomainServiceImpl implements OAuthDomainService {

    @Resource
    private OAuthStrategyFactory oauthStrategyFactory;

    @Resource
    private AuthUserSocialService authUserSocialService;

    @Resource
    private AuthUserService authUserService;

    @Resource
    private AuthUserDomainService authUserDomainService;

    @Resource
    private RedisUtil redisUtil;

    private static final String OAUTH_STATE_PREFIX = "oauth.state";
    private static final String DEFAULT_AVATAR = "http://127.0.0.1:9000/user/avatar/default_avatar.jpg";

    @Override
    public OAuthRenderBO renderAuthorize(String platformCode) {
        Preconditions.checkArgument(StringUtils.isNotBlank(platformCode), "第三方平台类型不能为空");
        OAuthPlatformEnum platformEnum = OAuthPlatformEnum.getByCode(platformCode);
        if (platformEnum == null) {
            throw new IllegalArgumentException("不支持的第三方平台: " + platformCode);
        }

        // 1. 生成防 CSRF 攻击的 state 并写入 Redis (有效期 10 分钟)
        String state = UUID.randomUUID().toString().replace("-", "");
        String stateKey = redisUtil.buildKey(OAUTH_STATE_PREFIX, state);
        redisUtil.setNx(stateKey, platformEnum.getCode(), 10L, TimeUnit.MINUTES);

        // 2. 通过对应策略获取跳转 URL
        OAuthStrategy strategy = oauthStrategyFactory.getStrategy(platformEnum);
        String authorizeUrl = strategy.getAuthorizeUrl(state);

        return OAuthRenderBO.builder()
                .platform(platformEnum.getCode())
                .authorizeUrl(authorizeUrl)
                .state(state)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SaTokenInfo doOAuthLogin(OAuthLoginBO loginBO) {
        Preconditions.checkNotNull(loginBO, "登录参数不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(loginBO.getPlatform()), "平台类型不能为空");
        Preconditions.checkArgument(StringUtils.isNotBlank(loginBO.getCode()), "授权 Code 不能为空");

        OAuthPlatformEnum platformEnum = OAuthPlatformEnum.getByCode(loginBO.getPlatform());
        if (platformEnum == null) {
            throw new IllegalArgumentException("不支持的第三方平台: " + loginBO.getPlatform());
        }

        // 1. 防 CSRF 校验：验证 state
        if (StringUtils.isNotBlank(loginBO.getState())) {
            String stateKey = redisUtil.buildKey(OAUTH_STATE_PREFIX, loginBO.getState());
            String cachedPlatform = redisUtil.get(stateKey);
            if (StringUtils.isBlank(cachedPlatform) || !cachedPlatform.equalsIgnoreCase(platformEnum.getCode())) {
                log.warn("OAuth state check failed or expired, state: {}", loginBO.getState());
                throw new RuntimeException("授权已过期或存在跨站伪造风险，请重新点击登录");
            }
            redisUtil.del(stateKey); // 一次性凭证，使用后立即销毁
        }

        // 2. 调用对应平台策略，换取 access_token 并获取第三方用户信息
        OAuthStrategy strategy = oauthStrategyFactory.getStrategy(platformEnum);
        OAuthUserInfo userInfo = strategy.getUserInfoByCode(loginBO.getCode());
        if (userInfo == null || StringUtils.isBlank(userInfo.getOpenId())) {
            throw new RuntimeException("获取第三方用户授权信息失败");
        }

        // 3. 查询第三方账号绑定关系 (platform + openId)
        AuthUserSocial social = authUserSocialService.queryByPlatformAndOpenId(platformEnum.getCode(), userInfo.getOpenId());
        String loginUserName;

        if (social != null) {
            // 已绑定老用户：直接查询关联的用户主账号
            AuthUser authUser = authUserService.queryById(social.getUserId());
            if (authUser != null && StringUtils.isNotBlank(authUser.getUserName())) {
                loginUserName = authUser.getUserName();
                // 同步更新第三方昵称/头像
                if (StringUtils.isNotBlank(userInfo.getNickname())) {
                    social.setNickname(userInfo.getNickname());
                }
                if (StringUtils.isNotBlank(userInfo.getAvatar())) {
                    social.setAvatar(userInfo.getAvatar());
                }
                authUserSocialService.update(social);
            } else {
                loginUserName = registerAndBindNewUser(platformEnum, userInfo);
            }
        } else {
            // 新用户：自动创建本地 auth_user 账号并插入绑定关系
            loginUserName = registerAndBindNewUser(platformEnum, userInfo);
        }

        // 4. 调用 Sa-Token 完成当前用户登录并签发会话 Token
        StpUtil.login(loginUserName);
        return StpUtil.getTokenInfo();
    }

    /**
     * 自动注册本地新用户并绑定第三方社交账号
     */
    private String registerAndBindNewUser(OAuthPlatformEnum platformEnum, OAuthUserInfo userInfo) {
        String openId = userInfo.getOpenId();
        String suffix = openId.length() > 6 ? openId.substring(openId.length() - 6) : openId;
        String randomSuffix = String.valueOf(System.currentTimeMillis() % 10000);
        String generatedUserName = String.format("sf_%s_%s%s", platformEnum.getCode().toLowerCase(), suffix, randomSuffix);

        // 1. 创建本地用户主账号 (通过 authUserDomainService.register 自动赋予 NORMAL_USER 角色)
        AuthUserBO authUserBO = new AuthUserBO();
        authUserBO.setUserName(generatedUserName);
        authUserBO.setNickName(StringUtils.isNotBlank(userInfo.getNickname()) ? userInfo.getNickname() : "随风的小伙伴");
        authUserBO.setAvatar(StringUtils.isNotBlank(userInfo.getAvatar()) ? userInfo.getAvatar() : DEFAULT_AVATAR);
        authUserBO.setEmail(userInfo.getEmail());
        authUserDomainService.register(authUserBO);

        // 2. 获取新建用户的真实主键 ID
        AuthUser queryCondition = new AuthUser();
        queryCondition.setUserName(generatedUserName);
        List<AuthUser> userList = authUserService.queryByCondition(queryCondition);
        if (CollectionUtils.isEmpty(userList)) {
            throw new RuntimeException("自动注册用户失败");
        }
        Long newUserId = userList.get(0).getId();

        // 3. 写入第三方账号绑定关系
        AuthUserSocial newSocial = new AuthUserSocial();
        newSocial.setUserId(newUserId);
        newSocial.setPlatform(platformEnum.getCode());
        newSocial.setOpenId(userInfo.getOpenId());
        newSocial.setUnionId(userInfo.getUnionId());
        newSocial.setNickname(userInfo.getNickname());
        newSocial.setAvatar(userInfo.getAvatar());
        newSocial.setRawUserInfo(userInfo.getRawUserInfo());
        newSocial.setCreatedBy(generatedUserName);
        authUserSocialService.insert(newSocial);

        return generatedUserName;
    }

}
