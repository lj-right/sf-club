package com.jingdiansuifeng.auth.domain.convert;

import com.jingdiansuifeng.auth.domain.entity.AuthDictTypeBO;
import com.jingdiansuifeng.auth.infra.basic.entity.AuthDictType;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 字典类型 BO 转换器
 */
@Mapper
public interface AuthDictTypeBOConverter {

    AuthDictTypeBOConverter INSTANCE = Mappers.getMapper(AuthDictTypeBOConverter.class);

    AuthDictType convertBOToEntity(AuthDictTypeBO authDictTypeBO);

    AuthDictTypeBO convertEntityToBO(AuthDictType authDictType);

    List<AuthDictTypeBO> convertEntityToBO(List<AuthDictType> authDictTypeList);

}
