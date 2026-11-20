package com.jingdiansuifeng.auth.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 字典类型 DTO (AuthDictTypeDTO)
 */
@Data
public class AuthDictTypeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String dictName;

    private String dictType;

    private Integer status;

    private String remark;

}
