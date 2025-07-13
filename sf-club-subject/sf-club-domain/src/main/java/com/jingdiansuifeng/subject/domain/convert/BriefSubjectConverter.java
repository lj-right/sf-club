package com.jingdiansuifeng.subject.domain.convert;

import com.jingdiansuifeng.subject.infra.basic.entity.SubjectAnswerBO;
import com.jingdiansuifeng.subject.infra.basic.entity.SubjectBrief;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


@Mapper
public interface BriefSubjectConverter {
    BriefSubjectConverter INSTANCE = Mappers.getMapper(BriefSubjectConverter.class);

    SubjectBrief convertBoToBriefEntity(SubjectAnswerBO subjectAnswerBO);

}
