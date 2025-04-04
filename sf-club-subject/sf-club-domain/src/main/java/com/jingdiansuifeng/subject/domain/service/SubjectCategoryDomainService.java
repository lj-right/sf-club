package com.jingdiansuifeng.subject.domain.service;

import com.jingdiansuifeng.subject.domain.entity.SubjectCategory;
import com.jingdiansuifeng.subject.domain.entity.SubjectCategoryBO;

import java.util.List;

public interface SubjectCategoryDomainService {



    void add(SubjectCategoryBO subjectCategoryBO);

    /**
     * 查询大分类
     * @return
     */
    List<SubjectCategoryBO> queryCategory(SubjectCategoryBO subjectCategoryBO);
}
