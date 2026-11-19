package com.jingdiansuifeng.club.gateway.filter;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 登录过滤器类，实现GlobalFilter接口，用于在请求处理前进行全局过滤和处理
 */
@Component
@Slf4j
public class LoginFilter implements GlobalFilter {

    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder mutate = request.mutate();
        String url = request.getURI().getPath();
        log.info("LoginFilter.filter.url:{}",url);
        if (url.equals("/user/doLogin") || url.equals("/auth/user/doLogin")){
            return chain.filter(exchange);
        }
        String loginId = null;
        try {
            loginId = (String) StpUtil.getLoginIdDefaultNull();
        } catch (Exception ignored) {
        }
        if (StringUtils.isNotEmpty(loginId)){
            //把loginId放到请求头中
            mutate.header("loginId", loginId);
        }
        return chain.filter(exchange.mutate().request(mutate.build()).build());
    }
}
