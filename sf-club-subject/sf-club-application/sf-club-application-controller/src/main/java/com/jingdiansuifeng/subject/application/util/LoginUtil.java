package com.jingdiansuifeng.subject.application.util;

import com.jingdiansuifeng.subject.application.context.LoginContextHolder;

/**
 * 用户登录util
 */
public class LoginUtil {
    public static String getLoginId(){
        return LoginContextHolder.getLoginId();
    }

}
