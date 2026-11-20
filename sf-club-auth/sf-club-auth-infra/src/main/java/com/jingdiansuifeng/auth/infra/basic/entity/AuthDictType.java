package com.jingdiansuifeng.auth.infra.basic.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 字典类型表实体 (AuthDictType)
 */
@Data
public class AuthDictType implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String dictName;

    private String dictType;

    private Integer status;

    private String remark;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

    private Integer isDeleted;

}
