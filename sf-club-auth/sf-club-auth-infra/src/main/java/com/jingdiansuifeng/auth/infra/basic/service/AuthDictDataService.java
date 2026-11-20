package com.jingdiansuifeng.auth.infra.basic.service;

import com.jingdiansuifeng.auth.infra.basic.entity.AuthDictData;

import java.util.List;

/**
 * 字典数据表服务接口 (AuthDictDataService)
 */
public interface AuthDictDataService {

    AuthDictData queryById(Long id);

    List<AuthDictData> queryByDictType(String dictType);

    AuthDictData queryByTypeAndKey(String dictType, String dictKey);

    List<AuthDictData> queryAllByLimit(AuthDictData authDictData);

    int insert(AuthDictData authDictData);

    int update(AuthDictData authDictData);

    int updateValueByTypeAndKey(String dictType, String dictKey, String dictValue);

    boolean deleteById(Long id);

    boolean deleteByTypeAndKey(String dictType, String dictKey);

}
