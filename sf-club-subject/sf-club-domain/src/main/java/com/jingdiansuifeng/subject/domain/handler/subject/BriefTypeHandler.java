package com.jingdiansuifeng.subject.domain.handler.subject;

import com.google.common.base.Preconditions;
import com.jingdiansuifeng.subject.common.enums.SubjectInfoTypeEnum;
import com.jingdiansuifeng.subject.domain.convert.BriefSubjectConverter;
import com.jingdiansuifeng.subject.domain.convert.JudgeSubjectConverter;
import com.jingdiansuifeng.subject.domain.entity.SubjectBrief;
import com.jingdiansuifeng.subject.domain.entity.SubjectInfoBO;
import com.jingdiansuifeng.subject.domain.entity.SubjectJudge;
import com.jingdiansuifeng.subject.domain.service.SubjectBriefService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * 简答题的策略类
 */
@Component
public class BriefTypeHandler implements SubjectTypeHandler{

    @Resource
    private SubjectBriefService subjectBriefService;

    @Override
    public SubjectInfoTypeEnum getHandlerType() {
        return SubjectInfoTypeEnum.BRIEF;

    }

    @Override
    public void add(SubjectInfoBO subjectInfoBO) {
        //简答题的插入
        Preconditions.checkNotNull(subjectInfoBO.getId(), "题目id不能为空");
        Preconditions.checkArgument(!StringUtils.isBlank(subjectInfoBO.getSubjectName()), "题目名称不能为空");

        List<SubjectBrief> subjectBriefList = new LinkedList<>();
        subjectInfoBO.getOptionList().forEach(option ->{
            SubjectBrief subjectBrief = BriefSubjectConverter.INSTANCE.convertBoToBriefEntity(option);
            subjectBrief.setSubjectId(subjectInfoBO.getId());
            subjectBriefList.add(subjectBrief);
        });
        subjectBriefService.batchInsert(subjectBriefList);
    }
}
