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
 * GitHub OAuth 策略实现 (GithubOAuthStrategy)
 */
@Component("githubOAuthStrategy")
@Slf4j
public class GithubOAuthStrategy implements OAuthStrategy {

    @Resource
    private DictConfigHelper dictConfigHelper;

    @Resource(name = "oauthRestTemplate")
    private RestTemplate restTemplate;

    private static final String DICT_TYPE = "oauth_config";
    private static final String DEFAULT_REDIRECT_URI = "http://localhost:5173/oauth/callback/github";
    private static final String GITHUB_AUTHORIZE_URL = "https://github.com/login/oauth/authorize";
    private static final String GITHUB_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_USER_INFO_URL = "https://api.github.com/user";

    @Override
    public OAuthPlatformEnum getPlatform() {
        return OAuthPlatformEnum.GITHUB;
    }

    @Override
    public String getAuthorizeUrl(String state) {
        String clientId = dictConfigHelper.getValue(DICT_TYPE, "github.client_id", "");
        String redirectUri = dictConfigHelper.getValue(DICT_TYPE, "github.redirect_uri", DEFAULT_REDIRECT_URI);
        try {
            return String.format("%s?client_id=%s&redirect_uri=%s&scope=read:user,user:email&state=%s",
                    GITHUB_AUTHORIZE_URL,
                    URLEncoder.encode(clientId, "UTF-8"),
                    URLEncoder.encode(redirectUri, "UTF-8"),
                    state
            );
        } catch (Exception e) {
            log.error("GithubOAuthStrategy.getAuthorizeUrl error: {}", e.getMessage(), e);
            throw new RuntimeException("生成 GitHub 授权链接失败");
        }
    }

    @Override
    public OAuthUserInfo getUserInfoByCode(String code) {
        String clientId = dictConfigHelper.getValue(DICT_TYPE, "github.client_id", "");
        String clientSecret = dictConfigHelper.getValue(DICT_TYPE, "github.client_secret", "");
        String redirectUri = dictConfigHelper.getValue(DICT_TYPE, "github.redirect_uri", DEFAULT_REDIRECT_URI);

        // 本地联调/Mock 支持：当未配置真实 ClientID 或 code 以 mock_ 开头时进入安全测试模式
        if (StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret) || code.startsWith("mock_")) {
            log.info("GithubOAuthStrategy using dev mock profile for code: {}", code);
            String mockId = "mock_gh_" + Math.abs(code.hashCode());
            return OAuthUserInfo.builder()
                    .platform(OAuthPlatformEnum.GITHUB.getCode())
                    .openId(mockId)
                    .nickname("GitHub开发者小伙伴")
                    .avatar("https://avatars.githubusercontent.com/u/9919?s=200&v=4")
                    .email("github_user@sfclub.com")
                    .rawUserInfo("{\"mock\": true, \"platform\": \"GITHUB\"}")
                    .build();
        }

        // 1. 使用 Code 换取 Access Token
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_JSON);
        tokenHeaders.set("Accept", "application/json");

        Map<String, String> tokenParams = new HashMap<>();
        tokenParams.put("client_id", clientId);
        tokenParams.put("client_secret", clientSecret);
        tokenParams.put("code", code);
        tokenParams.put("redirect_uri", redirectUri);

        HttpEntity<Map<String, String>> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);
        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(GITHUB_ACCESS_TOKEN_URL, tokenRequest, String.class);
        if (!tokenResponse.getStatusCode().is2xxSuccessful() || StringUtils.isBlank(tokenResponse.getBody())) {
            log.error("GitHub 获取 access_token 失败, status: {}, body: {}", tokenResponse.getStatusCode(), tokenResponse.getBody());
            throw new RuntimeException("GitHub 换取 access_token 失败");
        }

        JSONObject tokenJson = JSON.parseObject(tokenResponse.getBody());
        String accessToken = tokenJson.getString("access_token");
        if (StringUtils.isBlank(accessToken)) {
            log.error("GitHub 返回数据中未包含 access_token: {}", tokenResponse.getBody());
            throw new RuntimeException("GitHub 认证失败: " + tokenJson.getString("error_description"));
        }

        // 2. 使用 Access Token 获取 GitHub 用户基础信息
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.set("Authorization", "token " + accessToken);
        userHeaders.set("User-Agent", "sf-club-client");
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<String> userResponse = restTemplate.exchange(GITHUB_USER_INFO_URL, HttpMethod.GET, userRequest, String.class);
        if (!userResponse.getStatusCode().is2xxSuccessful() || StringUtils.isBlank(userResponse.getBody())) {
            log.error("GitHub 获取用户信息失败, body: {}", userResponse.getBody());
            throw new RuntimeException("GitHub 获取用户信息失败");
        }

        JSONObject userJson = JSON.parseObject(userResponse.getBody());
        String openId = userJson.getString("id");
        String login = userJson.getString("login");
        String name = userJson.getString("name");
        String nickname = StringUtils.isNotBlank(name) ? name : login;
        String avatar = userJson.getString("avatar_url");
        String email = userJson.getString("email");

        return OAuthUserInfo.builder()
                .platform(OAuthPlatformEnum.GITHUB.getCode())
                .openId(openId)
                .nickname(nickname)
                .avatar(avatar)
                .email(email)
                .rawUserInfo(userResponse.getBody())
                .build();
    }

}
