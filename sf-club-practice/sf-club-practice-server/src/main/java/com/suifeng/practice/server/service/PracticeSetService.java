package com.suifeng.practice.server.service;

import com.suifeng.practice.api.vo.SpecialPractiveVO;

import java.util.List;

public interface PracticeSetService {

    /**
     * 获取专项练习内容
     * @return
     */
    List<SpecialPractiveVO> getSpecialPracticeContent();
}
