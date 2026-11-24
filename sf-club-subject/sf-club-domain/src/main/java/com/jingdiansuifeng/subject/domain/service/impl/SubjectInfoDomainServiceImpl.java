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
import com.jingdiansuifeng.subject.domain.util.SubjectAutoImportCorpus;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
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
                    if (subjectInfoEs.getCategoryId() == null) {
                        subjectInfoEs.setCategoryId(category.getId());
                        subjectInfoEs.setCategoryName(category.getCategoryName());
                        if (category.getParentId() != null && category.getParentId() != 0) {
                            SubjectCategory primaryCat = subjectCategoryService.queryById(category.getParentId());
                            if (primaryCat != null) {
                                subjectInfoEs.setPrimaryCategoryId(primaryCat.getId());
                                subjectInfoEs.setPrimaryCategoryName(primaryCat.getCategoryName());
                            }
                        }
                    }
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
                if (subjectInfoEs.getLabelId() == null && !labels.isEmpty()) {
                    subjectInfoEs.setLabelId(labels.get(0).getId());
                    subjectInfoEs.setLabelName(labels.get(0).getLabelName());
                }
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
        subjectInfoEs.setPageNo(subjectInfoBO.getPageNo() != null && subjectInfoBO.getPageNo() > 0 ? subjectInfoBO.getPageNo() : 1);
        subjectInfoEs.setPageSize(subjectInfoBO.getPageSize() != null && subjectInfoBO.getPageSize() > 0 ? subjectInfoBO.getPageSize() : 10);
        subjectInfoEs.setKeyWord(subjectInfoBO.getKeyWord());
        subjectInfoEs.setSubjectType(subjectInfoBO.getSubjectType());

        PageResult<SubjectInfoEs> pageResult = null;
        try {
            pageResult = subjectEsService.querySubjectList(subjectInfoEs);
        } catch (Exception e) {
            log.warn("querySubjectList from ES failed, will fallback to DB search: {}", e.getMessage());
        }

        // 若 ES 检索成功且有结果，直接返回 ES 检索结果
        if (pageResult != null && pageResult.getTotal() != null && pageResult.getTotal() > 0) {
            return pageResult;
        }

        // 当 ES 服务异常、未开启、连接超时或 ES 索引无数据时，降级查询 MySQL 数据库并自动组装高亮结果
        log.info("ES query returned 0 results or unavailable, fallback to DB search for keyWord:[{}]", subjectInfoBO.getKeyWord());
        return querySubjectPageByDbSearch(subjectInfoBO);
    }

    private PageResult<SubjectInfoEs> querySubjectPageByDbSearch(SubjectInfoBO subjectInfoBO) {
        PageResult<SubjectInfoEs> pageResult = new PageResult<>();
        int pageNo = (subjectInfoBO.getPageNo() != null && subjectInfoBO.getPageNo() > 0) ? subjectInfoBO.getPageNo() : 1;
        int pageSize = (subjectInfoBO.getPageSize() != null && subjectInfoBO.getPageSize() > 0) ? subjectInfoBO.getPageSize() : 10;
        int start = (pageNo - 1) * pageSize;

        String keyWord = subjectInfoBO.getKeyWord();
        Integer subjectType = subjectInfoBO.getSubjectType();

        int count = subjectInfoService.countBySearch(keyWord, subjectType);
        pageResult.setPageNo(pageNo);
        pageResult.setPageSize(pageSize);

        if (count == 0) {
            pageResult.setRecords(Collections.emptyList());
            pageResult.setTotal(0);
            return pageResult;
        }

        List<SubjectInfo> infoList = subjectInfoService.queryBySearch(keyWord, subjectType, start, pageSize);
        if (CollectionUtils.isEmpty(infoList)) {
            pageResult.setRecords(Collections.emptyList());
            pageResult.setTotal(count);
            return pageResult;
        }

        List<SubjectInfoEs> esList = new LinkedList<>();
        for (SubjectInfo info : infoList) {
            SubjectInfoEs es = new SubjectInfoEs();
            es.setDocId(info.getId());
            es.setSubjectId(info.getId());
            es.setSubjectType(info.getSubjectType());
            es.setCreateUser(info.getCreatedBy());
            es.setCreateTime(info.getCreatedTime() != null ? info.getCreatedTime().getTime() : System.currentTimeMillis());

            // 匹配度打分
            if (StringUtils.isNotBlank(info.getSubjectName()) && StringUtils.isNotBlank(keyWord)
                    && info.getSubjectName().toLowerCase().contains(keyWord.toLowerCase())) {
                es.setScore(new BigDecimal("100.00"));
            } else {
                es.setScore(new BigDecimal("80.00"));
            }

            // 获取答案与解析
            String answer = "";
            try {
                SubjectTypeHandler handler = subjectTypeHandlerFactory.getHandler(info.getSubjectType());
                if (handler != null) {
                    SubjectOptionBO optionBO = handler.query(info.getId().intValue());
                    if (optionBO != null && StringUtils.isNotBlank(optionBO.getSubjectAnswer())) {
                        answer = optionBO.getSubjectAnswer();
                    }
                }
            } catch (Exception e) {
                log.warn("querySubjectPageByDbSearch get answer error, subjectId:{}", info.getId());
            }
            if (StringUtils.isBlank(answer) && StringUtils.isNotBlank(info.getSubjectParse())) {
                answer = info.getSubjectParse();
            }

            // 标题高亮处理
            String highlightedName = info.getSubjectName();
            if (StringUtils.isNotBlank(highlightedName) && StringUtils.isNotBlank(keyWord)) {
                highlightedName = highlightedName.replaceAll("(?i)" + Pattern.quote(keyWord), "<b style = \"color:red\">$0</b>");
            }
            es.setSubjectName(highlightedName);

            // 答案高亮处理
            String highlightedAnswer = answer;
            if (StringUtils.isNotBlank(highlightedAnswer) && StringUtils.isNotBlank(keyWord)) {
                highlightedAnswer = highlightedAnswer.replaceAll("(?i)" + Pattern.quote(keyWord), "<b style = \"color:red\">$0</b>");
            }
            es.setSubjectAnswer(highlightedAnswer);

            // 获取关联的分类与标签名称
            try {
                List<SubjectMapping> mappingList = subjectMappingService.queryBySubjectId(info.getId());
                if (!CollectionUtils.isEmpty(mappingList)) {
                    List<Long> catIds = mappingList.stream()
                            .map(SubjectMapping::getCategoryId)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(catIds)) {
                        List<String> catNames = new ArrayList<>();
                        for (Long cId : catIds) {
                            SubjectCategory cat = subjectCategoryService.queryById(cId);
                            if (cat != null && StringUtils.isNotBlank(cat.getCategoryName())) {
                                catNames.add(cat.getCategoryName());
                                if (es.getCategoryId() == null) {
                                    es.setCategoryId(cat.getId());
                                    es.setCategoryName(cat.getCategoryName());
                                    if (cat.getParentId() != null && cat.getParentId() != 0) {
                                        SubjectCategory primaryCat = subjectCategoryService.queryById(cat.getParentId());
                                        if (primaryCat != null) {
                                            es.setPrimaryCategoryId(primaryCat.getId());
                                            es.setPrimaryCategoryName(primaryCat.getCategoryName());
                                        }
                                    }
                                }
                            }
                        }
                        es.setCategoryNames(catNames);
                    }

                    List<Long> lblIds = mappingList.stream()
                            .map(SubjectMapping::getLabelId)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(lblIds)) {
                        List<SubjectLabel> labels = subjectLabelService.batchQueryById(lblIds);
                        if (!CollectionUtils.isEmpty(labels)) {
                            List<String> lblNames = labels.stream()
                                    .map(SubjectLabel::getLabelName)
                                    .filter(StringUtils::isNotBlank)
                                    .collect(Collectors.toList());
                            es.setLabelNames(lblNames);
                            if (es.getLabelId() == null && !labels.isEmpty()) {
                                es.setLabelId(labels.get(0).getId());
                                es.setLabelName(labels.get(0).getLabelName());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("querySubjectPageByDbSearch get mappings error, subjectId:{}", info.getId());
            }

            esList.add(es);
        }

        pageResult.setRecords(esList);
        pageResult.setTotal(count);
        return pageResult;
    }

    @Override
    public List<SubjectInfoBO> getContributeList() {
        Set<ZSetOperations.TypedTuple<String>> typedTuples = redisUtil.rankWithScore(RANK_KEY, 0, 9);
        if (log.isInfoEnabled()) {
            log.info("getContributeList.typedTuples:{}", JSON.toJSONString(typedTuples));
        }
        if (CollectionUtils.isEmpty(typedTuples)) {
            // 当 Redis 无排行榜缓存时，降级查询数据库聚合出题榜并自动回填 Redis
            List<SubjectInfo> dbList = subjectInfoService.getContributeCount();
            if (CollectionUtils.isEmpty(dbList)) {
                return Collections.emptyList();
            }
            List<SubjectInfoBO> boList = new LinkedList<>();
            for (SubjectInfo info : dbList) {
                if (StringUtils.isBlank(info.getCreatedBy())) {
                    continue;
                }
                // 回填 Redis 排行榜
                redisUtil.addScore(RANK_KEY, info.getCreatedBy(), info.getSubjectCount() != null ? info.getSubjectCount().doubleValue() : 0.0);

                SubjectInfoBO subjectInfoBO = new SubjectInfoBO();
                subjectInfoBO.setSubjectCount(info.getSubjectCount());
                UserInfo userInfo = userRpc.getUserInfo(info.getCreatedBy());
                subjectInfoBO.setCreateUser(StringUtils.isNotBlank(userInfo.getNickName()) ? userInfo.getNickName() : info.getCreatedBy());
                subjectInfoBO.setCreateUserAvatar(userInfo.getAvatar());
                boList.add(subjectInfoBO);
            }
            return boList;
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

    private static final AtomicBoolean IS_IMPORTING = new AtomicBoolean(false);

    @Override
    public SubjectAutoImportResultBO autoImportSubjects(SubjectAutoImportBO importBO) {
        String loginId = LoginUtil.getLoginId();
        log.info("SubjectInfoDomainServiceImpl.autoImportSubjects.loginId:{}", loginId);

        // 严格仅管理员校验
        if (StringUtils.isBlank(loginId) || (!"admin".equalsIgnoreCase(loginId) && !"super_user".equalsIgnoreCase(loginId))) {
            throw new SecurityException("无操作权限：自动录取题目功能仅限系统管理员执行。");
        }

        // 并发防重复执行
        if (!IS_IMPORTING.compareAndSet(false, true)) {
            throw new IllegalStateException("当前已有自动录取任务正在执行中，请稍后刷新重试。");
        }

        long startTime = System.currentTimeMillis();
        SubjectAutoImportResultBO result = new SubjectAutoImportResultBO();
        List<String> sampleTitles = new ArrayList<>();
        Set<Integer> touchedCategories = new HashSet<>();
        Set<Long> touchedLabels = new HashSet<>();

        try {
            String source = importBO != null && StringUtils.isNotBlank(importBO.getSource()) ? importBO.getSource() : "ALL";
            int limit = importBO != null && importBO.getLimit() != null && importBO.getLimit() > 0 ? importBO.getLimit() : 100;

            // 1. 查询数据库已有题目标题全量用于严格去重
            List<String> existingNames = subjectInfoService.queryAllSubjectNames();
            Set<String> seen = new HashSet<>();
            if (!CollectionUtils.isEmpty(existingNames)) {
                for (String name : existingNames) {
                    if (StringUtils.isNotBlank(name)) {
                        seen.add(name.trim());
                    }
                }
            }

            // 2. 加载真题语料候选集
            List<SubjectAutoImportCorpus.QuestionItem> candidates = SubjectAutoImportCorpus.getCandidateQuestions(source, limit);
            result.setTotalFound(candidates.size());

            int importedCount = 0;
            int duplicateCount = 0;
            Map<String, Long> labelCache = new HashMap<>();

            for (SubjectAutoImportCorpus.QuestionItem item : candidates) {
                String cleanTitle = SubjectAutoImportCorpus.cleanTitle(item.getTitle());
                if (StringUtils.isBlank(cleanTitle) || seen.contains(cleanTitle)) {
                    duplicateCount++;
                    continue;
                }

                // 智能判定难度与提取摘要
                int diff = SubjectAutoImportCorpus.determineDifficulty(cleanTitle);
                String summary = SubjectAutoImportCorpus.generateSummary(item.getAnswer());

                // 获取/创建分类下对应的三级标签
                String labelCacheKey = item.getCategoryId() + "_" + item.getDefaultLabel();
                Long labelId = labelCache.get(labelCacheKey);
                if (labelId == null) {
                    labelId = findOrCreateLabel(item.getCategoryId(), item.getDefaultLabel());
                    labelCache.put(labelCacheKey, labelId);
                }

                SubjectInfoBO bo = new SubjectInfoBO();
                bo.setSubjectName(cleanTitle);
                bo.setSubjectDifficult(diff);
                bo.setSubjectType(4); // 简答题
                bo.setSubjectScore(10);
                bo.setSubjectParse(summary);
                bo.setSubjectAnswer(item.getAnswer());
                bo.setCategoryIds(Collections.singletonList(item.getCategoryId()));
                bo.setLabelIds(Collections.singletonList(labelId.intValue()));
                bo.setSettleName("刷题鸭精选");
                bo.setCreateUser("admin");

                // 持久化入库 (联动 subject_info, subject_brief, subject_mapping)
                this.add(bo);

                seen.add(cleanTitle);
                importedCount++;
                touchedCategories.add(item.getCategoryId());
                touchedLabels.add(labelId);

                if (sampleTitles.size() < 5) {
                    sampleTitles.add(cleanTitle);
                }
            }

            result.setImportedCount(importedCount);
            result.setDuplicateCount(duplicateCount);
            result.setCategoriesCount(touchedCategories.size());
            result.setLabelsCount(touchedLabels.size());
            result.setSampleTitles(sampleTitles);

            // 3. 自动同步 ES 搜索引擎
            if (importBO == null || !Boolean.FALSE.equals(importBO.getAutoSyncEs())) {
                if (importedCount > 0) {
                    try {
                        this.syncAllToEs();
                    } catch (Exception e) {
                        log.warn("SubjectInfoDomainServiceImpl.autoImportSubjects syncAllToEs warning:{}", e.getMessage());
                    }
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            result.setDurationMs(duration);
            result.setSummaryMessage(String.format("自动录取完成：成功录入 %d 道去重真题，跳过 %d 道已有题目，覆盖 %d 个技能分类与 %d 个知识点标签，耗时 %d ms。",
                    importedCount, duplicateCount, touchedCategories.size(), touchedLabels.size(), duration));

            log.info("SubjectInfoDomainServiceImpl.autoImportSubjects finished: {}", result.getSummaryMessage());
            return result;
        } finally {
            IS_IMPORTING.set(false);
        }
    }

    private Long findOrCreateLabel(Integer categoryId, String labelName) {
        if (categoryId == null || StringUtils.isBlank(labelName)) {
            return 1L;
        }
        SubjectLabel query = new SubjectLabel();
        query.setCategoryId(Long.valueOf(categoryId));
        query.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        List<SubjectLabel> labelList = subjectLabelService.queryByCondition(query);
        if (!CollectionUtils.isEmpty(labelList)) {
            for (SubjectLabel label : labelList) {
                if (labelName.trim().equalsIgnoreCase(label.getLabelName())) {
                    return label.getId();
                }
            }
            return labelList.get(0).getId();
        }
        SubjectLabel newLabel = new SubjectLabel();
        newLabel.setCategoryId(Long.valueOf(categoryId));
        newLabel.setLabelName(labelName.trim());
        newLabel.setSortNum(0);
        newLabel.setIsDeleted(IsDeletedFlagEnum.UN_DELETED.getCode());
        subjectLabelService.insert(newLabel);
        return newLabel.getId();
    }
}
