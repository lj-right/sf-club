package com.jingdiansuifeng.auth.application.convert;

import com.jingdiansuifeng.auth.domain.entity.AuthDictDataBO;
import com.jingdiansuifeng.auth.entity.AuthDictDataDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 字典数据 DTO 转换器
 */
@Mapper
public interface AuthDictDataDTOConverter {

    AuthDictDataDTOConverter INSTANCE = Mappers.getMapper(AuthDictDataDTOConverter.class);

    AuthDictDataBO convertDTOToBO(AuthDictDataDTO authDictDataDTO);

    AuthDictDataDTO convertBOToDTO(AuthDictDataBO authDictDataBO);

    List<AuthDictDataDTO> convertBOToDTO(List<AuthDictDataBO> authDictDataBOList);

}
