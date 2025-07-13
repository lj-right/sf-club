package com.jingdiansuifeng.subject.domain.convert;

import com.jingdiansuifeng.subject.infra.basic.entity.SubjectCategory;
import com.jingdiansuifeng.subject.infra.basic.entity.SubjectCategoryBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SubjectCategoryConverter {
    SubjectCategoryConverter INSTANCE = Mappers.getMapper(SubjectCategoryConverter.class);

    SubjectCategory convertBoToCategory(SubjectCategoryBO subjectCategoryBO);

    List<SubjectCategoryBO> convertCategoryListToBoList(List<SubjectCategory> categoryList);
}
