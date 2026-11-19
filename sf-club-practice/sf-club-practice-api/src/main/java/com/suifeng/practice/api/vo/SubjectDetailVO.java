package com.suifeng.practice.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SubjectDetailVO implements Serializable {

    /**
     * 正确答案
     */
    private List<Integer> correctAnswer;

    /**
     * 答题者答案（数字选项）
     */
    private List<Integer> respondAnswer;

    /**
     * 答题者提交的原始内容（支持简答题文字内容及客观题原字符串）
     */
    private String answerContent;

    /**
     * 题目解析
     */
    private String subjectParse;

    /**
     * 答案详情（选项列表）
     */
    private List<PracticeSubjectOptionVO> optionList;

    /**
     * 涉及的标签
     */
    private List<String> labelNames;

    /**
     * 题目名称
     */
    private String subjectName;

}
