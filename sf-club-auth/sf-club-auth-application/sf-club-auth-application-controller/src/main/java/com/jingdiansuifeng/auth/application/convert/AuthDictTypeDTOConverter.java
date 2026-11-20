package com.jingdiansuifeng.auth.application.convert;

import com.jingdiansuifeng.auth.domain.entity.AuthDictTypeBO;
import com.jingdiansuifeng.auth.entity.AuthDictTypeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 字典类型 DTO 转换器
 */
@Mapper
public interface AuthDictTypeDTOConverter {

    AuthDictTypeDTOConverter INSTANCE = Mappers.getMapper(AuthDictTypeDTOConverter.class);

    AuthDictTypeBO convertDTOToBO(AuthDictTypeDTO authDictTypeDTO);

    AuthDictTypeDTO convertBOToDTO(AuthDictTypeBO authDictTypeBO);

    List<AuthDictTypeDTO> convertBOToDTO(List<AuthDictTypeBO> authDictTypeBOList);

}
