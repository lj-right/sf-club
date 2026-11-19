package com.suifeng.practice.api.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PracticeHistoryVO implements Serializable {

    /**
     * 练习记录ID (practiceId)
     */
    private Long id;

    /**
     * 练习ID
     */
    private Long practiceId;

    /**
     * 套卷ID
     */
    private Long setId;

    /**
     * 套卷名称/练习标题
     */
    private String title;

    /**
     * 提交时间
     */
    private String practiceTime;

    /**
     * 作答耗时
     */
    private String timeSpent;

    /**
     * 正确率百分比
     */
    private BigDecimal correctRate;

    /**
     * 综合得分（0-100分制）
     */
    private Integer score;

    /**
     * 总题数
     */
    private Integer total;

    /**
     * 正确题数
     */
    private Integer correctCount;

    /**
     * 错题数
     */
    private Integer wrongCount;

    /**
     * 完成状态：1 已交卷完成，0 进行中/未完成
     */
    private Integer completeStatus;

}
