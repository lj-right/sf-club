package com.suifeng.circle.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suifeng.circle.api.enums.IsDeletedFlagEnum;
import com.suifeng.circle.api.req.TouchMomentLikeReq;
import com.suifeng.circle.server.config.redis.RedisUtil;
import com.suifeng.circle.server.dao.ShareMomentLikedMapper;
import com.suifeng.circle.server.entity.po.ShareMoment;
import com.suifeng.circle.server.entity.po.ShareMomentLiked;
import com.suifeng.circle.server.service.ShareMessageService;
import com.suifeng.circle.server.service.ShareMomentLikedService;
import com.suifeng.circle.server.service.ShareMomentService;
import com.suifeng.circle.server.util.LoginUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

/**
 * 圈子动态点赞 服务实现类
 */
@Slf4j
@Service
public class ShareMomentLikedServiceImpl extends ServiceImpl<ShareMomentLikedMapper, ShareMomentLiked> implements ShareMomentLikedService {

    @Lazy
    @Resource
    private ShareMomentService shareMomentService;

    @Resource
    private ShareMessageService shareMessageService;

    @Resource
    private RedisUtil redisUtil;

    private static final String MOMENT_LIKED_DETAIL_KEY = "circle.moment.liked.detail";
    private static final String MOMENT_LIKED_COUNT_KEY = "circle.moment.liked.count";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean likeMoment(TouchMomentLikeReq req) {
        String loginId = LoginUtil.getLoginId();
        if (StringUtils.isBlank(loginId)) {
            throw new IllegalArgumentException("请先登录后再进行点赞操作！");
        }
        Long momentId = req.getMomentId();
        Integer targetStatus = (req.getStatus() != null && req.getStatus() == 1) ? 1 : 0;

        ShareMoment moment = shareMomentService.getById(momentId);
        if (moment == null || Objects.equals(moment.getIsDeleted(), IsDeletedFlagEnum.DELETED.getCode())) {
            throw new IllegalArgumentException("该动态不存在或已被删除！");
        }

        LambdaQueryWrapper<ShareMomentLiked> query = Wrappers.<ShareMomentLiked>lambdaQuery()
                .eq(ShareMomentLiked::getMomentId, momentId)
                .eq(ShareMomentLiked::getLikeUserId, loginId);
        ShareMomentLiked exist = super.getOne(query);

        int delta = 0;
        if (exist == null) {
            if (targetStatus == 1) {
                ShareMomentLiked newRecord = new ShareMomentLiked();
                newRecord.setMomentId(momentId);
                newRecord.setLikeUserId(loginId);
                newRecord.setStatus(1);
                newRecord.setCreatedBy(loginId);
                newRecord.setCreatedTime(new Date());
                newRecord.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
                super.save(newRecord);
                delta = 1;
            }
        } else {
            if (!Objects.equals(exist.getStatus(), targetStatus)) {
                exist.setStatus(targetStatus);
                exist.setUpdateBy(loginId);
                exist.setUpdateTime(new Date());
                super.updateById(exist);
                delta = (targetStatus == 1) ? 1 : -1;
            }
        }

        if (delta != 0) {
            shareMomentService.incrLikeCount(momentId, delta);
            String detailKey = MOMENT_LIKED_DETAIL_KEY + "." + momentId + "." + loginId;
            String countKey = MOMENT_LIKED_COUNT_KEY + "." + momentId;
            if (targetStatus == 1) {
                redisUtil.set(detailKey, "1");
                redisUtil.increment(countKey, 1);
                // 给被点赞作者发送通知（如果不是自己给自己点赞）
                if (StringUtils.isNotBlank(moment.getCreatedBy()) && !Objects.equals(loginId, moment.getCreatedBy())) {
                    shareMessageService.like(loginId, moment.getCreatedBy(), momentId);
                }
            } else {
                redisUtil.del(detailKey);
                redisUtil.increment(countKey, -1);
            }
        }
        return true;
    }

    @Override
    public Boolean isLiked(Long momentId, String userId) {
        if (momentId == null || StringUtils.isBlank(userId)) {
            return false;
        }
        String detailKey = MOMENT_LIKED_DETAIL_KEY + "." + momentId + "." + userId;
        if (redisUtil.exist(detailKey)) {
            return true;
        }
        LambdaQueryWrapper<ShareMomentLiked> query = Wrappers.<ShareMomentLiked>lambdaQuery()
                .eq(ShareMomentLiked::getMomentId, momentId)
                .eq(ShareMomentLiked::getLikeUserId, userId)
                .eq(ShareMomentLiked::getStatus, 1)
                .eq(ShareMomentLiked::getIsDeleted, IsDeletedFlagEnum.UN_DELETED.getCode());
        boolean has = super.count(query) > 0;
        if (has) {
            redisUtil.set(detailKey, "1");
        }
        return has;
    }
}
