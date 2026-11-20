package com.jingdiansuifeng.auth.infra.basic.service;

import com.jingdiansuifeng.auth.infra.basic.entity.AuthDictType;

import java.util.List;

/**
 * 字典类型表服务接口 (AuthDictTypeService)
 */
public interface AuthDictTypeService {

    AuthDictType queryById(Long id);

    AuthDictType queryByDictType(String dictType);

    List<AuthDictType> queryAllByLimit(AuthDictType authDictType);

    int insert(AuthDictType authDictType);

    int update(AuthDictType authDictType);

    boolean deleteById(Long id);

}
