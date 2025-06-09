package com.jingdiansuifeng.subject.domain.convert;

import com.jingdiansuifeng.subject.domain.entity.SubjectLabel;
import com.jingdiansuifeng.subject.domain.entity.SubjectLabelBO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-06-09T15:44:52+0800",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 1.8.0_144 (Oracle Corporation)"
)
public class SubjectLabelConverterImpl implements SubjectLabelConverter {

    @Override
    public SubjectLabel convertBoToLabel(SubjectLabelBO subjectLabelBO) {
        if ( subjectLabelBO == null ) {
            return null;
        }

        SubjectLabel subjectLabel = new SubjectLabel();

        subjectLabel.setId( subjectLabelBO.getId() );
        subjectLabel.setLabelName( subjectLabelBO.getLabelName() );
        subjectLabel.setCategoryId( subjectLabelBO.getCategoryId() );
        subjectLabel.setSortNum( subjectLabelBO.getSortNum() );

        return subjectLabel;
    }

    @Override
    public List<SubjectLabelBO> convertLabelListToBoList(List<SubjectLabel> labelList) {
        if ( labelList == null ) {
            return null;
        }

        List<SubjectLabelBO> list = new ArrayList<SubjectLabelBO>( labelList.size() );
        for ( SubjectLabel subjectLabel : labelList ) {
            list.add( subjectLabelToSubjectLabelBO( subjectLabel ) );
        }

        return list;
    }

    protected SubjectLabelBO subjectLabelToSubjectLabelBO(SubjectLabel subjectLabel) {
        if ( subjectLabel == null ) {
            return null;
        }

        SubjectLabelBO subjectLabelBO = new SubjectLabelBO();

        subjectLabelBO.setId( subjectLabel.getId() );
        subjectLabelBO.setCategoryId( subjectLabel.getCategoryId() );
        subjectLabelBO.setLabelName( subjectLabel.getLabelName() );
        subjectLabelBO.setSortNum( subjectLabel.getSortNum() );

        return subjectLabelBO;
    }
}
