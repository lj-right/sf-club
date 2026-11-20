package com.jingdiansuifeng.auth.infra.basic.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 字典数据表实体 (AuthDictData)
 */
@Data
public class AuthDictData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String dictType;

    private String dictKey;

    private String dictLabel;

    private String dictValue;

    private Integer dictSort;

    private Integer status;

    private String remark;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

    private Integer isDeleted;

}
