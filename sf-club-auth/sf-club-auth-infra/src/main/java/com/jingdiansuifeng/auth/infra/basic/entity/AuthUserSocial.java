package com.jingdiansuifeng.auth.infra.basic.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 第三方社交账号绑定实体 (AuthUserSocial)
 */
@Data
public class AuthUserSocial implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String platform;

    private String openId;

    private String unionId;

    private String nickname;

    private String avatar;

    private String rawUserInfo;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

    private Integer isDeleted;

}
