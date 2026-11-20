package com.jingdiansuifeng.auth.infra.basic.mapper;

import com.jingdiansuifeng.auth.infra.basic.entity.AuthDictData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典数据表数据库访问层 (AuthDictDataDao)
 */
public interface AuthDictDataDao {

    AuthDictData queryById(Long id);

    List<AuthDictData> queryByDictType(@Param("dictType") String dictType);

    AuthDictData queryByTypeAndKey(@Param("dictType") String dictType, @Param("dictKey") String dictKey);

    List<AuthDictData> queryAllByLimit(AuthDictData authDictData);

    int insert(AuthDictData authDictData);

    int update(AuthDictData authDictData);

    int updateValueByTypeAndKey(@Param("dictType") String dictType, @Param("dictKey") String dictKey, @Param("dictValue") String dictValue);

    int deleteById(Long id);

    int deleteByTypeAndKey(@Param("dictType") String dictType, @Param("dictKey") String dictKey);

}
