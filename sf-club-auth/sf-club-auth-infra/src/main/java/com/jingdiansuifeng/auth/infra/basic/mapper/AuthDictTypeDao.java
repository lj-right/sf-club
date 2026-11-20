package com.jingdiansuifeng.auth.infra.basic.mapper;

import com.jingdiansuifeng.auth.infra.basic.entity.AuthDictType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 字典类型表数据库访问层 (AuthDictTypeDao)
 */
public interface AuthDictTypeDao {

    AuthDictType queryById(Long id);

    AuthDictType queryByDictType(@Param("dictType") String dictType);

    List<AuthDictType> queryAllByLimit(AuthDictType authDictType);

    int insert(AuthDictType authDictType);

    int update(AuthDictType authDictType);

    int deleteById(Long id);

}
