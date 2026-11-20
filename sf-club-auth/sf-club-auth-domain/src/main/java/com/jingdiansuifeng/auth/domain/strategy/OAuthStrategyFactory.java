package com.jingdiansuifeng.auth.domain.strategy;

import com.jingdiansuifeng.auth.common.enums.OAuthPlatformEnum;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 第三方 OAuth 策略工厂 (OAuthStrategyFactory)
 */
@Component("oauthStrategyFactory")
public class OAuthStrategyFactory implements InitializingBean {

    @Resource
    private List<OAuthStrategy> strategyList;

    private final Map<OAuthPlatformEnum, OAuthStrategy> strategyMap = new EnumMap<>(OAuthPlatformEnum.class);

    @Override
    public void afterPropertiesSet() {
        if (strategyList != null) {
            for (OAuthStrategy strategy : strategyList) {
                strategyMap.put(strategy.getPlatform(), strategy);
            }
        }
    }

    /**
     * 根据平台枚举获取对应的 OAuth 策略实现
     */
    public OAuthStrategy getStrategy(OAuthPlatformEnum platform) {
        OAuthStrategy strategy = strategyMap.get(platform);
        if (strategy == null) {
            throw new IllegalArgumentException("暂不支持该第三方登录平台: " + platform);
        }
        return strategy;
    }

}
