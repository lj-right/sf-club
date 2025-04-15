package com.jingdiansuifeng.subject.application.convert;

import com.jingdiansuifeng.subject.application.dto.SubjectAnswerDTO;
import com.jingdiansuifeng.subject.domain.entity.SubjectAnswerBO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-15T19:18:10+0800",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 1.8.0_144 (Oracle Corporation)"
)
public class SubjectAnswerDTOConverterImpl implements SubjectAnswerDTOConverter {

    @Override
    public SubjectAnswerBO convertDtoToAnswerBo(SubjectAnswerDTO answerDTO) {
        if ( answerDTO == null ) {
            return null;
        }

        SubjectAnswerBO subjectAnswerBO = new SubjectAnswerBO();

        subjectAnswerBO.setOptionType( answerDTO.getOptionType() );
        subjectAnswerBO.setOptionContent( answerDTO.getOptionContent() );
        subjectAnswerBO.setIsCorrect( answerDTO.getIsCorrect() );

        return subjectAnswerBO;
    }

    @Override
    public List<SubjectAnswerBO> convertDtoListToAnswerBoList(List<SubjectAnswerDTO> answerDTOList) {
        if ( answerDTOList == null ) {
            return null;
        }

        List<SubjectAnswerBO> list = new ArrayList<SubjectAnswerBO>( answerDTOList.size() );
        for ( SubjectAnswerDTO subjectAnswerDTO : answerDTOList ) {
            list.add( convertDtoToAnswerBo( subjectAnswerDTO ) );
        }

        return list;
    }

    @Override
    public List<SubjectAnswerDTO> convertBoListToAnswerDTOList(List<SubjectAnswerBO> subjectAnswerBOList) {
        if ( subjectAnswerBOList == null ) {
            return null;
        }

        List<SubjectAnswerDTO> list = new ArrayList<SubjectAnswerDTO>( subjectAnswerBOList.size() );
        for ( SubjectAnswerBO subjectAnswerBO : subjectAnswerBOList ) {
            list.add( subjectAnswerBOToSubjectAnswerDTO( subjectAnswerBO ) );
        }

        return list;
    }

    protected SubjectAnswerDTO subjectAnswerBOToSubjectAnswerDTO(SubjectAnswerBO subjectAnswerBO) {
        if ( subjectAnswerBO == null ) {
            return null;
        }

        SubjectAnswerDTO subjectAnswerDTO = new SubjectAnswerDTO();

        subjectAnswerDTO.setOptionType( subjectAnswerBO.getOptionType() );
        subjectAnswerDTO.setOptionContent( subjectAnswerBO.getOptionContent() );
        subjectAnswerDTO.setIsCorrect( subjectAnswerBO.getIsCorrect() );

        return subjectAnswerDTO;
    }
}
