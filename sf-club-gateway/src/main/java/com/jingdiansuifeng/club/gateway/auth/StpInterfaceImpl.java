package com.jingdiansuifeng.club.gateway.auth;

import cn.dev33.satoken.stp.StpInterface;
import com.alibaba.nacos.api.utils.StringUtils;
import com.google.gson.Gson;
import com.jingdiansuifeng.club.gateway.redis.RedisUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 自定义权限验证接口扩展
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private RedisUtil redisUtil;

    private String authPermissionPrefix = "auth:permission:";

    private String authRolePrefix = "auth:role:";

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 返回拥有的权限列表
        return getAuth(loginId.toString(),authPermissionPrefix);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return getAuth(loginId.toString(),authRolePrefix);
    }

    private List<String> getAuth(Object loginId,String prefix) {
        // 返回拥有的角色列表
        String authKey = redisUtil.buildKey(prefix, loginId.toString());
        String authValue = redisUtil.get(authKey);
        if (StringUtils.isBlank(authValue)){
            return Collections.emptyList();
        }
        List<String> authList = new Gson().fromJson(authValue, List.class);
        return authList;
    }

}

