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
 * 飞书 OAuth 策略实现 (FeishuOAuthStrategy)
 */
@Component("feishuOAuthStrategy")
@Slf4j
public class FeishuOAuthStrategy implements OAuthStrategy {

    @Resource
    private DictConfigHelper dictConfigHelper;

    @Resource(name = "oauthRestTemplate")
    private RestTemplate restTemplate;

    private static final String DICT_TYPE = "oauth_config";
    private static final String DEFAULT_REDIRECT_URI = "http://localhost:5173/oauth/callback/feishu";
    private static final String FEISHU_AUTHORIZE_URL = "https://open.feishu.cn/open-apis/authen/v1/index";
    private static final String FEISHU_ACCESS_TOKEN_URL = "https://open.feishu.cn/open-apis/authen/v1/oidc/access_token";
    private static final String FEISHU_USER_INFO_URL = "https://open.feishu.cn/open-apis/authen/v1/user_info";

    @Override
    public OAuthPlatformEnum getPlatform() {
        return OAuthPlatformEnum.FEISHU;
    }

    @Override
    public String getAuthorizeUrl(String state) {
        String appId = dictConfigHelper.getValue(DICT_TYPE, "feishu.app_id", "");
        String redirectUri = dictConfigHelper.getValue(DICT_TYPE, "feishu.redirect_uri", DEFAULT_REDIRECT_URI);
        try {
            return String.format("%s?app_id=%s&redirect_uri=%s&state=%s",
                    FEISHU_AUTHORIZE_URL,
                    URLEncoder.encode(appId, "UTF-8"),
                    URLEncoder.encode(redirectUri, "UTF-8"),
                    state
            );
        } catch (Exception e) {
            log.error("FeishuOAuthStrategy.getAuthorizeUrl error: {}", e.getMessage(), e);
            throw new RuntimeException("生成飞书授权链接失败");
        }
    }

    @Override
    public OAuthUserInfo getUserInfoByCode(String code) {
        String appId = dictConfigHelper.getValue(DICT_TYPE, "feishu.app_id", "");
        String appSecret = dictConfigHelper.getValue(DICT_TYPE, "feishu.app_secret", "");

        // 本地联调/Mock 支持
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(appSecret) || code.startsWith("mock_")) {
            log.info("FeishuOAuthStrategy using dev mock profile for code: {}", code);
            String mockId = "mock_fs_" + Math.abs(code.hashCode());
            return OAuthUserInfo.builder()
                    .platform(OAuthPlatformEnum.FEISHU.getCode())
                    .openId(mockId)
                    .unionId("on_" + mockId)
                    .nickname("飞书技术同学")
                    .avatar("https://lf1-cdn-tos.bytegoofy.com/goofy/lark/passport/favicon.ico")
                    .email("feishu_user@sfclub.com")
                    .rawUserInfo("{\"mock\": true, \"platform\": \"FEISHU\"}")
                    .build();
        }

        // 1. 获取 user_access_token
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> tokenParams = new HashMap<>();
        tokenParams.put("grant_type", "authorization_code");
        tokenParams.put("client_id", appId);
        tokenParams.put("client_secret", appSecret);
        tokenParams.put("code", code);

        HttpEntity<Map<String, String>> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);
        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(FEISHU_ACCESS_TOKEN_URL, tokenRequest, String.class);
        if (!tokenResponse.getStatusCode().is2xxSuccessful() || StringUtils.isBlank(tokenResponse.getBody())) {
            log.error("飞书 获取 access_token 失败: {}", tokenResponse.getBody());
            throw new RuntimeException("飞书换取 access_token 失败");
        }

        JSONObject tokenJson = JSON.parseObject(tokenResponse.getBody());
        Integer errCode = tokenJson.getInteger("code");
        if (errCode != null && errCode != 0) {
            log.error("飞书返回错误码: {}, msg: {}", errCode, tokenJson.getString("msg"));
            throw new RuntimeException("飞书认证失败: " + tokenJson.getString("msg"));
        }

        JSONObject dataObj = tokenJson.getJSONObject("data");
        String accessToken = dataObj != null ? dataObj.getString("access_token") : tokenJson.getString("access_token");
        if (StringUtils.isBlank(accessToken)) {
            throw new RuntimeException("飞书响应中未包含 access_token");
        }

        // 2. 获取用户基础信息
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<String> userResponse = restTemplate.exchange(FEISHU_USER_INFO_URL, HttpMethod.GET, userRequest, String.class);
        if (!userResponse.getStatusCode().is2xxSuccessful() || StringUtils.isBlank(userResponse.getBody())) {
            log.error("飞书 获取用户信息失败: {}", userResponse.getBody());
            throw new RuntimeException("飞书获取用户信息失败");
        }

        JSONObject userJson = JSON.parseObject(userResponse.getBody());
        JSONObject userData = userJson.getJSONObject("data");
        if (userData == null) {
            userData = userJson;
        }

        String openId = userData.getString("open_id");
        String unionId = userData.getString("union_id");
        String name = userData.getString("name");
        String avatar = userData.getString("avatar_url");
        String email = userData.getString("email");

        return OAuthUserInfo.builder()
                .platform(OAuthPlatformEnum.FEISHU.getCode())
                .openId(openId)
                .unionId(unionId)
                .nickname(name)
                .avatar(avatar)
                .email(email)
                .rawUserInfo(userResponse.getBody())
                .build();
    }

}
