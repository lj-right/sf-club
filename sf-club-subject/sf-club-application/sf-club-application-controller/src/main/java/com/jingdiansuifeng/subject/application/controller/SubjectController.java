package com.jingdiansuifeng.subject.application.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.jingdiansuifeng.subject.application.convert.SubjectAnswerDTOConverter;
import com.jingdiansuifeng.subject.application.convert.SubjectInfoDTOConverter;
import com.jingdiansuifeng.subject.application.dto.SubjectAutoImportReqDTO;
import com.jingdiansuifeng.subject.application.dto.SubjectAutoImportRespDTO;
import com.jingdiansuifeng.subject.application.dto.SubjectInfoDTO;
import com.jingdiansuifeng.subject.common.entity.PageResult;
import com.jingdiansuifeng.subject.common.entity.Result;
import com.jingdiansuifeng.subject.domain.entity.SubjectAnswerBO;
import com.jingdiansuifeng.subject.domain.entity.SubjectAutoImportBO;
import com.jingdiansuifeng.subject.domain.entity.SubjectAutoImportResultBO;
import com.jingdiansuifeng.subject.domain.entity.SubjectInfoBO;
import com.jingdiansuifeng.subject.domain.entity.SubjectInfoEs;
import com.jingdiansuifeng.subject.domain.service.SubjectInfoDomainService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


/**
 * 刷题Controller
 */
@RestController
@RequestMapping("/subject")
@Slf4j
public class SubjectController {

