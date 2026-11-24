package com.suifeng.circle.server.rpc;

import com.jingdiansuifeng.auth.api.UserFeignService;
import com.jingdiansuifeng.auth.entity.AuthUserDTO;
import com.jingdiansuifeng.auth.entity.Result;
import com.suifeng.circle.server.entity.dto.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

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
            log.error("circle.userRpc.getUserInfo.error:{}", e.getMessage(), e);
            return userInfo;
        }
    }

    public Map<String, UserInfo> batchGetUserInfo(List<String> userNameList) {
        if (CollectionUtils.isEmpty(userNameList)) {
            return Collections.emptyMap();
        }
        Map<String, UserInfo> result = new HashMap<>();
        try {
            Result<List<AuthUserDTO>> listResult = userFeignService.listUserInfoByIds(userNameList);
            if (listResult != null && Boolean.TRUE.equals(listResult.getSuccess()) && listResult.getData() != null) {
                for (AuthUserDTO data : listResult.getData()) {
                    UserInfo userInfo = new UserInfo();
                    userInfo.setUserName(data.getUserName());
                    userInfo.setNickName(StringUtils.isNotBlank(data.getNickName()) ? data.getNickName() : data.getUserName());
                    userInfo.setAvatar(StringUtils.isNotBlank(data.getAvatar()) ? data.getAvatar() : DEFAULT_AVATAR);
                    result.put(userInfo.getUserName(), userInfo);
                }
            }
        } catch (Exception e) {
            log.error("circle.userRpc.batchGetUserInfo.error:{}", e.getMessage(), e);
        }
        for (String userName : userNameList) {
            if (!result.containsKey(userName)) {
                UserInfo defaultUser = new UserInfo();
                defaultUser.setUserName(userName);
                defaultUser.setNickName(userName);
                defaultUser.setAvatar(DEFAULT_AVATAR);
                result.put(userName, defaultUser);
            }
        }
        return result;
    }

}
