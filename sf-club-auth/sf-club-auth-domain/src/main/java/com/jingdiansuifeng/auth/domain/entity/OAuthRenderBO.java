package com.jingdiansuifeng.auth.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 授权链接业务对象 (OAuthRenderBO)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthRenderBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String platform;

    private String authorizeUrl;

    private String state;

}
