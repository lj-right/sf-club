package com.jingdiansuifeng.auth.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录业务参数对象 (OAuthLoginBO)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthLoginBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String platform;

    private String code;

    private String state;

}
