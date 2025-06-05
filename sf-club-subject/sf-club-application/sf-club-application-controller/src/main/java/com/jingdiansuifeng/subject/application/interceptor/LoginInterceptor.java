package com.jingdiansuifeng.subject.application.interceptor;

import com.jingdiansuifeng.subject.application.context.LoginContextHolder;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器
 */
public class LoginInterceptor implements HandlerInterceptor {

    public static final String LOGIN_ID = "loginId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String loginId = request.getHeader(LOGIN_ID);
        LoginContextHolder.set(LOGIN_ID, loginId);
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        LoginContextHolder.remove();
    }
}
