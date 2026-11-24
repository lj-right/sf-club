package com.suifeng.interview.server.service.impl;

import com.google.common.base.Preconditions;
import com.suifeng.interview.api.enums.EngineEnum;
import com.suifeng.interview.api.req.InterviewReq;
import com.suifeng.interview.api.req.InterviewSubmitReq;
import com.suifeng.interview.api.req.StartReq;
import com.suifeng.interview.api.vo.InterviewQuestionVO;
import com.suifeng.interview.api.vo.InterviewResultVO;
import com.suifeng.interview.api.vo.InterviewVO;
import com.suifeng.interview.server.dao.SubjectDao;
import com.suifeng.interview.server.entity.po.SubjectLabel;
import com.suifeng.interview.server.service.InterviewEngine;
import com.suifeng.interview.server.service.InterviewService;
import com.suifeng.interview.server.util.PDFUtil;
import com.suifeng.interview.server.util.keyword.KeyWordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InterviewServiceImpl implements InterviewService, ApplicationContextAware {

    private static final Map<String, InterviewEngine> engineMap = new HashMap<>();

    private static final List<String> COMMON_TECH_WORDS = Arrays.asList(
            "Java", "Spring", "SpringBoot", "Spring Boot", "MySQL", "Redis",
            "MyBatis", "Kafka", "RocketMQ", "Docker", "Kubernetes", "K8s",
            "JVM", "Linux", "Netty", "Vue", "React", "多线程", "并发", "微服务"
    );

    @Resource
    private SubjectDao subjectLabelDao;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Collection<InterviewEngine> engines = applicationContext.getBeansOfType(InterviewEngine.class).values();
        for (InterviewEngine engine : engines) {
            engineMap.put(engine.engineType().name(), engine);
        }
    }

    @Override
    public InterviewVO analyse(InterviewReq req) {
        List<String> keyWords = buildKeyWords(req.getUrl());
        InterviewEngine engine = engineMap.get(req.getEngine());
        Preconditions.checkArgument(!Objects.isNull(engine), "引擎不能为空！");
        return engine.analyse(keyWords);
    }

    @Override
    public InterviewVO analyseFile(MultipartFile file, String engineName) {
        Preconditions.checkArgument(!Objects.isNull(file) && !file.isEmpty(), "上传简历文件不能为空！");
        List<String> keyWords = buildKeyWordsFromFile(file);
        if (!StringUtils.hasText(engineName)) {
            engineName = EngineEnum.JI_CHI.name();
        }
        InterviewEngine engine = engineMap.get(engineName);
        if (engine == null) {
            engine = engineMap.get(EngineEnum.JI_CHI.name());
        }
        Preconditions.checkArgument(!Objects.isNull(engine), "未找到匹配的面试引擎！");
        return engine.analyse(keyWords);
    }

    @Override
    public InterviewQuestionVO start(StartReq req) {
        InterviewEngine engine = engineMap.get(req.getEngine());
        Preconditions.checkArgument(!Objects.isNull(engine), "引擎不能为空！");
        return engine.start(req);
    }

    @Override
    public InterviewResultVO submit(InterviewSubmitReq req) {
        InterviewEngine engine = engineMap.get(req.getEngine());
        Preconditions.checkArgument(!Objects.isNull(engine), "引擎不能为空！");
        return engine.submit(req);
    }

    private List<String> buildKeyWords(String url) {
        String pdfText = PDFUtil.getPdfText(url);
        ensureKeyWordInit();
        List<String> result = KeyWordUtil.buildKeyWordsLists(pdfText);
        if (result.isEmpty()) {
            result = extractCommonKeywordsFallback(pdfText);
        }
        return result;
    }

    private List<String> buildKeyWordsFromFile(MultipartFile file) {
        String pdfText = "";
        try {
            pdfText = PDFUtil.getPdfText(file.getInputStream());
        } catch (Exception e) {
            log.error("从文件提取简历文本失败: {}", e.getMessage(), e);
            throw new RuntimeException("读取简历文件失败: " + e.getMessage());
        }
        ensureKeyWordInit();
        List<String> result = KeyWordUtil.buildKeyWordsLists(pdfText);
        if (result.isEmpty()) {
            result = extractCommonKeywordsFallback(pdfText);
        }
        return result;
    }

    private synchronized void ensureKeyWordInit() {
        if (!KeyWordUtil.isInit()) {
            try {
                List<SubjectLabel> labels = subjectLabelDao.listAllLabel();
                if (labels != null && !labels.isEmpty()) {
                    List<String> list = labels.stream().map(SubjectLabel::getLabelName).collect(Collectors.toList());
                    KeyWordUtil.addWord(list);
                }
            } catch (Exception e) {
                log.warn("从数据库初始化关键词词典异常，启用通用词典: {}", e.getMessage());
                KeyWordUtil.addWord(COMMON_TECH_WORDS);
            }
        }
    }

    private List<String> extractCommonKeywordsFallback(String text) {
        if (!StringUtils.hasText(text)) {
            return Collections.emptyList();
        }
        String lower = text.toLowerCase(Locale.ROOT);
        List<String> matched = new ArrayList<>();
        for (String word : COMMON_TECH_WORDS) {
            if (lower.contains(word.toLowerCase(Locale.ROOT))) {
                matched.add(word);
            }
        }
        return matched;
    }
}
