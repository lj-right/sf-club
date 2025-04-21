package com.jingdiansuifeng.club.gateway.auth;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义权限验证接口扩展
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 返回此 loginId 拥有的权限列表
        ArrayList<String> permissList = new ArrayList<>();
        permissList.add("user:add");
        return permissList;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 返回此 loginId 拥有的角色列表
        ArrayList<String> roleList = new ArrayList<>();
        roleList.add("admin");
        return roleList;
    }

}

