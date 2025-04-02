package com.jingdiansuifeng.subject.domain.convert;

import com.jingdiansuifeng.subject.domain.entity.SubjectCategory;
import com.jingdiansuifeng.subject.domain.entity.SubjectCategoryBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SubjectCategoryConverter {
    SubjectCategoryConverter INSTANCE = Mappers.getMapper(SubjectCategoryConverter.class);

    SubjectCategory convertBoToCategory(SubjectCategoryBO subjectCategoryBO);
}
