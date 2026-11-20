package com.jingdiansuifeng.auth.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 字典类型业务实体 (AuthDictTypeBO)
 */
@Data
public class AuthDictTypeBO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String dictName;

    private String dictType;

    private Integer status;

    private String remark;

}
