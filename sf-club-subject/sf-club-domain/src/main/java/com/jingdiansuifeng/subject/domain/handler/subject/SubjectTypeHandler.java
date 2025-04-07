package com.jingdiansuifeng.subject.domain.handler.subject;

import com.jingdiansuifeng.subject.common.enums.SubjectInfoTypeEnum;
import com.jingdiansuifeng.subject.domain.entity.SubjectInfoBO;

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
}
