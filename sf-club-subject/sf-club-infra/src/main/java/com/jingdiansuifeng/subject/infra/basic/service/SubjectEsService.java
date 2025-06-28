package com.jingdiansuifeng.subject.infra.basic.service;

import com.jingdiansuifeng.auth.common.entity.PageResult;
import com.jingdiansuifeng.subject.infra.basic.entity.SubjectInfoEs;

public interface SubjectEsService {

    Boolean insert(SubjectInfoEs subjectInfoEs);

    PageResult<SubjectInfoEs> querySubjectList(SubjectInfoEs subjectInfoEs);
}
