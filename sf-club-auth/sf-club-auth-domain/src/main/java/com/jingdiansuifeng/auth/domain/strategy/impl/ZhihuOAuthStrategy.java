package com.jingdiansuifeng.auth.domain.strategy.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jingdiansuifeng.auth.common.enums.OAuthPlatformEnum;
import com.jingdiansuifeng.auth.domain.entity.OAuthUserInfo;
import com.jingdiansuifeng.auth.domain.strategy.OAuthStrategy;
import com.jingdiansuifeng.auth.domain.util.DictConfigHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 知乎 OAuth 策略实现 (ZhihuOAuthStrategy)
 */
@Component("zhihuOAuthStrategy")
@Slf4j
public class ZhihuOAuthStrategy implements OAuthStrategy {

    @Resource
    private DictConfigHelper dictConfigHelper;

    @Resource(name = "oauthRestTemplate")
    private RestTemplate restTemplate;

    private static final String DICT_TYPE = "oauth_config";
    private static final String DEFAULT_REDIRECT_URI = "http://localhost:5173/oauth/callback/zhihu";
    private static final String ZHIHU_AUTHORIZE_URL = "https://openapi.zhihu.com/authorize";
    private static final String ZHIHU_ACCESS_TOKEN_URL = "https://openapi.zhihu.com/token";
    private static final String ZHIHU_USER_INFO_URL = "https://openapi.zhihu.com/user_info";

    @Override
    public OAuthPlatformEnum getPlatform() {
        return OAuthPlatformEnum.ZHIHU;
    }

    @Override
    public String getAuthorizeUrl(String state) {
        String appId = dictConfigHelper.getValue(DICT_TYPE, "zhihu.app_id", "");
        String redirectUri = dictConfigHelper.getValue(DICT_TYPE, "zhihu.redirect_uri", DEFAULT_REDIRECT_URI);
        try {
            return String.format("%s?redirect_uri=%s&app_id=%s&response_type=code&state=%s",
                    ZHIHU_AUTHORIZE_URL,
                    URLEncoder.encode(redirectUri, "UTF-8"),
                    URLEncoder.encode(appId, "UTF-8"),
                    state
            );
        } catch (Exception e) {
            log.error("ZhihuOAuthStrategy.getAuthorizeUrl error: {}", e.getMessage(), e);
            throw new RuntimeException("生成知乎授权链接失败");
        }
    }

    @Override
    public OAuthUserInfo getUserInfoByCode(String code) {
        String appId = dictConfigHelper.getValue(DICT_TYPE, "zhihu.app_id", "");
        String appSecret = dictConfigHelper.getValue(DICT_TYPE, "zhihu.app_secret", "");
        String redirectUri = dictConfigHelper.getValue(DICT_TYPE, "zhihu.redirect_uri", DEFAULT_REDIRECT_URI);

        // 本地联调/Mock 支持（知乎开放平台通常需要官方邮件审核，审核周期内可直接联调测试）
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(appSecret) || code.startsWith("mock_")) {
            log.info("ZhihuOAuthStrategy using dev mock profile for code: {}", code);
            String mockId = "mock_zh_" + Math.abs(code.hashCode());
            return OAuthUserInfo.builder()
                    .platform(OAuthPlatformEnum.ZHIHU.getCode())
                    .openId(mockId)
                    .nickname("知乎技术极客")
                    .avatar("https://static.zhihu.com/heifetz/assets/apple-touch-icon-152.a53ae37b.png")
                    .email("zhihu_user@sfclub.com")
                    .rawUserInfo("{\"mock\": true, \"platform\": \"ZHIHU\"}")
                    .build();
        }

        // 1. 获取 access_token
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> tokenParams = new HashMap<>();
        tokenParams.put("grant_type", "authorization_code");
        tokenParams.put("client_id", appId);
        tokenParams.put("client_secret", appSecret);
        tokenParams.put("code", code);
        tokenParams.put("redirect_uri", redirectUri);

        HttpEntity<Map<String, String>> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);
        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(ZHIHU_ACCESS_TOKEN_URL, tokenRequest, String.class);
        if (!tokenResponse.getStatusCode().is2xxSuccessful() || StringUtils.isBlank(tokenResponse.getBody())) {
            log.error("知乎 获取 access_token 失败: {}", tokenResponse.getBody());
            throw new RuntimeException("知乎换取 access_token 失败");
        }

        JSONObject tokenJson = JSON.parseObject(tokenResponse.getBody());
        String accessToken = tokenJson.getString("access_token");
        if (StringUtils.isBlank(accessToken)) {
            throw new RuntimeException("知乎未返回有效的 access_token");
        }

        // 2. 获取用户基础信息
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<String> userResponse = restTemplate.exchange(ZHIHU_USER_INFO_URL, HttpMethod.GET, userRequest, String.class);
        if (!userResponse.getStatusCode().is2xxSuccessful() || StringUtils.isBlank(userResponse.getBody())) {
            log.error("知乎 获取用户信息失败: {}", userResponse.getBody());
            throw new RuntimeException("知乎获取用户信息失败");
        }

        JSONObject userJson = JSON.parseObject(userResponse.getBody());
        String openId = userJson.getString("id");
        String name = userJson.getString("name");
        String avatar = userJson.getString("avatar_url");
        String email = userJson.getString("email");

        return OAuthUserInfo.builder()
                .platform(OAuthPlatformEnum.ZHIHU.getCode())
                .openId(openId)
                .nickname(name)
                .avatar(avatar)
                .email(email)
                .rawUserInfo(userResponse.getBody())
                .build();
    }

}
