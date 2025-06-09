package com.jingdiansuifeng.subject.domain.convert;

import com.jingdiansuifeng.subject.domain.entity.SubjectAnswerBO;
import com.jingdiansuifeng.subject.domain.entity.SubjectMultiple;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-09T15:44:52+0800",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 1.8.0_144 (Oracle Corporation)"
)
public class MultipleSubjectConverterImpl implements MultipleSubjectConverter {

    @Override
    public SubjectMultiple convertBoToMultipleEntity(SubjectAnswerBO subjectAnswerBO) {
        if ( subjectAnswerBO == null ) {
            return null;
        }

        SubjectMultiple subjectMultiple = new SubjectMultiple();

        subjectMultiple.setOptionType( subjectAnswerBO.getOptionType() );
        subjectMultiple.setOptionContent( subjectAnswerBO.getOptionContent() );
        subjectMultiple.setIsCorrect( subjectAnswerBO.getIsCorrect() );

        return subjectMultiple;
    }

    @Override
    public List<SubjectAnswerBO> convertMultipleListToAnswerBo(List<SubjectMultiple> subjectMultipleList) {
        if ( subjectMultipleList == null ) {
            return null;
        }

        List<SubjectAnswerBO> list = new ArrayList<SubjectAnswerBO>( subjectMultipleList.size() );
        for ( SubjectMultiple subjectMultiple : subjectMultipleList ) {
            list.add( subjectMultipleToSubjectAnswerBO( subjectMultiple ) );
        }

        return list;
    }

    protected SubjectAnswerBO subjectMultipleToSubjectAnswerBO(SubjectMultiple subjectMultiple) {
        if ( subjectMultiple == null ) {
            return null;
        }

        SubjectAnswerBO subjectAnswerBO = new SubjectAnswerBO();

        subjectAnswerBO.setOptionType( subjectMultiple.getOptionType() );
        subjectAnswerBO.setOptionContent( subjectMultiple.getOptionContent() );
        subjectAnswerBO.setIsCorrect( subjectMultiple.getIsCorrect() );

        return subjectAnswerBO;
    }
}
