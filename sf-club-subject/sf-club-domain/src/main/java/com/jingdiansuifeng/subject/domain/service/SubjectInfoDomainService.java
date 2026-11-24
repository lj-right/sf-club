package com.jingdiansuifeng.subject.domain.service;

import com.jingdiansuifeng.subject.common.entity.PageResult;
import com.jingdiansuifeng.subject.domain.entity.SubjectInfoEs;
import com.jingdiansuifeng.subject.domain.entity.SubjectInfoBO;
import com.jingdiansuifeng.subject.domain.entity.SubjectAutoImportBO;
import com.jingdiansuifeng.subject.domain.entity.SubjectAutoImportResultBO;

import java.util.List;


/**
 * 题目领域服务
 */
public interface SubjectInfoDomainService {


    /**
     * 新增题目
     * @param subjectInfoBO
     */
    void add(SubjectInfoBO subjectInfoBO);

    /**
     * 分页查询题目
     * @param subjectInfoBO
     * @return
     */
    PageResult<SubjectInfoBO> getSubjectPage(SubjectInfoBO subjectInfoBO);

    /**
     * 查询题目信息
     * @param subjectInfoBO
     * @return
     */
    SubjectInfoBO querySubjectInfo(SubjectInfoBO subjectInfoBO);

    /**
     * 全文检索
     * @param subjectInfoBO
     * @return
     */
    PageResult<SubjectInfoEs> getSubjectPageBySearch(SubjectInfoBO subjectInfoBO);

    List<SubjectInfoBO> getContributeList();

    /**
     * 初始化/创建题目ES索引（配置中文IK分词器）
     */
    Boolean initSubjectIndex();

    /**
     * 全量同步 MySQL 题目数据至 ES 检索索引
     */
    Boolean syncAllToEs();

    /**
     * 通过 MQ 消息异步同步题目数据至 ES 检索索引
     * @param subjectInfoEs
     */
    void syncEsByMsg(SubjectInfoEs subjectInfoEs);

    /**
     * 自动录取题目 (仅管理员操作)
     * @param importBO
     * @return
     */
    SubjectAutoImportResultBO autoImportSubjects(SubjectAutoImportBO importBO);
}
