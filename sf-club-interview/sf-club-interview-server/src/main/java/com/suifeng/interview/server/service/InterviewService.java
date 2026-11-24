package com.suifeng.interview.server.service;

import com.suifeng.interview.api.req.InterviewReq;
import com.suifeng.interview.api.req.InterviewSubmitReq;
import com.suifeng.interview.api.req.StartReq;
import com.suifeng.interview.api.vo.InterviewQuestionVO;
import com.suifeng.interview.api.vo.InterviewResultVO;
import com.suifeng.interview.api.vo.InterviewVO;
import org.springframework.web.multipart.MultipartFile;

public interface InterviewService {

    InterviewVO analyse(InterviewReq req);

    /**
     * 直接从上传的简历文件解析关键字并生成题目
     */
    InterviewVO analyseFile(MultipartFile file, String engine);

    InterviewQuestionVO start(StartReq req);

    InterviewResultVO submit(InterviewSubmitReq req);
}
