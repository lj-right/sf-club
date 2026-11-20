package com.jingdiansuifeng.subject.domain.service;

import com.jingdiansuifeng.subject.common.entity.PageResult;
import com.jingdiansuifeng.subject.domain.entity.SubjectInfoEs;

public interface SubjectEsService {

    Boolean createIndex();

    Boolean insert(SubjectInfoEs subjectInfoEs);

    Boolean delete(Long subjectId);

    Boolean batchInsert(java.util.List<SubjectInfoEs> subjectInfoEsList);

    PageResult<SubjectInfoEs> querySubjectList(SubjectInfoEs subjectInfoEs);
}
