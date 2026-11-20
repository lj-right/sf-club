package com.jingdiansuifeng.auth.domain.service;

import com.jingdiansuifeng.auth.domain.entity.AuthDictDataBO;
import com.jingdiansuifeng.auth.domain.entity.AuthDictTypeBO;

import java.util.List;
import java.util.Map;

/**
 * 字典领域服务接口 (AuthDictDomainService)
 */
public interface AuthDictDomainService {

    /**
     * 查询字典类型列表
     */
    List<AuthDictTypeBO> queryDictTypeList();

    /**
     * 根据字典类型获取数据明细列表（优先从 Redis 缓存获取）
     */
    List<AuthDictDataBO> queryDictDataByType(String dictType);

    /**
     * 根据字典类型与键名获取单项配置值（优先从 Redis 缓存获取）
     */
    String getDictValue(String dictType, String dictKey);

    /**
     * 获取指定字典类型下的所有键值对 Map
     */
    Map<String, String> getDictDataMap(String dictType);

    /**
     * 快速更新指定字典项的值（自动清理/更新缓存）
     */
    Boolean updateDictValue(String dictType, String dictKey, String dictValue);

    /**
     * 新增或更新字典数据（自动清理/更新缓存）
     */
    Boolean saveOrUpdateDictData(AuthDictDataBO authDictDataBO);

    /**
     * 删除字典数据（自动清理缓存）
     */
    Boolean deleteDictData(String dictType, String dictKey);

    /**
     * 刷新/清理指定类型的字典缓存
     */
    void refreshDictCache(String dictType);

}
