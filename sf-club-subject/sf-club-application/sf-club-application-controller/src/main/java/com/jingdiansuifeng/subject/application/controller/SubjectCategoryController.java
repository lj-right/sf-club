package com.jingdiansuifeng.subject.application.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.jingdiansuifeng.subject.application.convert.SubjectCategoryDTOConverter;
import com.jingdiansuifeng.subject.application.dto.SubjectCategoryDTO;
import com.jingdiansuifeng.subject.common.entity.Result;
import com.jingdiansuifeng.subject.domain.entity.SubjectCategoryBO;
import com.jingdiansuifeng.subject.domain.service.SubjectCategoryDomainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 刷题分类Controller
 */
@RestController
@RequestMapping("/subject/category")
@Slf4j
public class SubjectCategoryController {

    @Resource
    private SubjectCategoryDomainService subjectCategoryDomainService;


    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody SubjectCategoryDTO subjectCategoryDTO) {
        try {
            if (log.isInfoEnabled()) {
                log.info("SubjectCategoryController.add.subjectCategoryDTO:{}"
                        , JSON.toJSONString(subjectCategoryDTO));
            }

            Preconditions.checkNotNull(subjectCategoryDTO.getCategoryType(), "分类类型不能为空");
            Preconditions.checkArgument(!StringUtils.isEmpty(subjectCategoryDTO.getCategoryName()), "分类名称不能为空");
            Preconditions.checkNotNull(subjectCategoryDTO.getParentId(), "分类父级id不能为空");


            SubjectCategoryBO subjectCategoryBO = SubjectCategoryDTOConverter.INSTANCE
                    .convertDtoToCategoryBo(subjectCategoryDTO);
            subjectCategoryDomainService.add(subjectCategoryBO);
            return Result.ok(true);
        } catch (Exception e) {
            log.error("SubjectCategoryController.add.error:{}", e.getMessage(), e);
            return Result.fail(e.getMessage());
        }
    }


    @GetMapping("/queryPrimaryCategory")
    public Result<List<SubjectCategoryDTO>> queryPrimaryCategory() {
        try {
            SubjectCategoryBO subjectCategoryBO = new SubjectCategoryBO();
            List<SubjectCategoryBO> subjectCategoryBOList = subjectCategoryDomainService.queryCategory(subjectCategoryBO);
            List<SubjectCategoryDTO> subjectCategoryDTOList = SubjectCategoryDTOConverter.INSTANCE
                    .convertBoListToCategoryDTOList(subjectCategoryBOList);
            return Result.ok(subjectCategoryDTOList);
        } catch (Exception e) {
            log.error("SubjectCategoryController.queryPrimaryCategory.error:{}", e.getMessage(), e);
            return Result.fail("查询失败");
        }
    }

    @GetMapping("/queryCategoryByPrimary")
    public Result<List<SubjectCategoryDTO>> queryCategoryByPrimary(@RequestBody  SubjectCategoryDTO subjectCategoryDTO) {
        try {
            if (log.isInfoEnabled()) {
                log.info("SubjectCategoryController.queryCategoryByPrimary.subjectCategoryDTO:{}"
                        , JSON.toJSONString(subjectCategoryDTO));
            }
            Preconditions.checkNotNull(subjectCategoryDTO.getParentId(), "大类下分类id不能为空");
            SubjectCategoryBO subjectCategoryBO = SubjectCategoryDTOConverter.INSTANCE.convertDtoToCategoryBo(subjectCategoryDTO);

            List<SubjectCategoryBO> subjectCategoryBOList = subjectCategoryDomainService.queryCategory(subjectCategoryBO);
            List<SubjectCategoryDTO> subjectCategoryDTOList = SubjectCategoryDTOConverter.INSTANCE
                    .convertBoListToCategoryDTOList(subjectCategoryBOList);
            return Result.ok(subjectCategoryDTOList);
        } catch (Exception e) {
            log.error("SubjectCategoryController.queryCategoryByPrimary.error:{}", e.getMessage(), e);
            return Result.fail("查询失败");
        }
    }
}
