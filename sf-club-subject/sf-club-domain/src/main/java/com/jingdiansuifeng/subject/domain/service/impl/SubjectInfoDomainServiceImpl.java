package com.jingdiansuifeng.subject.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.jingdiansuifeng.subject.common.entity.PageResult;
import com.jingdiansuifeng.subject.common.enums.IsDeletedFlagEnum;
import com.jingdiansuifeng.subject.common.util.LoginUtil;
import com.jingdiansuifeng.subject.domain.convert.SubjectInfoConverter;
import com.jingdiansuifeng.subject.domain.entity.*;
import com.jingdiansuifeng.subject.domain.redis.RedisUtil;
import com.jingdiansuifeng.subject.domain.service.*;
import com.jingdiansuifeng.subject.domain.handler.SubjectTypeHandler;
import com.jingdiansuifeng.subject.domain.handler.SubjectTypeHandlerFactory;
import com.jingdiansuifeng.subject.domain.entity.UserInfo;
import com.jingdiansuifeng.subject.infra.rpc.UserRpc;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SubjectInfoDomainServiceImpl implements SubjectInfoDomainService {

    @Resource
    private SubjectInfoService subjectInfoService;

    @Resource
    private SubjectTypeHandlerFactory subjectTypeHandlerFactory;

    @Resource
    private SubjectLabelService subjectLabelService;

    @Resource
    private SubjectMappingService subjectMappingService;

    @Resource
    private SubjectCategoryService subjectCategoryService;

    @Resource
    private SubjectEsService subjectEsService;

    @Resource
    private SubjectLikedDomainService subjectLikedDomainService;

    @Resource
    private UserRpc userRpc;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    private static final String RANK_KEY = "subject_rank:";


    @Transactional(rollbackFor = Exception.class)
    public void add(SubjectInfoBO subjectInfoBO) {
        if (log.isInfoEnabled()) {
            log.info("SubjectInfoDomainServiceImpl.add.subjectInfoBO:{}"
                    , JSON.toJSONString(subjectInfoBO));
        }
        //工厂+策略模式
        //一个工厂包含了4种类型，根据传入的type自动映射选择处理
        SubjectInfo subjectInfo = SubjectInfoConverter.INSTANCE
                .convertBoToInfo(subjectInfoBO);
        subjectInfo.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        subjectInfoService.insert(subjectInfo);
        SubjectTypeHandler handler = subjectTypeHandlerFactory.getHandler(subjectInfo.getSubjectType());
        subjectInfoBO.setId(subjectInfo.getId());
        if (handler == null) {
            throw new UnsupportedOperationException("未找到对应的题目类型处理器: " + subjectInfo.getSubjectType());
        }
        handler.add(subjectInfoBO);
        List<Integer> categoryIds = subjectInfoBO.getCategoryIds();
        List<Integer> labelIds = subjectInfoBO.getLabelIds();
        List<SubjectMapping> mappingList = new LinkedList<>();
        categoryIds.forEach(categoryId -> {
            labelIds.forEach(labelId -> {
                SubjectMapping subjectMapping = new SubjectMapping();
                subjectMapping.setSubjectId(subjectInfo.getId());
                subjectMapping.setCategoryId(Long.valueOf(categoryId));
                subjectMapping.setLabelId(Long.valueOf(labelId));
                subjectMapping.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
                mappingList.add(subjectMapping);
            });
        });
        subjectMappingService.batchInsert(mappingList);
        // 组装并丰富 ES 实体数据（包含分类名、标签名及操作类型）
        SubjectInfoEs subjectInfoEs = new SubjectInfoEs();
        subjectInfoEs.setDocId(subjectInfo.getId());
        subjectInfoEs.setSubjectId(subjectInfo.getId());
        subjectInfoEs.setSubjectAnswer(subjectInfoBO.getSubjectAnswer());
        subjectInfoEs.setCreateTime(new Date().getTime());
        subjectInfoEs.setCreateUser(LoginUtil.getLoginId() != null ? LoginUtil.getLoginId() : "suifeng");
        subjectInfoEs.setSubjectName(subjectInfo.getSubjectName());
        subjectInfoEs.setSubjectType(subjectInfo.getSubjectType());
        subjectInfoEs.setOperationType("ADD");

        if (!CollectionUtils.isEmpty(categoryIds)) {
            List<String> categoryNames = new ArrayList<>();
            for (Integer catId : categoryIds) {
                SubjectCategory category = subjectCategoryService.queryById(Long.valueOf(catId));
                if (category != null && category.getCategoryName() != null) {
                    categoryNames.add(category.getCategoryName());
                }
            }
            subjectInfoEs.setCategoryNames(categoryNames);
        }

        if (!CollectionUtils.isEmpty(labelIds)) {
            List<Long> longLabelIds = labelIds.stream().map(Long::valueOf).collect(Collectors.toList());
            List<SubjectLabel> labels = subjectLabelService.batchQueryById(longLabelIds);
            if (!CollectionUtils.isEmpty(labels)) {
                List<String> labelNames = labels.stream().map(SubjectLabel::getLabelName).filter(Objects::nonNull).collect(Collectors.toList());
                subjectInfoEs.setLabelNames(labelNames);
            }
        }

        // 同步es (通过事务同步器在 DB 事务 commit 成功后发送 RocketMQ 消息，实现可靠解耦与最终一致性)
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        if (log.isInfoEnabled()) {
                            log.info("SubjectInfoDomainServiceImpl.add.sendMQ.afterCommit.subjectInfoEs:{}", JSON.toJSONString(subjectInfoEs));
                        }
                        rocketMQTemplate.convertAndSend("subject-index", JSON.toJSONString(subjectInfoEs));
                    } catch (Exception e) {
                        log.error("SubjectInfoDomainServiceImpl.add.sendMQ.afterCommit error", e);
                    }
                }
            });
        } else {
            rocketMQTemplate.convertAndSend("subject-index", JSON.toJSONString(subjectInfoEs));
        }
        // redis放入zadd计入排行榜
        String loginUser = LoginUtil.getLoginId();
        if (loginUser != null && !loginUser.trim().isEmpty()) {
            redisUtil.addScore(RANK_KEY, loginUser, 1);
        }

    }

    @Override
    public PageResult<SubjectInfoBO> getSubjectPage(SubjectInfoBO subjectInfoBO) {
        PageResult<SubjectInfoBO> pageResult = new PageResult<>();
        pageResult.setPageNo(subjectInfoBO.getPageNo());
        pageResult.setPageSize(subjectInfoBO.getPageSize());
        int start = (subjectInfoBO.getPageNo() - 1) * subjectInfoBO.getPageSize();
        SubjectInfo subjectInfo = SubjectInfoConverter.INSTANCE
                .convertBoToInfo(subjectInfoBO);
        int count = subjectInfoService.countByCondition(subjectInfo, subjectInfoBO.getCategoryId()
                ,subjectInfoBO.getLabelId());
        if(count == 0){
            return pageResult;
        }
        List<SubjectInfo> subjectInfoList =  subjectInfoService.queryPage(subjectInfo, subjectInfoBO.getCategoryId()
                ,subjectInfoBO.getLabelId(),start, subjectInfoBO.getPageSize());
        List<SubjectInfoBO> subjectInfoBOS = SubjectInfoConverter.INSTANCE
                .convertInfoListToBoList(subjectInfoList);
        pageResult.setRecords(subjectInfoBOS);
        pageResult.setTotal(count);
        return pageResult;
    }

    @Override
    public SubjectInfoBO querySubjectInfo(SubjectInfoBO subjectInfoBO) {
        SubjectInfo subjectInfo = subjectInfoService.queryById(subjectInfoBO.getId());
        SubjectTypeHandler handler = subjectTypeHandlerFactory.getHandler(subjectInfo.getSubjectType());
        SubjectOptionBO optionBO = handler.query(subjectInfoBO.getId().intValue());
        SubjectInfoBO bo = SubjectInfoConverter.INSTANCE
                .convertOptionAndInfoToBo(optionBO,subjectInfo);
        SubjectMapping subjectMapping = new SubjectMapping();
        subjectMapping.setSubjectId(subjectInfo.getId());
        subjectMapping.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        List<SubjectMapping> mappingList = subjectMappingService.queryLabelId(subjectMapping);
        List<Long> labelIds = mappingList.stream().map(SubjectMapping::getLabelId).collect(Collectors.toList());
        List<SubjectLabel> subjectLabels = subjectLabelService.batchQueryById(labelIds);
        List<String> labelNames = subjectLabels.stream().map(SubjectLabel::getLabelName).collect(Collectors.toList());
        bo.setLabelNames(labelNames);
        bo.setLiked(subjectLikedDomainService.isLiked(subjectInfo.getId().toString(), LoginUtil.getLoginId()));
        bo.setLikedCount(subjectLikedDomainService.getLikedCount(subjectInfo.getId().toString()));
        assembleSubjectCursor(subjectInfoBO,bo);

        return bo;
    }

    private void assembleSubjectCursor(SubjectInfoBO subjectInfoBO,SubjectInfoBO bo) {
        Long categoryId = subjectInfoBO.getCategoryId();
        Long labelId = subjectInfoBO.getLabelId();
        Long subjectId = subjectInfoBO.getId();
        if (Objects.isNull(categoryId) || Objects.isNull(labelId)) {
            return;
        }
        Long nextSubjectId = subjectInfoService.querySubjectIdCursor(categoryId, labelId, subjectId, 1);
        subjectInfoBO.setNextSubjectId(nextSubjectId);
        Long lastSubjectId = subjectInfoService.querySubjectIdCursor(categoryId, labelId, subjectId, 0);
        subjectInfoBO.setNextSubjectId(lastSubjectId);
    }

    @Override
    public PageResult<SubjectInfoEs> getSubjectPageBySearch(SubjectInfoBO subjectInfoBO) {
        SubjectInfoEs subjectInfoEs = new SubjectInfoEs();
        subjectInfoEs.setPageNo(subjectInfoBO.getPageNo());
        subjectInfoEs.setPageSize(subjectInfoBO.getPageSize());
        subjectInfoEs.setKeyWord(subjectInfoBO.getKeyWord());
        return subjectEsService.querySubjectList(subjectInfoEs);

    }

    @Override
    public List<SubjectInfoBO> getContributeList() {
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisUtil.rankWithScore(RANK_KEY, 0, 9);
        if (log.isInfoEnabled()) {
            log.info("getContributeList.typedTuples:{}", JSON.toJSONString(typedTuples));
        }
        if (CollectionUtils.isEmpty(typedTuples)) {
            return Collections.emptyList();
        }
        List<SubjectInfoBO> boList = new LinkedList<>();
        typedTuples.forEach((rank -> {
            if (StringUtils.isBlank(rank.getValue())) {
                return;
            }
            SubjectInfoBO subjectInfoBO = new SubjectInfoBO();
            subjectInfoBO.setSubjectCount(rank.getScore() != null ? rank.getScore().intValue() : 0);
            UserInfo userInfo = userRpc.getUserInfo(rank.getValue());
            subjectInfoBO.setCreateUser(StringUtils.isNotBlank(userInfo.getNickName()) ? userInfo.getNickName() : rank.getValue());
            subjectInfoBO.setCreateUserAvatar(userInfo.getAvatar());
            boList.add(subjectInfoBO);
        }));
        return boList;
    }

    @Override
    public Boolean initSubjectIndex() {
        return subjectEsService.createIndex();
    }

    @Override
    public Boolean syncAllToEs() {
        log.info("SubjectInfoDomainServiceImpl.syncAllToEs start...");
        // 确保索引存在，不存在则创建（包含 ik 中文分词与字段映射）
        subjectEsService.createIndex();

        // 预加载所有有效分类与标签至内存 Map
        SubjectCategory catQuery = new SubjectCategory();
        catQuery.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        List<SubjectCategory> categoryList = subjectCategoryService.queryCategory(catQuery);
        Map<Long, String> categoryMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(categoryList)) {
            categoryList.forEach(c -> categoryMap.put(c.getId(), c.getCategoryName()));
        }

        SubjectLabel labelQuery = new SubjectLabel();
        labelQuery.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        List<SubjectLabel> labelList = subjectLabelService.queryByCondition(labelQuery);
        Map<Long, String> labelMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(labelList)) {
            labelList.forEach(l -> labelMap.put(l.getId(), l.getLabelName()));
        }

        // 分批查询所有未删除题目
        SubjectInfo query = new SubjectInfo();
        query.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        int pageSize = 100;
        int offset = 0;
        int totalSynced = 0;

        while (true) {
            List<SubjectInfo> subjectInfoList = subjectInfoService.queryAllByLimit(query, offset, pageSize);
            if (CollectionUtils.isEmpty(subjectInfoList)) {
                break;
            }

            List<SubjectInfoEs> esList = new LinkedList<>();
            for (SubjectInfo info : subjectInfoList) {
                SubjectInfoEs es = new SubjectInfoEs();
                es.setDocId(info.getId());
                es.setSubjectId(info.getId());
                es.setSubjectName(info.getSubjectName());
                es.setSubjectType(info.getSubjectType());
                es.setCreateUser(info.getCreatedBy());
                es.setCreateTime(info.getCreatedTime() != null ? info.getCreatedTime().getTime() : System.currentTimeMillis());

                // 获取答案
                try {
                    SubjectTypeHandler handler = subjectTypeHandlerFactory.getHandler(info.getSubjectType());
                    if (handler != null) {
                        SubjectOptionBO optionBO = handler.query(info.getId().intValue());
                        if (optionBO != null) {
                            es.setSubjectAnswer(optionBO.getSubjectAnswer());
                        }
                    }
                } catch (Exception e) {
                    log.warn("syncAllToEs get answer error, subjectId:{}", info.getId(), e);
                }

                // 获取题目关联的分类与标签
                try {
                    List<SubjectMapping> mappingList = subjectMappingService.queryBySubjectId(info.getId());
                    if (!CollectionUtils.isEmpty(mappingList)) {
                        List<String> catNames = mappingList.stream()
                                .map(SubjectMapping::getCategoryId)
                                .filter(Objects::nonNull)
                                .distinct()
                                .map(categoryMap::get)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        es.setCategoryNames(catNames);

                        List<String> lblNames = mappingList.stream()
                                .map(SubjectMapping::getLabelId)
                                .filter(Objects::nonNull)
                                .distinct()
                                .map(labelMap::get)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                        es.setLabelNames(lblNames);
                    }
                } catch (Exception e) {
                    log.warn("syncAllToEs get mapping error, subjectId:{}", info.getId(), e);
                }

                esList.add(es);
            }

            boolean batchSuccess = subjectEsService.batchInsert(esList);
            log.info("SubjectInfoDomainServiceImpl.syncAllToEs batch: offset={}, size={}, success={}", offset, esList.size(), batchSuccess);
            totalSynced += esList.size();

            if (subjectInfoList.size() < pageSize) {
                break;
            }
            offset += pageSize;
        }

        log.info("SubjectInfoDomainServiceImpl.syncAllToEs finish, totalSynced:{}", totalSynced);
        return true;
    }

    @Override
    public void syncEsByMsg(SubjectInfoEs subjectInfoEs) {
        if (Objects.isNull(subjectInfoEs) || Objects.isNull(subjectInfoEs.getSubjectId())) {
            return;
        }
        if (log.isInfoEnabled()) {
            log.info("SubjectInfoDomainServiceImpl.syncEsByMsg.subjectInfoEs:{}", JSON.toJSONString(subjectInfoEs));
        }
        if ("DELETE".equalsIgnoreCase(subjectInfoEs.getOperationType())) {
            subjectEsService.delete(subjectInfoEs.getSubjectId());
        } else {
            subjectEsService.insert(subjectInfoEs);
        }
    }
}
