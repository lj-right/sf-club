package com.jingdiansuifeng.subject.application.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 自动录取题目响应DTO
 */
@Data
public class SubjectAutoImportRespDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 发现候选题目总数
     */
    private Integer totalFound;

    /**
     * 本次成功录取并持久化入库的题目数
     */
    private Integer importedCount;

    /**
     * 数据库已有、自动去重跳过的题目数
     */
    private Integer duplicateCount;

    /**
     * 覆盖二级技能分类数
     */
    private Integer categoriesCount;

    /**
     * 覆盖三级知识点标签数
     */
    private Integer labelsCount;

    /**
     * 任务执行耗时 (毫秒)
     */
    private Long durationMs;

    /**
     * 执行概要结果描述
     */
    private String summaryMessage;

    /**
     * 本次录入的部分题目标题样本预览
     */
    private List<String> sampleTitles;
}
