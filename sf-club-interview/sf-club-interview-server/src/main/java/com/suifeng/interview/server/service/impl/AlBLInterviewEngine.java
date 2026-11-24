package com.suifeng.interview.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.suifeng.interview.api.enums.EngineEnum;
import com.suifeng.interview.api.req.InterviewSubmitReq;
import com.suifeng.interview.api.req.StartReq;
import com.suifeng.interview.api.vo.InterviewQuestionVO;
import com.suifeng.interview.api.vo.InterviewResultVO;
import com.suifeng.interview.api.vo.InterviewVO;
import com.suifeng.interview.server.service.InterviewEngine;
import com.suifeng.interview.server.util.EvaluateUtils;
import com.suifeng.interview.server.util.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@SuppressWarnings("all")
public class AlBLInterviewEngine implements InterviewEngine {

    //换自己的token
    private static final String apiKey = "sk-44209xxxxx";

    @Override
    public EngineEnum engineType() {
        return EngineEnum.ALI_BL;
    }

    @Override
    public InterviewVO analyse(List<String> KeyWords) {
        InterviewVO vo = new InterviewVO();
        List<InterviewVO.Interview> questionList = KeyWords.stream().map(item -> {
            InterviewVO.Interview interview = new InterviewVO.Interview();
            interview.setKeyWord(item);
            interview.setCategoryId(-1L);
            interview.setLabelId(-1L);
            return interview;
        }).collect(Collectors.toList());
        vo.setQuestionList(questionList);
        return vo;
    }

    @Override
    public InterviewResultVO submit(InterviewSubmitReq req) {

        long start = System.currentTimeMillis();
        List<CompletableFuture<InterviewSubmitReq.Submit>> list = req.getQuestionList().stream().
                map(keyword -> CompletableFuture.supplyAsync(() -> buildInterviewScore(keyword)))
                .collect(Collectors.toList());
        List<InterviewSubmitReq.Submit> interviews = new ArrayList<>();
        list.forEach(future -> {
            try {
                if (Objects.nonNull(future)) {
                    InterviewSubmitReq.Submit interview = future.get();
                    interviews.add(interview);
                }
            } catch (Exception e) {
                log.error("buildInterview.get.error", e);
            }
        });
        req.setQuestionList(interviews);
        String tips = interviews.stream().map(item -> {
            String keyWord = item.getLabelName();
            String evaluate = EvaluateUtils.evaluate(item.getUserScore());
            return String.format(evaluate, keyWord);
        }).distinct().collect(Collectors.joining(";"));
        List<InterviewSubmitReq.Submit> submits = req.getQuestionList();
        double total = submits.stream().mapToDouble(InterviewSubmitReq.Submit::getUserScore).sum();
        double avg = total / submits.size();
        String avtTips = EvaluateUtils.avgEvaluate(avg);
        InterviewResultVO vo = new InterviewResultVO();
        vo.setAvgScore(avg);
        vo.setTips(tips);
        vo.setAvgTips(avtTips);
        log.info("submit total cost {}", System.currentTimeMillis() - start);
        return vo;

    }

