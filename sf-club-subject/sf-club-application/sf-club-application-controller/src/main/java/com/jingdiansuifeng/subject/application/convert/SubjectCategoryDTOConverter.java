package com.jingdiansuifeng.subject.application.convert;

import com.jingdiansuifeng.subject.application.dto.SubjectCategoryDTO;
import com.jingdiansuifeng.subject.domain.entity.SubjectCategory;
import com.jingdiansuifeng.subject.domain.entity.SubjectCategoryBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SubjectCategoryDTOConverter {
    SubjectCategoryDTOConverter INSTANCE = Mappers.getMapper(SubjectCategoryDTOConverter.class);

    SubjectCategoryBO convertBoToCategory(SubjectCategoryDTO subjectCategoryDTO);
}
