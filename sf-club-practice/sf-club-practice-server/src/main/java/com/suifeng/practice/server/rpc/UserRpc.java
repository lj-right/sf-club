package com.suifeng.practice.server.rpc;

import com.jingdiansuifeng.auth.api.UserFeignService;
import com.jingdiansuifeng.auth.entity.AuthUserDTO;
import com.jingdiansuifeng.auth.entity.Result;
import com.suifeng.practice.server.entity.dto.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class UserRpc {

    private static final String DEFAULT_AVATAR = "http://127.0.0.1:9005/user/avatar/default_avatar.jpg";

    @Resource
    private UserFeignService userFeignService;

    public UserInfo getUserInfo(String userName) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserName(userName);
        userInfo.setNickName(userName);
        userInfo.setAvatar(DEFAULT_AVATAR);
        if (StringUtils.isBlank(userName)) {
            return userInfo;
        }
        try {
            AuthUserDTO authUserDTO = new AuthUserDTO();
            authUserDTO.setUserName(userName);
            Result<AuthUserDTO> result = userFeignService.getUserInfo(authUserDTO);
            if (result == null || !Boolean.TRUE.equals(result.getSuccess()) || result.getData() == null) {
                return userInfo;
            }
            AuthUserDTO data = result.getData();
            if (StringUtils.isNotBlank(data.getUserName())) {
                userInfo.setUserName(data.getUserName());
            }
            if (StringUtils.isNotBlank(data.getNickName())) {
                userInfo.setNickName(data.getNickName());
            }
            if (StringUtils.isNotBlank(data.getAvatar())) {
                userInfo.setAvatar(data.getAvatar());
            }
            return userInfo;
        } catch (Exception e) {
            log.error("practice.userRpc.getUserInfo.error:{}", e.getMessage(), e);
            return userInfo;
        }
    }

}
