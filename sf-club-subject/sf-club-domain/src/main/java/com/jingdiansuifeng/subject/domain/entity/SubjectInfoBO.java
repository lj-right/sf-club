package com.jingdiansuifeng.subject.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 题目DTO
 */
@Data
public class SubjectInfoBO implements Serializable {
    private static final long serialVersionUID = -92805262683575195L;
    /**
     * 主键
     */
    private Long id;
    /**
     * 题目名称
     */
    private String subjectName;
    /**
     * 题目难度
     */
    private Integer subjectDifficult;
    /**
     * 出题人名
     */
    private String settleName;
    /**
     * 题目类型 1单选 2多选 3判断 4简答
     */
    private Integer subjectType;
    /**
     * 题目分数
     */
    private Integer subjectScore;
    /**
     * 题目解析
     */
    private String subjectParse;
    /**
     * 题目答案
     */
    private String subjectAnswer;

    /**
     * 题目分类id集合
     */
    private List<Integer> categoryIds;

    /**
     * 题目标签id集合
     */
    private List<Integer> labelIds;

    /**
     * 题目答案集合
     */
    private List<SubjectAnswerBO> optionList;

}

