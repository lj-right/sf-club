package com.jingdiansuifeng.auth.infra.basic.service.impl;

import com.jingdiansuifeng.auth.infra.basic.entity.AuthDictData;
import com.jingdiansuifeng.auth.infra.basic.mapper.AuthDictDataDao;
import com.jingdiansuifeng.auth.infra.basic.service.AuthDictDataService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 字典数据表服务实现 (AuthDictDataServiceImpl)
 */
@Service("authDictDataService")
public class AuthDictDataServiceImpl implements AuthDictDataService {

    @Resource
    private AuthDictDataDao authDictDataDao;

    @Override
    public AuthDictData queryById(Long id) {
        return this.authDictDataDao.queryById(id);
    }

    @Override
    public List<AuthDictData> queryByDictType(String dictType) {
        return this.authDictDataDao.queryByDictType(dictType);
    }

    @Override
    public AuthDictData queryByTypeAndKey(String dictType, String dictKey) {
        return this.authDictDataDao.queryByTypeAndKey(dictType, dictKey);
    }

    @Override
    public List<AuthDictData> queryAllByLimit(AuthDictData authDictData) {
        return this.authDictDataDao.queryAllByLimit(authDictData);
    }

    @Override
    public int insert(AuthDictData authDictData) {
        return this.authDictDataDao.insert(authDictData);
    }

    @Override
    public int update(AuthDictData authDictData) {
        return this.authDictDataDao.update(authDictData);
    }

    @Override
    public int updateValueByTypeAndKey(String dictType, String dictKey, String dictValue) {
        return this.authDictDataDao.updateValueByTypeAndKey(dictType, dictKey, dictValue);
    }

    @Override
    public boolean deleteById(Long id) {
        return this.authDictDataDao.deleteById(id) > 0;
    }

    @Override
    public boolean deleteByTypeAndKey(String dictType, String dictKey) {
        return this.authDictDataDao.deleteByTypeAndKey(dictType, dictKey) > 0;
    }

}
