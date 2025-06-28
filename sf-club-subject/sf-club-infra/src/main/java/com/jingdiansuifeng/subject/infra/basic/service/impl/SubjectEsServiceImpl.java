package com.jingdiansuifeng.subject.infra.basic.service.impl;

import com.jingdiansuifeng.auth.common.entity.PageResult;
import com.jingdiansuifeng.subject.infra.basic.entity.EsSubjectFields;
import com.jingdiansuifeng.subject.infra.basic.entity.SubjectInfoEs;
import com.jingdiansuifeng.subject.infra.basic.es.EsIndexInfo;
import com.jingdiansuifeng.subject.infra.basic.es.EsRestClient;
import com.jingdiansuifeng.subject.infra.basic.es.EsSourceData;
import com.jingdiansuifeng.subject.infra.basic.service.SubjectEsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class SubjectEsServiceImpl implements SubjectEsService {

    @Override
    public Boolean insert(SubjectInfoEs subjectInfoEs) {
        EsSourceData esSourceData = new EsSourceData();
        Map<String,Object> data = convert2EsSourceData(subjectInfoEs);
        esSourceData.setDocId(subjectInfoEs.getDocId().toString());
        esSourceData.setData(data);
        return EsRestClient.insertDoc(getEsIndexInfo(), esSourceData);
    }

    private Map<String, Object> convert2EsSourceData(SubjectInfoEs subjectInfoEs) {
        Map<String, Object> data = new HashMap<>();
        data.put(EsSubjectFields.SUBJECT_ID, subjectInfoEs.getSubjectId());
        data.put(EsSubjectFields.DOC_ID, subjectInfoEs.getDocId());
        data.put(EsSubjectFields.SUBJECT_NAME, subjectInfoEs.getSubjectName());
        data.put(EsSubjectFields.SUBJECT_ANSWER, subjectInfoEs.getSubjectAnswer());
        data.put(EsSubjectFields.SUBJECT_TYPE, subjectInfoEs.getSubjectType());
        data.put(EsSubjectFields.CREATE_USER, subjectInfoEs.getCreateUser());
        data.put(EsSubjectFields.CREATE_TIME, subjectInfoEs.getCreateTime());
        return data;
    }

    @Override
    public PageResult<SubjectInfoEs> querySubjectList(SubjectInfoEs subjectInfoEs) {
        return null;
    }

    private EsIndexInfo getEsIndexInfo(){
        EsIndexInfo esIndexInfo = new EsIndexInfo();
        esIndexInfo.setClusterName("b4a3cd3300b0");
        esIndexInfo.setIndexName("subject_info");
        return esIndexInfo;
    }
}
