package com.jingdiansuifeng.auth.domain.util;

import com.jingdiansuifeng.auth.domain.service.AuthDictDomainService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 字典动态配置获取助手 (DictConfigHelper)
 * 用于在后端业务代码中便捷、高性能地获取字典配置项，支持自动兜底默认值
 */
@Component("dictConfigHelper")
@Slf4j
public class DictConfigHelper {

    @Resource
    private AuthDictDomainService authDictDomainService;

    /**
     * 获取字符串配置值
     *
     * @param dictType 字典类型 (如 oauth_config)
     * @param dictKey  字典键 (如 feishu.app_id)
     * @return 配置值，若未配置返回 null
     */
    public String getValue(String dictType, String dictKey) {
        return authDictDomainService.getDictValue(dictType, dictKey);
    }

    /**
     * 获取字符串配置值（带默认值兜底）
     */
    public String getValue(String dictType, String dictKey, String defaultValue) {
        String val = authDictDomainService.getDictValue(dictType, dictKey);
        return StringUtils.isNotBlank(val) ? val.trim() : defaultValue;
    }

    /**
     * 获取整数配置值（带默认值兜底）
     */
    public Integer getInt(String dictType, String dictKey, Integer defaultValue) {
        String val = getValue(dictType, dictKey);
        if (StringUtils.isBlank(val)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            log.warn("DictConfigHelper.getInt parse error, dictType:{}, dictKey:{}, val:{}", dictType, dictKey, val);
            return defaultValue;
        }
    }

    /**
     * 获取布尔配置值（带默认值兜底）
     */
    public Boolean getBoolean(String dictType, String dictKey, Boolean defaultValue) {
        String val = getValue(dictType, dictKey);
        if (StringUtils.isBlank(val)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(val.trim());
    }

    /**
     * 获取指定字典类型下的所有键值对 Map
     */
    public Map<String, String> getDictMap(String dictType) {
        return authDictDomainService.getDictDataMap(dictType);
    }

}
