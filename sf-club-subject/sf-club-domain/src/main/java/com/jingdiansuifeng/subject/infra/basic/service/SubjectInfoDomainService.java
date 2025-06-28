package com.jingdiansuifeng.subject.infra.basic.service;

import com.jingdiansuifeng.subject.common.entity.PageResult;
import com.jingdiansuifeng.subject.infra.basic.entity.SubjectInfoBO;
import com.jingdiansuifeng.subject.infra.basic.entity.SubjectInfoEs;


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
}
