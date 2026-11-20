package com.jingdiansuifeng.auth.domain.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.jingdiansuifeng.auth.domain.convert.AuthDictDataBOConverter;
import com.jingdiansuifeng.auth.domain.convert.AuthDictTypeBOConverter;
import com.jingdiansuifeng.auth.domain.entity.AuthDictDataBO;
import com.jingdiansuifeng.auth.domain.entity.AuthDictTypeBO;
import com.jingdiansuifeng.auth.domain.redis.RedisUtil;
import com.jingdiansuifeng.auth.domain.service.AuthDictDomainService;
import com.jingdiansuifeng.auth.infra.basic.entity.AuthDictData;
import com.jingdiansuifeng.auth.infra.basic.entity.AuthDictType;
import com.jingdiansuifeng.auth.infra.basic.service.AuthDictDataService;
import com.jingdiansuifeng.auth.infra.basic.service.AuthDictTypeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 字典领域服务实现 (AuthDictDomainServiceImpl)
 */
@Service("authDictDomainService")
@Slf4j
public class AuthDictDomainServiceImpl implements AuthDictDomainService {

    @Resource
    private AuthDictTypeService authDictTypeService;

    @Resource
    private AuthDictDataService authDictDataService;

    @Resource
    private RedisUtil redisUtil;

    private static final String DICT_LIST_PREFIX = "dict.list";
    private static final String DICT_VAL_PREFIX = "dict.val";

    @Override
    public List<AuthDictTypeBO> queryDictTypeList() {
        AuthDictType condition = new AuthDictType();
        condition.setStatus(0);
        List<AuthDictType> list = authDictTypeService.queryAllByLimit(condition);
        return AuthDictTypeBOConverter.INSTANCE.convertEntityToBO(list);
    }

    @Override
    public List<AuthDictDataBO> queryDictDataByType(String dictType) {
        if (StringUtils.isBlank(dictType)) {
            return Collections.emptyList();
        }
        String cacheKey = redisUtil.buildKey(DICT_LIST_PREFIX, dictType);
        try {
            String cacheVal = redisUtil.get(cacheKey);
            if (StringUtils.isNotBlank(cacheVal)) {
                return JSON.parseObject(cacheVal, new TypeReference<List<AuthDictDataBO>>() {});
            }
        } catch (Exception e) {
            log.warn("queryDictDataByType redis get error: {}", e.getMessage());
        }

        List<AuthDictData> dataList = authDictDataService.queryByDictType(dictType);
        List<AuthDictDataBO> boList = AuthDictDataBOConverter.INSTANCE.convertEntityToBO(dataList);
        if (!CollectionUtils.isEmpty(boList)) {
            try {
                redisUtil.set(cacheKey, JSON.toJSONString(boList));
            } catch (Exception e) {
                log.warn("queryDictDataByType redis set error: {}", e.getMessage());
            }
        }
        return boList;
    }

    @Override
    public String getDictValue(String dictType, String dictKey) {
        if (StringUtils.isBlank(dictType) || StringUtils.isBlank(dictKey)) {
            return null;
        }
        String cacheKey = redisUtil.buildKey(DICT_VAL_PREFIX, dictType, dictKey);
        try {
            String cacheVal = redisUtil.get(cacheKey);
            if (cacheVal != null) {
                return cacheVal;
            }
        } catch (Exception e) {
            log.warn("getDictValue redis get error: {}", e.getMessage());
        }

        AuthDictData dictData = authDictDataService.queryByTypeAndKey(dictType, dictKey);
        if (dictData != null && dictData.getDictValue() != null) {
            String val = dictData.getDictValue();
            try {
                redisUtil.set(cacheKey, val);
            } catch (Exception e) {
                log.warn("getDictValue redis set error: {}", e.getMessage());
            }
            return val;
        }
        return null;
    }

    @Override
    public Map<String, String> getDictDataMap(String dictType) {
        List<AuthDictDataBO> list = queryDictDataByType(dictType);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (AuthDictDataBO bo : list) {
            if (StringUtils.isNotBlank(bo.getDictKey())) {
                map.put(bo.getDictKey(), bo.getDictValue() != null ? bo.getDictValue() : "");
            }
        }
        return map;
    }

    @Override
    public Boolean updateDictValue(String dictType, String dictKey, String dictValue) {
        int count = authDictDataService.updateValueByTypeAndKey(dictType, dictKey, dictValue);
        refreshDictCache(dictType);
        return count > 0;
    }

    @Override
    public Boolean saveOrUpdateDictData(AuthDictDataBO authDictDataBO) {
        if (authDictDataBO == null || StringUtils.isBlank(authDictDataBO.getDictType()) || StringUtils.isBlank(authDictDataBO.getDictKey())) {
            return false;
        }
        AuthDictData exist = authDictDataService.queryByTypeAndKey(authDictDataBO.getDictType(), authDictDataBO.getDictKey());
        int count;
        if (exist != null) {
            AuthDictData entity = AuthDictDataBOConverter.INSTANCE.convertBOToEntity(authDictDataBO);
            entity.setId(exist.getId());
            count = authDictDataService.update(entity);
        } else {
            AuthDictData entity = AuthDictDataBOConverter.INSTANCE.convertBOToEntity(authDictDataBO);
            count = authDictDataService.insert(entity);
        }
        refreshDictCache(authDictDataBO.getDictType());
        return count > 0;
    }

    @Override
    public Boolean deleteDictData(String dictType, String dictKey) {
        boolean success = authDictDataService.deleteByTypeAndKey(dictType, dictKey);
        refreshDictCache(dictType);
        return success;
    }

    @Override
    public void refreshDictCache(String dictType) {
        if (StringUtils.isBlank(dictType)) {
            return;
        }
        try {
            redisUtil.del(redisUtil.buildKey(DICT_LIST_PREFIX, dictType));
            // 清理已缓存的单值
            List<AuthDictData> list = authDictDataService.queryByDictType(dictType);
            for (AuthDictData d : list) {
                redisUtil.del(redisUtil.buildKey(DICT_VAL_PREFIX, dictType, d.getDictKey()));
            }
        } catch (Exception e) {
            log.warn("refreshDictCache error: {}", e.getMessage());
        }
    }

}