    @Override
    public InterviewQuestionVO start(StartReq req) {

        long start = System.currentTimeMillis();
        List<String> keywords = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            StartReq.Key key = req.getQuestionList().get(new Random().nextInt(req.getQuestionList().size()));
            keywords.add(key.getKeyWord());
        }
        List<CompletableFuture<InterviewQuestionVO.Interview>> list = keywords.stream().
                map(keyword -> CompletableFuture.supplyAsync(() -> buildInterview(keyword)))
                .collect(Collectors.toList());
        List<InterviewQuestionVO.Interview> interviews = new ArrayList<>();
        list.forEach(future -> {
            try {
                if (Objects.nonNull(future)) {
                    InterviewQuestionVO.Interview interview = future.get();
                    interviews.add(interview);
                }
            } catch (Exception e) {
                log.error("buildInterview.get.error", e);
            }
        });
        InterviewQuestionVO vo = new InterviewQuestionVO();
        vo.setQuestionList(interviews);
        log.info("start total cost {}", System.currentTimeMillis() - start);
        return vo;

    }


    private static InterviewSubmitReq.Submit buildInterviewScore(InterviewSubmitReq.Submit submit) {
        long start = System.currentTimeMillis();
        Map<String, Object> reqMap = new HashMap<>();
        JSONObject jsonData = new JSONObject();
        jsonData.put("model", "qwen1.5-110b-chat");
        JSONObject input = new JSONObject();
        JSONObject message2 = new JSONObject();
        message2.put("role", "user");
        String subject = String.format("作为顶级互联网大厂资深技术面试官，请根据面试题目【%s】与候选人实际作答【%s】，严格依据技术专业性、原理掌握深度及完整性评定合理得分（0-5分），并给出详尽的专业参考解答。严格按照JSON格式返回：{\"userScore\":5,\"subjectAnswer\":\"参考答案详细解析\"}，切勿输出任何多余说明。", 
                submit.getSubjectName(), submit.getUserAnswer());
        log.info("buildInterview {}", subject);
        message2.put("content", subject);
        input.put("messages", new JSONObject[]{message2});
        jsonData.put("input", input);
        jsonData.put("parameters", new JSONObject());

        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", "Bearer " + apiKey);
        headerMap.put("Content-Type", "application/json");
        headerMap.put("X-DashScope-SSE", "enable");

        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
        String body = HttpUtils.executePost(url, jsonData.toJSONString(), headerMap);

        // 输出响应结果
        int index = body.lastIndexOf("data:");
        String substring = body.substring(index + 5, body.length() - 1);
        JSONObject jsonObject = JSONObject.parseObject(substring);
        String text = jsonObject.getJSONObject("output").getString("text");
        int jsonIndex = text.lastIndexOf("```json");
        String json = text.substring(jsonIndex + 7, text.lastIndexOf("```"));
        InterviewSubmitReq.Submit interviews = JSONObject.parseObject(json, InterviewSubmitReq.Submit.class);
        interviews.setLabelName(submit.getLabelName());
        interviews.setSubjectName(submit.getSubjectName());
        interviews.setUserAnswer(submit.getUserAnswer());
        log.info("cost {} data:{}", System.currentTimeMillis() - start, JSON.toJSONString(interviews));
        return interviews;
    }

    private static InterviewQuestionVO.Interview buildInterview(String keyword) {
        long start = System.currentTimeMillis();
        JSONObject jsonData = new JSONObject();
        jsonData.put("model", "qwen1.5-110b-chat");
        JSONObject input = new JSONObject();
        JSONObject message2 = new JSONObject();
        message2.put("role", "user");
        String subject = String.format("你是一位顶级大厂资深技术面试官。请针对技术关键词【%s】，紧扣生产实际开发场景与底层源码原理，精选生成1道具备含金量的高频核心技术考题及对应技能分类标签。严格按照JSON格式返回：{\"labelName\":\"分类名称\",\"subjectName\":\"题目内容\"}，切勿输出多余说明或Markdown标记。", keyword);
        log.info("buildInterview {}", subject);
        message2.put("content", subject);
        input.put("messages", new JSONObject[]{message2});
        jsonData.put("input", input);
        jsonData.put("parameters", new JSONObject());


        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", "Bearer " + apiKey);
        headerMap.put("Content-Type", "application/json");
        headerMap.put("X-DashScope-SSE", "enable");

        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
        String body = HttpUtils.executePost(url, jsonData.toJSONString(), headerMap);

        // 输出响应结果
        int index = body.lastIndexOf("data:");
        String substring = body.substring(index + 5, body.length() - 1);
        JSONObject jsonObject = JSONObject.parseObject(substring);
        String text = jsonObject.getJSONObject("output").getString("text");
        int jsonIndex = text.lastIndexOf("```json");
        String json = text.substring(jsonIndex + 7, text.lastIndexOf("```"));
        InterviewQuestionVO.Interview interviews = JSONObject.parseObject(json, InterviewQuestionVO.Interview.class);
        log.info("cost {} data:{}", System.currentTimeMillis() - start, JSON.toJSONString(interviews));
        return interviews;
    }

}
