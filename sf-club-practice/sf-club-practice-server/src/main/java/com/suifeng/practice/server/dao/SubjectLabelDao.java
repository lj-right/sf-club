package com.suifeng.practice.server.dao;


import com.suifeng.practice.server.entity.po.SubjectLabelPO;

/**
 * 题目标签表(SubjectLabel)表数据库访问层
 *
 * @author makejava
 * @since 2025-04-06 09:32:15
 */
public interface SubjectLabelDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    SubjectLabelPO queryById(Long id);

}

