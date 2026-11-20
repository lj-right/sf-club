package com.jingdiansuifeng.auth.domain.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 字典数据业务实体 (AuthDictDataBO)
 */
@Data
public class AuthDictDataBO implements Serializable {

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
