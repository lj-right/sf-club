package com.jingdiansuifeng.subject.application.mq;

import com.alibaba.fastjson.JSON;
import com.jingdiansuifeng.subject.domain.entity.SubjectInfoEs;
import com.jingdiansuifeng.subject.domain.service.SubjectInfoDomainService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 题目检索 ES 索引同步消费者
 */
@Component
@RocketMQMessageListener(topic = "subject-index", consumerGroup = "subject-index-consumer")
@Slf4j
public class SubjectEsConsumer implements RocketMQListener<String> {

    @Resource
    private SubjectInfoDomainService subjectInfoDomainService;

    @Override
    public void onMessage(String s) {
        if (log.isInfoEnabled()) {
            log.info("SubjectEsConsumer.接收到题目检索同步mq，消息为：{}", s);
        }
        try {
            SubjectInfoEs subjectInfoEs = JSON.parseObject(s, SubjectInfoEs.class);
            subjectInfoDomainService.syncEsByMsg(subjectInfoEs);
        } catch (Exception e) {
            log.error("SubjectEsConsumer.syncEsByMsg.error:{}", e.getMessage(), e);
        }
    }
}
