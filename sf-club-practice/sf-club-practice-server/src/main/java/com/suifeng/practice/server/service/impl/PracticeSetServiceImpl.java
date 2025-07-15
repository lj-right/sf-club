package com.suifeng.practice.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.suifeng.practice.api.enums.SubjectInfoTypeEnum;
import com.suifeng.practice.api.vo.SpecialPracticeLabelVO;
import com.suifeng.practice.api.vo.SpecialPractiveCategoryVO;
import com.suifeng.practice.api.vo.SpecialPractiveVO;
import com.suifeng.practice.server.dao.SubjectCategoryDao;
import com.suifeng.practice.server.dao.SubjectLabelDao;
import com.suifeng.practice.server.dao.SubjectMappingDao;
import com.suifeng.practice.server.entity.dto.CategoryDTO;
import com.suifeng.practice.server.entity.po.CategoryPO;
import com.suifeng.practice.server.entity.po.LabelCountPO;
import com.suifeng.practice.server.entity.po.PrimaryCategoryPO;
import com.suifeng.practice.server.entity.po.SubjectLabelPO;
import com.suifeng.practice.server.service.PracticeSetService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PracticeSetServiceImpl implements PracticeSetService {

    @Resource
    private SubjectCategoryDao subjectCategoryDao;

    @Resource
    private SubjectMappingDao subjectMappingDao;
    @Resource
    private SubjectLabelDao subjectLabelDao;

    @Override
    public List<SpecialPractiveVO> getSpecialPracticeContent() {
        List<SpecialPractiveVO> specialPractiveVOList = new LinkedList<>();
        List<Integer> subjectTypeList = new LinkedList<>();
        subjectTypeList.add(SubjectInfoTypeEnum.RADIO.getCode());
        subjectTypeList.add(SubjectInfoTypeEnum.MULTIPLE.getCode());
        subjectTypeList.add(SubjectInfoTypeEnum.JUDGE.getCode());

        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setSubjectTypeList(subjectTypeList);

        List<PrimaryCategoryPO> poList = subjectCategoryDao.getPrimaryCategory(categoryDTO);
        //上面有效的只有parentId
        if (CollectionUtils.isEmpty(poList)) {
            return specialPractiveVOList;
        }
        poList.forEach(primaryCategoryPO -> {
            SpecialPractiveVO specialPractiveVO = new SpecialPractiveVO();

            //拿到大类集合
            List<PrimaryCategoryPO> categoryPOList = subjectCategoryDao.selectByParentId(primaryCategoryPO.getParentId());

            categoryPOList.forEach(categoryPO -> {
                specialPractiveVO.setPrimaryCategoryId(categoryPO.getId());
                specialPractiveVO.setPrimaryCategoryName(categoryPO.getCategoryName());
                CategoryDTO categoryDTOTemp = new CategoryDTO();
                categoryDTOTemp.setCategoryType(2);
                categoryDTOTemp.setParentId(primaryCategoryPO.getParentId()+1);
                //拿到子分类
                List<CategoryPO> smallPoList = subjectCategoryDao.selectList(categoryDTOTemp);
                if (CollectionUtils.isEmpty(smallPoList)) {
                    return;
                }

                List<SpecialPractiveCategoryVO> categoryVOList = new LinkedList<>();
                smallPoList.forEach(smallPo -> {
                    List<SpecialPracticeLabelVO> labelVOList = getLabelVOList(smallPo.getId(), subjectTypeList);
                    //同理上面也只能拿到labelId
                    if (CollectionUtils.isEmpty(labelVOList)) {
                        return;
                    }
                    SpecialPractiveCategoryVO specialPractiveCategoryVO = new SpecialPractiveCategoryVO();
                    specialPractiveCategoryVO.setCategoryId(smallPo.getId());
                    specialPractiveCategoryVO.setCategoryName(smallPo.getCategoryName());

                    List<SpecialPracticeLabelVO> labelList = new LinkedList<>();
                    labelVOList.forEach(labelVO -> {
                        SpecialPracticeLabelVO specialPracticeLabelVO = new SpecialPracticeLabelVO();
                        specialPracticeLabelVO.setId(labelVO.getId());
                        specialPracticeLabelVO.setAssembleId(labelVO.getAssembleId());
                        specialPracticeLabelVO.setLabelName(labelVO.getLabelName());
                        labelList.add(specialPracticeLabelVO);
                    });
                    specialPractiveCategoryVO.setLabelList(labelList);
                    categoryVOList.add(specialPractiveCategoryVO);
                });
                specialPractiveVO.setCategoryList(categoryVOList);

            });

            specialPractiveVOList.add(specialPractiveVO);
        });
        return specialPractiveVOList;
    }

    private List<SpecialPracticeLabelVO> getLabelVOList(Long categoryId, List<Integer> subjectTypeList) {
        List<LabelCountPO> countPOList = subjectMappingDao.getLabelSubjectCount(categoryId, subjectTypeList);
        if (CollectionUtils.isEmpty(countPOList)) {
            return Collections.emptyList();
        }
        List<SpecialPracticeLabelVO> voList = new LinkedList<>();
        countPOList.forEach(countPo -> {
            SpecialPracticeLabelVO vo = new SpecialPracticeLabelVO();
            vo.setId(countPo.getLabelId());
            vo.setAssembleId(categoryId+"-"+countPo.getLabelId());
            SubjectLabelPO subjectLabelPO = subjectLabelDao.queryById(countPo.getLabelId());
            vo.setLabelName(subjectLabelPO.getLabelName());
            voList.add(vo);

        });
        return voList;
    }
}
