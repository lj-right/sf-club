package com.jingdiansuifeng.subject.infra.rpc;

import com.jingdiansuifeng.auth.api.UserFeignService;
import com.jingdiansuifeng.auth.entity.AuthUserDTO;
import com.jingdiansuifeng.auth.entity.Result;
import com.jingdiansuifeng.subject.domain.entity.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class UserRpc {

    @Resource
    private UserFeignService userFeignService;

    public UserInfo getUserInfo(String userName) {
        UserInfo userInfo = new UserInfo();
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
            userInfo.setUserName(data.getUserName());
            userInfo.setNickName(data.getNickName());
            userInfo.setAvatar(data.getAvatar());
            return userInfo;
        } catch (Exception e) {
            log.error("userRpc.getUserInfo.error:{}", e.getMessage(), e);
            return userInfo;
        }
    }
}
