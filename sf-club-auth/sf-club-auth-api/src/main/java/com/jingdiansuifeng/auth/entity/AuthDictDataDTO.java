package com.jingdiansuifeng.auth.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 字典数据 DTO (AuthDictDataDTO)
 */
@Data
public class AuthDictDataDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String dictType;

    private String dictKey;

    private String dictLabel;

    private String dictValue;

    private Integer dictSort;

    private Integer status;

    private String remark;

}
