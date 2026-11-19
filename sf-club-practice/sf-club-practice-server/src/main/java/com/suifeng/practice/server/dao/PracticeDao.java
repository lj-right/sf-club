package com.suifeng.practice.server.dao;

import com.suifeng.practice.server.entity.po.PracticePO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PracticeDao {

    /**
     * 根据练习id获取详情
     */
    PracticePO selectById(Long id);

    /**
     * 新增
     */
    int insert(PracticePO practicePO);

    /**
     * 更新练习详情
     */
    int update(PracticePO practicePO);

    /**
     * 删除练习
     */
    int deleteById(Long id);

    /**
     * 查询未完成的练习题数量
     */
    Integer getUnCompleteCount(String loginId);

    /**
     * 查询未完成的练习题
     */
    List<PracticePO> getUnCompleteList(@Param("loginId") String loginId,
                                       @Param("limit") int limit,
                                       @Param("offset") int pageSize);

    /**
     * 查询用户所有练习记录总数（包含已完成和未完成）
     */
    Integer getPracticeHistoryCount(@Param("loginId") String loginId);

    /**
     * 查询用户练习记录列表（降序）
     */
    List<PracticePO> getPracticeHistoryList(@Param("loginId") String loginId,
                                            @Param("limit") int limit,
                                            @Param("offset") int pageSize);

}
