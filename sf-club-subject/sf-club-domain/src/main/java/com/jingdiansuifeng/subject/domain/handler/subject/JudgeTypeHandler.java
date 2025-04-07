package com.jingdiansuifeng.subject.domain.handler.subject;

import com.google.common.base.Preconditions;
import com.jingdiansuifeng.subject.common.enums.SubjectInfoTypeEnum;
import com.jingdiansuifeng.subject.domain.convert.JudgeSubjectConverter;
import com.jingdiansuifeng.subject.domain.convert.MultipleSubjectConverter;
import com.jingdiansuifeng.subject.domain.entity.SubjectInfoBO;
import com.jingdiansuifeng.subject.domain.entity.SubjectJudge;
import com.jingdiansuifeng.subject.domain.entity.SubjectMultiple;
import com.jingdiansuifeng.subject.domain.service.SubjectJudgeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * 判断题的策略类
 */
@Component
public class JudgeTypeHandler implements SubjectTypeHandler{

    @Resource
    private SubjectJudgeService subjectJudgeService;

    @Override
    public SubjectInfoTypeEnum getHandlerType() {
        return SubjectInfoTypeEnum.JUDGE;

    }

    @Override
    public void add(SubjectInfoBO subjectInfoBO) {
        //判读题的插入
        Preconditions.checkNotNull(subjectInfoBO.getId(), "题目id不能为空");
        Preconditions.checkArgument(!StringUtils.isBlank(subjectInfoBO.getSubjectName()), "题目名称不能为空");

        List<SubjectJudge> subjectJudgeList = new LinkedList<>();
        subjectInfoBO.getOptionList().forEach(option ->{
            SubjectJudge subjectJudge = JudgeSubjectConverter.INSTANCE.convertBoToJudgeEntity(option);
            subjectJudge.setSubjectId(subjectInfoBO.getId());
            subjectJudgeList.add(subjectJudge);
        });
        subjectJudgeService.batchInsert(subjectJudgeList);
    }
}
