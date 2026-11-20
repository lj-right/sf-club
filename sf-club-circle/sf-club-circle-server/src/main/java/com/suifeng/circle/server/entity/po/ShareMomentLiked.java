package com.suifeng.circle.server.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 圈子动态点赞表 PO
 */
@Getter
@Setter
@TableName("share_moment_liked")
public class ShareMomentLiked implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long momentId;

    private String likeUserId;

    /**
     * 点赞状态：1点赞，0取消点赞
     */
    private Integer status;

    private String createdBy;

    private Date createdTime;

    private String updateBy;

    private Date updateTime;

    private Integer isDeleted;
}
