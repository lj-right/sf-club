package com.jingdiansuifeng.auth.application.controller;

import com.google.common.base.Preconditions;
import com.jingdiansuifeng.auth.application.convert.AuthDictDataDTOConverter;
import com.jingdiansuifeng.auth.application.convert.AuthDictTypeDTOConverter;
import com.jingdiansuifeng.auth.domain.entity.AuthDictDataBO;
import com.jingdiansuifeng.auth.domain.entity.AuthDictTypeBO;
import com.jingdiansuifeng.auth.domain.service.AuthDictDomainService;
import com.jingdiansuifeng.auth.entity.AuthDictDataDTO;
import com.jingdiansuifeng.auth.entity.AuthDictTypeDTO;
import com.jingdiansuifeng.auth.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 字典管理与动态配置控制器 (AuthDictController)
 */
@RestController
@RequestMapping("/dict/")
@Slf4j
public class AuthDictController {

    @Resource
    private AuthDictDomainService authDictDomainService;

    /**
     * 查询所有可用字典类型
     */
    @GetMapping("type/list")
    public Result<List<AuthDictTypeDTO>> queryDictTypeList() {
        try {
            List<AuthDictTypeBO> boList = authDictDomainService.queryDictTypeList();
            return Result.ok(AuthDictTypeDTOConverter.INSTANCE.convertBOToDTO(boList));
        } catch (Exception e) {
            log.error("AuthDictController.queryDictTypeList error: {}", e.getMessage(), e);
            return Result.fail("查询字典类型失败");
        }
    }

    /**
     * 根据字典类型获取数据列表 (优先从 Redis 缓存获取)
     */
    @GetMapping("data/queryByType")
    public Result<List<AuthDictDataDTO>> queryDictDataByType(@RequestParam("dictType") String dictType) {
        try {
            Preconditions.checkArgument(!StringUtils.isBlank(dictType), "字典类型不能为空");
            List<AuthDictDataBO> boList = authDictDomainService.queryDictDataByType(dictType);
            return Result.ok(AuthDictDataDTOConverter.INSTANCE.convertBOToDTO(boList));
        } catch (Exception e) {
            log.error("AuthDictController.queryDictDataByType error: {}", e.getMessage(), e);
            return Result.fail("查询字典数据失败");
        }
    }

    /**
     * 根据字典类型与键名获取单个配置值
     */
    @GetMapping("data/getValue")
    public Result<String> getDictValue(@RequestParam("dictType") String dictType,
                                       @RequestParam("dictKey") String dictKey) {
        try {
            Preconditions.checkArgument(!StringUtils.isBlank(dictType), "字典类型不能为空");
            Preconditions.checkArgument(!StringUtils.isBlank(dictKey), "字典键名不能为空");
            String value = authDictDomainService.getDictValue(dictType, dictKey);
            return Result.ok(value);
        } catch (Exception e) {
            log.error("AuthDictController.getDictValue error: {}", e.getMessage(), e);
            return Result.fail("获取字典配置值失败");
        }
    }

    /**
     * 获取指定字典类型下所有键值对 Map
     */
    @GetMapping("data/getMap")
    public Result<Map<String, String>> getDictMap(@RequestParam("dictType") String dictType) {
        try {
            Preconditions.checkArgument(!StringUtils.isBlank(dictType), "字典类型不能为空");
            return Result.ok(authDictDomainService.getDictDataMap(dictType));
        } catch (Exception e) {
            log.error("AuthDictController.getDictMap error: {}", e.getMessage(), e);
            return Result.fail("获取字典键值对失败");
        }
    }

    /**
     * 快速更新某个配置键的值（自动清除与更新缓存）
     */
    @PostMapping("data/updateValue")
    public Result<Boolean> updateDictValue(@RequestParam("dictType") String dictType,
                                           @RequestParam("dictKey") String dictKey,
                                           @RequestParam("dictValue") String dictValue) {
        try {
            Preconditions.checkArgument(!StringUtils.isBlank(dictType), "字典类型不能为空");
            Preconditions.checkArgument(!StringUtils.isBlank(dictKey), "字典键名不能为空");
            Boolean success = authDictDomainService.updateDictValue(dictType, dictKey, dictValue);
            return Result.ok(success);
        } catch (Exception e) {
            log.error("AuthDictController.updateDictValue error: {}", e.getMessage(), e);
            return Result.fail("更新字典配置值失败");
        }
    }

    /**
     * 保存或更新字典项
     */
    @PostMapping("data/saveOrUpdate")
    public Result<Boolean> saveOrUpdateDictData(@RequestBody AuthDictDataDTO authDictDataDTO) {
        try {
            Preconditions.checkNotNull(authDictDataDTO, "入参不能为空");
            Preconditions.checkArgument(!StringUtils.isBlank(authDictDataDTO.getDictType()), "字典类型不能为空");
            Preconditions.checkArgument(!StringUtils.isBlank(authDictDataDTO.getDictKey()), "字典键名不能为空");
            AuthDictDataBO bo = AuthDictDataDTOConverter.INSTANCE.convertDTOToBO(authDictDataDTO);
            return Result.ok(authDictDomainService.saveOrUpdateDictData(bo));
        } catch (Exception e) {
            log.error("AuthDictController.saveOrUpdateDictData error: {}", e.getMessage(), e);
            return Result.fail("保存字典项失败");
        }
    }

    /**
     * 删除字典数据项
     */
    @PostMapping("data/delete")
    public Result<Boolean> deleteDictData(@RequestParam("dictType") String dictType,
                                          @RequestParam("dictKey") String dictKey) {
        try {
            Preconditions.checkArgument(!StringUtils.isBlank(dictType), "字典类型不能为空");
            Preconditions.checkArgument(!StringUtils.isBlank(dictKey), "字典键名不能为空");
            return Result.ok(authDictDomainService.deleteDictData(dictType, dictKey));
        } catch (Exception e) {
            log.error("AuthDictController.deleteDictData error: {}", e.getMessage(), e);
            return Result.fail("删除字典项失败");
        }
    }

    /**
     * 手动清除/刷新指定字典类型缓存
     */
    @PostMapping("refreshCache")
    public Result<Boolean> refreshCache(@RequestParam("dictType") String dictType) {
        try {
            authDictDomainService.refreshDictCache(dictType);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("AuthDictController.refreshCache error: {}", e.getMessage(), e);
            return Result.fail("刷新字典缓存失败");
        }
    }

}
