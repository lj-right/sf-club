package com.jingdiansuifeng.auth.infra.basic.service.impl;

import com.jingdiansuifeng.auth.infra.basic.entity.AuthDictType;
import com.jingdiansuifeng.auth.infra.basic.mapper.AuthDictTypeDao;
import com.jingdiansuifeng.auth.infra.basic.service.AuthDictTypeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 字典类型表服务实现 (AuthDictTypeServiceImpl)
 */
@Service("authDictTypeService")
public class AuthDictTypeServiceImpl implements AuthDictTypeService {

    @Resource
    private AuthDictTypeDao authDictTypeDao;

    @Override
    public AuthDictType queryById(Long id) {
        return this.authDictTypeDao.queryById(id);
    }

    @Override
    public AuthDictType queryByDictType(String dictType) {
        return this.authDictTypeDao.queryByDictType(dictType);
    }

    @Override
    public List<AuthDictType> queryAllByLimit(AuthDictType authDictType) {
        return this.authDictTypeDao.queryAllByLimit(authDictType);
    }

    @Override
    public int insert(AuthDictType authDictType) {
        return this.authDictTypeDao.insert(authDictType);
    }

    @Override
    public int update(AuthDictType authDictType) {
        return this.authDictTypeDao.update(authDictType);
    }

    @Override
    public boolean deleteById(Long id) {
        return this.authDictTypeDao.deleteById(id) > 0;
    }

}
