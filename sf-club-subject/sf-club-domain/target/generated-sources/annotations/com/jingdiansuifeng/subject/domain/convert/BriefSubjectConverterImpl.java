package com.jingdiansuifeng.subject.domain.convert;

import com.jingdiansuifeng.subject.domain.entity.SubjectAnswerBO;
import com.jingdiansuifeng.subject.domain.entity.SubjectBrief;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-13T12:52:48+0800",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 1.8.0_144 (Oracle Corporation)"
)
public class BriefSubjectConverterImpl implements BriefSubjectConverter {

    @Override
    public SubjectBrief convertBoToBriefEntity(SubjectAnswerBO subjectAnswerBO) {
        if ( subjectAnswerBO == null ) {
            return null;
        }

        SubjectBrief subjectBrief = new SubjectBrief();

        return subjectBrief;
    }
}
