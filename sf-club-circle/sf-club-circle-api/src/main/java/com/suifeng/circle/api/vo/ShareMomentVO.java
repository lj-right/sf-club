package com.suifeng.circle.api.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 动态信息 VO
 * </p>
 */
@Getter
@Setter
public class ShareMomentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 动态ID
     */
    private Long id;

    /**
     * 圈子ID
     */
    private Long circleId;

    /**
     * 动态内容
     */
    private String content;

    /**
     * 动态图片内容
     */
    private List<String> picUrlList;

    /**
     * 回复数
     */
    private Integer replyCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 兼容前端字段 likes
     */
    private Integer likes;

    /**
     * 当前登录用户是否已点赞
     */
    private Boolean hasLiked;

    /**
     * 兼容前端字段 liked
     */
    private Boolean liked;

    /**
     * 创建时间
     */
    private Long createdTime;

    private String userName;

    private String userAvatar;

    public Integer getLikes() {
        return likes != null ? likes : (likeCount != null ? likeCount : 0);
    }

    public Integer getLikeCount() {
        return likeCount != null ? likeCount : (likes != null ? likes : 0);
    }

    public Boolean getLiked() {
        return liked != null ? liked : (hasLiked != null ? hasLiked : false);
    }

    public Boolean getHasLiked() {
        return hasLiked != null ? hasLiked : (liked != null ? liked : false);
    }
}
