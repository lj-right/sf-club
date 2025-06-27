package com.jingdiansuifeng.subject.infra.basic.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 题目DTO
 */
@Data
public class SubjectOptionBO implements Serializable {

    /**
     * 题目答案
     */
    private String subjectAnswer;


    /**
     * 题目答案集合
     */
    private List<SubjectAnswerBO> optionList;


}

