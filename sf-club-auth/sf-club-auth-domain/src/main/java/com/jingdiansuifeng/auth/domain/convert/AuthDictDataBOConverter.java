package com.jingdiansuifeng.auth.domain.convert;

import com.jingdiansuifeng.auth.domain.entity.AuthDictDataBO;
import com.jingdiansuifeng.auth.infra.basic.entity.AuthDictData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 字典数据 BO 转换器
 */
@Mapper
public interface AuthDictDataBOConverter {

    AuthDictDataBOConverter INSTANCE = Mappers.getMapper(AuthDictDataBOConverter.class);

    AuthDictData convertBOToEntity(AuthDictDataBO authDictDataBO);

    AuthDictDataBO convertEntityToBO(AuthDictData authDictData);

    List<AuthDictDataBO> convertEntityToBO(List<AuthDictData> authDictDataList);

}
