package com.jingdiansuifeng.subject.domain.entity;
import com.jingdiansuifeng.subject.common.entity.PageInfo;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SubjectInfoEs extends PageInfo implements Serializable {

    private Long subjectId;

    private Long docId;

    private String subjectName;

    private String subjectAnswer;

    private String createUser;

    private Long createTime;

    private Integer subjectType;

    private String keyWord;

    private BigDecimal score;

    private List<String> categoryNames;

    private List<String> labelNames;

    /**
     * MQ操作类型: ADD / UPDATE / DELETE
     */
    private String operationType;

}
