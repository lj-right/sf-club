package com.suifeng.circle.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.suifeng.circle.api.req.TouchMomentLikeReq;
import com.suifeng.circle.server.entity.po.ShareMomentLiked;

/**
 * 圈子动态点赞 服务接口
 */
public interface ShareMomentLikedService extends IService<ShareMomentLiked> {

    /**
     * 点赞或取消点赞动态
     */
    Boolean likeMoment(TouchMomentLikeReq req);

    /**
     * 判断指定用户是否已点赞某动态
     */
    Boolean isLiked(Long momentId, String userId);

}