    @Resource
    private SubjectInfoDomainService subjectInfoDomainService;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 新增题目
     * @param subjectInfoDTO
     * @return
     */
    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody SubjectInfoDTO subjectInfoDTO) {
        try {
            if (log.isInfoEnabled()) {
                log.info("SubjectController.add.subjectInfoDTO:{}"
                        , JSON.toJSONString(subjectInfoDTO));
            }

            Preconditions.checkArgument(!StringUtils.isBlank(subjectInfoDTO.getSubjectName()), "题目名称不能为空");
            Preconditions.checkNotNull(subjectInfoDTO.getSubjectDifficult(), "题目难度不能为空");
            Preconditions.checkNotNull(subjectInfoDTO.getSubjectType(), "题目类型不能为空");
            Preconditions.checkNotNull(subjectInfoDTO.getSubjectScore(), "题目分数不能为空");

            Preconditions.checkArgument(!CollectionUtils.isEmpty(subjectInfoDTO.getCategoryIds())
                    ,"分类id集合不能为空");
            Preconditions.checkArgument(!CollectionUtils.isEmpty(subjectInfoDTO.getLabelIds())
                    ,"标签id集合不能为空");

            SubjectInfoBO subjectInfoBO = SubjectInfoDTOConverter.INSTANCE
                    .convertDtoToInfoBo(subjectInfoDTO);
            List<SubjectAnswerBO> subjectAnswerBOS = SubjectAnswerDTOConverter.INSTANCE
                    .convertDtoListToAnswerBoList(subjectInfoDTO.getOptionList());
            subjectInfoBO.setOptionList(subjectAnswerBOS);
            subjectInfoDomainService.add(subjectInfoBO);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("SubjectController.add.error:{}", e.getMessage(), e);
            return Result.fail("新增题目失败");
        }
    }

    /**
     * 查询题目列表
     * @param subjectInfoDTO
     * @return
     */
    @PostMapping("/getSubjectPage")
    public Result<PageResult<SubjectInfoDTO>> getSubjectPage(@RequestBody SubjectInfoDTO subjectInfoDTO) {
        try {
            if (log.isInfoEnabled()) {
                log.info("SubjectController.getSubjectPage.subjectInfoDTO:{}"
                        , JSON.toJSONString(subjectInfoDTO));
            }

            if (subjectInfoDTO.getPageNo() == null || subjectInfoDTO.getPageNo() < 1) {
                subjectInfoDTO.setPageNo(1);
            }
            if (subjectInfoDTO.getPageSize() == null || subjectInfoDTO.getPageSize() < 1) {
                subjectInfoDTO.setPageSize(10);
            }

            SubjectInfoBO subjectInfoBO = SubjectInfoDTOConverter.INSTANCE
                    .convertDtoToInfoBo(subjectInfoDTO);
            PageResult<SubjectInfoBO> boPageResult = subjectInfoDomainService.getSubjectPage(subjectInfoBO);

            PageResult<SubjectInfoDTO> dtoPageResult = SubjectInfoDTOConverter.INSTANCE.convertBoPageToInfoDTOPage(boPageResult);
            return Result.ok(dtoPageResult);
        } catch (Exception e) {
            log.error("SubjectController.getSubjectPage.error:{}", e.getMessage(), e);
            return Result.fail("查询题目列表失败");
        }
    }

    /**
     * 查询题目详情
     * @param subjectInfoDTO
     * @return
     */
    @PostMapping("/querySubjectInfo")
    public Result<SubjectInfoDTO> querySubjectInfo(@RequestBody SubjectInfoDTO subjectInfoDTO) {
        try {
            if (log.isInfoEnabled()) {
                log.info("SubjectController.querySubjectInfo.subjectInfoDTO:{}"
                        , JSON.toJSONString(subjectInfoDTO));
            }
            Preconditions.checkNotNull(subjectInfoDTO.getId(), "题目id不能为空");


            SubjectInfoBO subjectInfoBO = SubjectInfoDTOConverter.INSTANCE
                    .convertDtoToInfoBo(subjectInfoDTO);
            SubjectInfoBO boResult = subjectInfoDomainService.querySubjectInfo(subjectInfoBO);
            SubjectInfoDTO dto = SubjectInfoDTOConverter.INSTANCE
                    .convertBoToInfoDTO(boResult);
            return Result.ok(dto);
        } catch (Exception e) {
            log.error("SubjectController.querySubjectInfo.error:{}", e.getMessage(), e);
            return Result.fail("查询题目详情失败");
        }
    }

    /**
     * 全文检索
     */
    @PostMapping("/getSubjectPageBySearch")
    public Result<PageResult<SubjectInfoEs>> getSubjectPageBySearch(@RequestBody SubjectInfoDTO subjectInfoDTO) {
        try {
            if (log.isInfoEnabled()) {
                log.info("SubjectController.getSubjectPageBySearch.subjectInfoDTO:{}",
                        JSON.toJSONString(subjectInfoDTO));
            }

            Preconditions.checkArgument(StringUtils.isNotBlank(subjectInfoDTO.getKeyWord()), "搜索关键字不能为空");


            SubjectInfoBO subjectInfoBO = SubjectInfoDTOConverter.INSTANCE
                    .convertDtoToInfoBo(subjectInfoDTO);
            subjectInfoBO.setPageNo(subjectInfoDTO.getPageNo() != null && subjectInfoDTO.getPageNo() > 0 ? subjectInfoDTO.getPageNo() : 1);
            subjectInfoBO.setPageSize(subjectInfoDTO.getPageSize() != null && subjectInfoDTO.getPageSize() > 0 ? subjectInfoDTO.getPageSize() : 10);
            subjectInfoBO.setKeyWord(subjectInfoDTO.getKeyWord().trim());
            PageResult<SubjectInfoEs> boPageResult = subjectInfoDomainService.getSubjectPageBySearch(subjectInfoBO);
            return Result.ok(boPageResult);
        } catch (Exception e) {
            log.error("SubjectController.getSubjectPageBySearch.error:{}", e.getMessage(), e);
            return Result.fail("全文检索失败");
        }
    }

    /**
     * 获取题目的贡献榜
     */
    @PostMapping("/getContributeList")
    public Result<List<SubjectInfoDTO>> getContributeList() {
        try {
            List<SubjectInfoBO> boList =subjectInfoDomainService.getContributeList();
            List<SubjectInfoDTO> dtoList = SubjectInfoDTOConverter
                    .INSTANCE.convertBoListToInfoDTOList(boList);
            return Result.ok(dtoList);
        } catch (Exception e) {
            log.error("SubjectController.getContributeList.error:{}", e.getMessage(), e);
            return Result.fail("获取题目的贡献榜失败");
        }
    }

    /**
     * 测试mq发送
     */
    @PostMapping("/pushMessage")
    public Result<Boolean> pushMessage(@Param("id") int id) {
        try {
            rocketMQTemplate.convertAndSend("first-topic","随风向您问好！");
            return Result.ok(true);
        } catch (Exception e) {
            log.error("SubjectController.pushMessage.error:{}", e.getMessage(), e);
            return Result.fail("mq推送消息失败");
        }
    }

    /**
     * 初始化/创建题目ES索引（配置中文IK分词器）
     */
    @PostMapping("/initSubjectIndex")
    public Result<Boolean> initSubjectIndex() {
        try {
            return Result.ok(subjectInfoDomainService.initSubjectIndex());
        } catch (Exception e) {
            log.error("SubjectController.initSubjectIndex.error:{}", e.getMessage(), e);
            return Result.fail("初始化ES索引失败");
        }
    }

    /**
     * 全量同步题目至ES检索索引
     */
    @PostMapping("/syncAllToEs")
    public Result<Boolean> syncAllToEs() {
        try {
            return Result.ok(subjectInfoDomainService.syncAllToEs());
        } catch (Exception e) {
            log.error("SubjectController.syncAllToEs.error:{}", e.getMessage(), e);
            return Result.fail("全量同步ES失败");
        }
    }

    /**
     * 自动录取题目 (仅系统管理员操作)
     */
    @PostMapping("/admin/autoImport")
    public Result<SubjectAutoImportRespDTO> autoImport(@RequestBody(required = false) SubjectAutoImportReqDTO reqDTO) {
        try {
            if (log.isInfoEnabled()) {
                log.info("SubjectController.autoImport.reqDTO:{}", JSON.toJSONString(reqDTO));
            }
            SubjectAutoImportBO importBO = new SubjectAutoImportBO();
            if (reqDTO != null) {
                importBO.setSource(reqDTO.getSource());
                importBO.setLimit(reqDTO.getLimit());
                importBO.setAutoSyncEs(reqDTO.getAutoSyncEs());
            }
            SubjectAutoImportResultBO resultBO = subjectInfoDomainService.autoImportSubjects(importBO);

            SubjectAutoImportRespDTO respDTO = new SubjectAutoImportRespDTO();
            respDTO.setTotalFound(resultBO.getTotalFound());
            respDTO.setImportedCount(resultBO.getImportedCount());
            respDTO.setDuplicateCount(resultBO.getDuplicateCount());
            respDTO.setCategoriesCount(resultBO.getCategoriesCount());
            respDTO.setLabelsCount(resultBO.getLabelsCount());
            respDTO.setDurationMs(resultBO.getDurationMs());
            respDTO.setSummaryMessage(resultBO.getSummaryMessage());
            respDTO.setSampleTitles(resultBO.getSampleTitles());

            return Result.ok(respDTO);
        } catch (SecurityException se) {
            log.warn("SubjectController.autoImport forbidden: {}", se.getMessage());
            return Result.fail(se.getMessage());
        } catch (Exception e) {
            log.error("SubjectController.autoImport.error:{}", e.getMessage(), e);
            return Result.fail("自动录取题目失败: " + e.getMessage());
        }
    }

}
