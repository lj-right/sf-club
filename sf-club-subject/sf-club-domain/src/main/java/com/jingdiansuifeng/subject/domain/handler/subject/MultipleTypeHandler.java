package com.jingdiansuifeng.subject.domain.handler.subject;

import com.google.common.base.Preconditions;
import com.jingdiansuifeng.subject.common.enums.SubjectInfoTypeEnum;
import com.jingdiansuifeng.subject.domain.convert.MultipleSubjectConverter;
import com.jingdiansuifeng.subject.domain.convert.RadioSubjectConverter;
import com.jingdiansuifeng.subject.domain.entity.SubjectInfoBO;
import com.jingdiansuifeng.subject.domain.entity.SubjectMultiple;
import com.jingdiansuifeng.subject.domain.entity.SubjectRadio;
import com.jingdiansuifeng.subject.domain.service.SubjectMappingService;
import com.jingdiansuifeng.subject.domain.service.SubjectMultipleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * 多选的题目的策略类
 */
@Component
public class MultipleTypeHandler implements SubjectTypeHandler{

    @Resource
    private SubjectMultipleService subjectMultipleService;

    @Override
    public SubjectInfoTypeEnum getHandlerType() {
        return SubjectInfoTypeEnum.MULTIPLE;

    }

    @Override
    public void add(SubjectInfoBO subjectInfoBO) {
        //多选题目的插入
        Preconditions.checkNotNull(subjectInfoBO.getId(), "题目id不能为空");
        Preconditions.checkArgument(!StringUtils.isBlank(subjectInfoBO.getSubjectName()), "题目名称不能为空");

        List<SubjectMultiple> subjectMultipleList = new LinkedList<>();
        subjectInfoBO.getOptionList().forEach(option ->{
            SubjectMultiple subjectMultiple = MultipleSubjectConverter.INSTANCE.convertBoToMultipleEntity(option);
            subjectMultiple.setSubjectId(subjectInfoBO.getId());
            subjectMultipleList.add(subjectMultiple);
        });
        subjectMultipleService.batchInsert(subjectMultipleList);
    }
}
