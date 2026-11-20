package com.suifeng.circle.api.req;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 动态点赞请求入参
 */
@Getter
@Setter
public class TouchMomentLikeReq implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 动态ID
     */
    private Long momentId;

    /**
     * 点赞状态：1点赞，0取消点赞
     */
    private Integer status;
}
