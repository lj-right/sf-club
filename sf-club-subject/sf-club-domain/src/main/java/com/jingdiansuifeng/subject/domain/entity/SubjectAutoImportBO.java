package com.jingdiansuifeng.subject.domain.entity;

import lombok.Data;
import java.io.Serializable;

/**
 * 自动录取题目业务入参BO
 */
@Data
public class SubjectAutoImportBO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 题源范围: ALL, BACKEND, FRONTEND, AI, ALGORITHM
     */
    private String source;

    /**
     * 数量上限
     */
    private Integer limit;

    /**
     * 是否在导入后触发全量ES同步
     */
    private Boolean autoSyncEs;
}
