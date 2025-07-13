package com.jingdiansuifeng.subject.domain.handler;

import com.jingdiansuifeng.subject.common.enums.SubjectInfoTypeEnum;
import com.jingdiansuifeng.subject.infra.basic.entity.SubjectInfoBO;
import com.jingdiansuifeng.subject.infra.basic.entity.SubjectOptionBO;

public interface SubjectTypeHandler {

    /**
     * 枚举身份的识别
     * @return
     */
    SubjectInfoTypeEnum getHandlerType();

    /**
     * 实际的题目的插入
     * @param subjectInfoBO
     */
    void add(SubjectInfoBO subjectInfoBO);

    /**
     * 查询
     * @param subjectId
     */
    SubjectOptionBO query(int subjectId);
}
