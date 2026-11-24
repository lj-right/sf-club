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
     * 所属一级分类/岗位ID (如 1-后端开发)
     */
    private Long primaryCategoryId;

    /**
     * 所属一级分类/岗位名称 (如 后端开发)
     */
    private String primaryCategoryName;

    /**
     * 所属二级分类ID (技能/中间件，如 101-Java基础)
     */
    private Long categoryId;

    /**
     * 所属二级分类名 (如 Java基础)
     */
    private String categoryName;

    /**
     * 所属三级标签ID (知识点，如 1-集合框架)
     */
    private Long labelId;

    /**
     * 所属三级标签名 (如 集合框架)
     */
    private String labelName;

    /**
     * MQ操作类型: ADD / UPDATE / DELETE
     */
    private String operationType;

}
