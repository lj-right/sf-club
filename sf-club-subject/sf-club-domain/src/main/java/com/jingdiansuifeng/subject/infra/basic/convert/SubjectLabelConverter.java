package com.jingdiansuifeng.subject.infra.basic.convert;

import com.jingdiansuifeng.subject.infra.basic.entity.SubjectCategory;
import com.jingdiansuifeng.subject.infra.basic.entity.SubjectCategoryBO;
import com.jingdiansuifeng.subject.infra.basic.entity.SubjectLabel;
import com.jingdiansuifeng.subject.infra.basic.entity.SubjectLabelBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SubjectLabelConverter {
    SubjectLabelConverter INSTANCE = Mappers.getMapper(SubjectLabelConverter.class);

    SubjectLabel convertBoToLabel(SubjectLabelBO subjectLabelBO);

    List<SubjectLabelBO> convertLabelListToBoList(List<SubjectLabel> labelList);
}
