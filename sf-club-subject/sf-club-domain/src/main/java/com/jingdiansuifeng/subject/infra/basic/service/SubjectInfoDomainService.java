package com.jingdiansuifeng.subject.infra.basic.service;

import com.jingdiansuifeng.subject.common.entity.PageResult;
import com.jingdiansuifeng.subject.infra.basic.entity.SubjectInfoBO;


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

    SubjectInfoBO querySubjectInfo(SubjectInfoBO subjectInfoBO);
}
