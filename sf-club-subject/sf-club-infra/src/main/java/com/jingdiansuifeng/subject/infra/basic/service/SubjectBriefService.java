package com.jingdiansuifeng.subject.infra.basic.service;

import com.jingdiansuifeng.subject.infra.basic.entity.SubjectBrief;

import java.util.List;

/**
 * 简答题信息表(SubjectBrief)表服务接口
 *
 * @author makejava
 * @since 2025-04-07 10:33:48
 */
public interface SubjectBriefService {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    SubjectBrief queryById(Long id);


    /**
     * 新增数据
     *
     * @param subjectBrief 实例对象
     * @return 实例对象
     */
    SubjectBrief insert(SubjectBrief subjectBrief);

    /**
     * 修改数据
     *
     * @param subjectBrief 实例对象
     * @return 实例对象
     */
    SubjectBrief update(SubjectBrief subjectBrief);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Long id);

    void batchInsert(List<SubjectBrief> subjectBriefList);

    SubjectBrief queryByCondition(SubjectBrief subjectBrief);
}
