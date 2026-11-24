package com.jingdiansuifeng.subject.application.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 自动录取题目请求DTO (仅管理员可用)
 */
@Data
public class SubjectAutoImportReqDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 题源范围: ALL(全栈综合), BACKEND(后端开发), FRONTEND(前端开发), AI(大模型与AI), ALGORITHM(算法与数据结构)
     */
    private String source;

    /**
     * 录入批次上限 (默认 50)
     */
    private Integer limit;

    /**
     * 是否在导入后自动全量触发ES搜索引擎同步 (默认 true)
     */
    private Boolean autoSyncEs = true;
}
